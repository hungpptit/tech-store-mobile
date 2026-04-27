# Hướng dẫn chuẩn bị Stripe sandbox thanh toán Visa cho `tech-store-mobile`

> Mục tiêu: chuẩn bị môi trường Stripe **test mode / sandbox** để app Android Java này thanh toán thẻ Visa an toàn, đúng chuẩn, và sẵn sàng cắm API backend sau này.
>
> Lưu ý quan trọng: **không lưu số thẻ, CVC, expiry vào Firestore**. Với Stripe, app chỉ nên nhận `publishable key` và `client_secret`; còn `secret key` phải nằm ở backend.

---

## 1) Tóm tắt luồng chuẩn nên dùng

Luồng đề xuất cho dự án này:

1. User chọn sản phẩm / giỏ hàng / đơn hàng.
2. App Android gọi backend của bạn để tạo `PaymentIntent`.
3. Backend dùng `Stripe Secret Key` để tạo `PaymentIntent` trong **test mode**.
4. Backend trả về `client_secret` cho app.
5. App dùng Stripe Android SDK để mở giao diện thanh toán (khuyến nghị `PaymentSheet`).
6. User nhập thẻ Visa test và xác nhận thanh toán.
7. Stripe trả kết quả thanh toán.
8. Backend kiểm tra kết quả bằng **webhook** và cập nhật đơn hàng / trạng thái thanh toán.

**Không nên làm:**
- Không tự lưu card number / CVC / expiry trong Firestore.
- Không để `Stripe Secret Key` trong app.
- Không tự validate/encode dữ liệu thẻ như một form local rồi gửi lên Firestore.

---

## 2) Tình trạng hiện tại của dự án

Dự án hiện đang dùng Firebase cho auth / Firestore / cart / saved / payment method UI.
Các màn như `AddCardFragment`, `PaymentMethodFragment` đang giống kiểu quản lý thẻ nội bộ.

Với Stripe, phần đó chỉ nên xem là **UI hỗ trợ thanh toán** hoặc **lịch sử/phương thức hiển thị**, còn thông tin thẻ thật phải xử lý qua Stripe SDK và backend.

Nếu sau này bạn muốn giữ màn “Payment Method” trong app, thì có thể dùng nó để:
- hiển thị phương thức thanh toán đã chọn
- chọn thẻ / ví / phương thức đã lưu của Stripe
- không lưu card data thô

---

## 3) Tài khoản Stripe và chế độ sandbox

### 3.1 Tạo tài khoản Stripe

1. Đăng ký / đăng nhập tại Stripe Dashboard.
2. Bật **Test mode**.
3. Vào mục Developers để lấy:
   - **Publishable key**: dùng trong app
   - **Secret key**: dùng ở backend
4. Không dùng key production trong giai đoạn test.

### 3.2 Hai loại key phải nhớ

- `Publishable key`
  - có thể đặt trong Android app
  - ví dụ placeholder: `YOUR_STRIPE_PUBLISHABLE_KEY`
  - an toàn hơn nhưng vẫn nên quản lý cẩn thận

- `Secret key`
  - chỉ dùng ở backend
  - ví dụ placeholder: `YOUR_STRIPE_SECRET_KEY`
  - **không được** nhúng vào Android app

### 3.3 Cấu hình key đúng nguyên tắc

**Android app**
- chỉ nhận `publishable key`
- chỉ gọi backend của bạn
- nhận `client_secret`

**Backend**
- dùng `secret key` để tạo `PaymentIntent`
- xác thực thanh toán bằng webhook
- cập nhật trạng thái đơn hàng

---

## 4) Cấu hình Android cho dự án này

Dự án đang dùng Android Java, `minSdk 24`, `Java 11`.

### 4.1 Dependency Stripe cần thêm vào `app/build.gradle`

Bạn sẽ cần thêm Stripe Android SDK, ví dụ:

```gradle
implementation 'com.stripe:stripe-android:<latest-version>'
```

> Khi bạn chốt version, nên chọn version mới nhất tương thích với project hiện tại.

### 4.2 Nên lưu publishable key ở đâu

Tạm thời bạn có thể để một hằng số placeholder trong app:

```java
private static final String STRIPE_PUBLISHABLE_KEY = "YOUR_STRIPE_PUBLISHABLE_KEY";
```

Hoặc tốt hơn:
- để trong `BuildConfig`
- hoặc Android string resource / local config

Ví dụ:

```java
String publishableKey = BuildConfig.STRIPE_PUBLISHABLE_KEY;
```

### 4.3 Không nên làm trong Android

Không nên:
- tạo `PaymentIntent` trực tiếp từ app
- gắn `secret key` trong source Android
- lưu card number / cvc / expiry vào Firestore
- tự mô phỏng “add card” bằng dữ liệu thẻ thật

---

## 5) Cấu hình backend tối thiểu

Stripe thanh toán chuẩn cần backend để tạo `PaymentIntent`.

### 5.1 Endpoint tối thiểu nên có

Bạn nên chuẩn bị 1 API dạng:

- `POST /api/payments/create-payment-intent`

Input gợi ý:
- `orderId`
- `amount`
- `currency`
- `customerId` hoặc `userId`
- dữ liệu đơn hàng liên quan

Output gợi ý:
- `clientSecret`
- `paymentIntentId`
- có thể trả thêm `amount`, `currency`

### 5.2 Logic backend

Backend sẽ:
1. kiểm tra user/đơn hàng
2. tính lại số tiền từ server, **không tin số tiền từ client 100%**
3. tạo `PaymentIntent`
4. trả `client_secret` về app
5. webhook nhận kết quả thanh toán thành công / thất bại
6. cập nhật đơn hàng trong database

### 5.3 Webhook rất quan trọng

Webhook nên xử lý các event như:
- `payment_intent.succeeded`
- `payment_intent.payment_failed`
- `charge.succeeded`

Dùng webhook để:
- xác nhận thanh toán thật sự đã hoàn tất
- tránh trường hợp app báo thành công nhưng server chưa xác nhận

---

## 6) Kiến trúc đề xuất cho app này

### 6.1 Luồng tốt nhất

```text
Android App
   -> Backend API
      -> Stripe PaymentIntent
         -> client_secret trả về app
            -> Stripe PaymentSheet / UI thanh toán
               -> thanh toán test Visa
                  -> webhook backend xác nhận
                     -> cập nhật trạng thái đơn hàng
```

### 6.2 Gợi ý mapping với project hiện tại

Có thể tách như sau:

- `CartFragment`
  - bấm checkout → sang màn hình thanh toán

- `PaymentMethodFragment`
  - chọn phương thức thanh toán / hiển thị payment option
  - sau này có thể dùng để mở Stripe checkout

- `AddCardFragment`
  - nếu giữ lại, chỉ nên là màn UI mô tả / chọn thẻ, không nên lưu card thô

- `Order / Checkout screen`
  - gọi backend tạo payment intent
  - nhận `client_secret`
  - mở Stripe payment flow

### 6.3 Khuyến nghị

Nếu mục tiêu là thanh toán Visa qua Stripe sandbox, bạn nên dùng:
- **PaymentSheet**: dễ tích hợp, an toàn hơn, ít code UI thẻ
- hoặc **CardField / CardForm UI** của Stripe nếu bạn muốn tự thiết kế form

Khuyến nghị thực tế: **PaymentSheet**.

---

## 7) Cách chuẩn bị sandbox Visa test

### 7.1 Chế độ test

Đảm bảo:
- dashboard đang ở **Test mode**
- backend dùng `test secret key`
- app dùng `test publishable key`

### 7.2 Test card phổ biến

Một số card test hay dùng trong sandbox:

- Visa thành công: `4242 4242 4242 4242`
- Mastercard test khác: `5555 5555 5555 4444`
- Amex test: `3782 822463 10005`

Thông tin thường dùng khi test:
- Expiry: tháng/năm tương lai
- CVC: `123`
- Postal code: bất kỳ (tuỳ flow)

> Với Visa sandbox, thường dùng `4242 4242 4242 4242`.

### 7.3 Các case nên test

Nên test ít nhất các tình huống:
- thanh toán thành công
- thẻ bị từ chối
- payment bị require authentication (nếu flow hỗ trợ 3D Secure)
- nhập số tiền lớn / nhỏ
- retry sau khi fail
- mạng yếu / mất kết nối

---

## 8) Những gì cần chuẩn bị sẵn trong backend API

### 8.1 Thông tin tạm thời bạn sẽ điền sau

Bạn nói sẽ cung cấp API sau, nên tạm thời để placeholder:

- `YOUR_BACKEND_BASE_URL`
- `YOUR_STRIPE_PUBLISHABLE_KEY`
- `YOUR_STRIPE_SECRET_KEY`
- `YOUR_CREATE_PAYMENT_INTENT_ENDPOINT`

### 8.2 Dữ liệu nên gửi lên backend

Khi app request tạo thanh toán, gửi tối thiểu:
- `userId`
- `orderId`
- `items`
- `shippingAddressId`
- `amount` nếu backend vẫn cần tham khảo

Nhưng server vẫn phải tự kiểm tra lại amount.

### 8.3 Dữ liệu backend nên trả về

Tối thiểu trả:
- `clientSecret`
- `paymentIntentId`
- `orderId`
- `status`

---

## 9) Gợi ý tích hợp vào Android app này

### 9.1 Khi user bấm Checkout ở cart

Nên làm:
1. kiểm tra login
2. tạo order tạm / lấy order id
3. gọi backend tạo payment intent
4. mở Stripe PaymentSheet
5. khi thành công, cập nhật order status

### 9.2 Trạng thái nên lưu trong Firestore

Không lưu card data.
Chỉ nên lưu:
- `orderId`
- `userId`
- `amount`
- `currency`
- `paymentIntentId`
- `paymentStatus`
- `createdAt`
- `updatedAt`

### 9.3 Nếu muốn lưu “phương thức thanh toán”

Nếu bạn cần màn payment method sau này:
- lưu **metadata** như tên phương thức, last4, brand, default flag
- không lưu full card number, expiry, CVC

Ví dụ metadata:
- `cardBrand = VISA`
- `last4 = 4242`
- `isDefault = true`

---

## 10) Mẫu checklist triển khai

### Trước khi code
- [ ] Có Stripe account test mode
- [ ] Có publishable key test
- [ ] Có secret key test
- [ ] Có backend endpoint tạo PaymentIntent
- [ ] Có webhook endpoint
- [ ] Có test card Visa

### Trong app
- [ ] Thêm Stripe Android SDK
- [ ] Chỉ dùng publishable key
- [ ] Gọi backend để lấy `client_secret`
- [ ] Mở PaymentSheet / Stripe UI
- [ ] Không lưu card thô vào Firestore

### Trên backend
- [ ] Tạo PaymentIntent từ secret key
- [ ] Xác thực amount từ server
- [ ] Xử lý webhook
- [ ] Cập nhật trạng thái đơn hàng

---

## 11) Lưu ý bảo mật rất quan trọng

1. **Không để secret key trong app**.
2. **Không lưu số thẻ / CVC / expiry** trong Firestore.
3. **Không tin số tiền do client gửi lên**.
4. **Webhook phải là nguồn xác nhận thanh toán cuối cùng**.
5. **Chỉ dùng test keys trong sandbox**.
6. Khi chuyển production, phải thay toàn bộ test key và test card.

---

## 12) Hướng đi tiếp theo sau khi bạn đưa API

Khi bạn cung cấp API, mình sẽ giúp bạn nối tiếp theo đúng luồng sau:

- thêm dependency Stripe vào Android project
- tạo lớp API client để gọi backend
- tạo màn checkout / payment flow
- gắn `client_secret` vào Stripe PaymentSheet
- xử lý success / fail
- cập nhật Firestore order status

---

## 13) Kết luận ngắn

Để làm thanh toán Visa bằng Stripe sandbox đúng chuẩn cho app này:

- app chỉ giữ **publishable key**
- backend giữ **secret key**
- backend tạo `PaymentIntent`
- app nhận `client_secret`
- user thanh toán qua Stripe UI
- webhook xác nhận kết quả
- Firestore chỉ lưu đơn hàng / trạng thái, không lưu dữ liệu thẻ thô

---

> Khi bạn gửi API backend sau, mình sẽ dựa trên file hướng dẫn này để viết tiếp phần tích hợp thực tế cho dự án.

