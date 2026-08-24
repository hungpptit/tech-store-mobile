# BÁO CÁO CHI TIẾT LUỒNG HOẠT ĐỘNG, SỰ LIÊN KẾT MODEL - ADAPTER VÀ CƠ SỞ LÝ THUYẾT

Tài liệu này phân tích chi tiết quy trình xử lý (workflow) của 4 phân hệ chính trong ứng dụng **Tech Store Mobile**:
1. **Thanh toán (Payment & Checkout)**
2. **Theo dõi đơn hàng (Order Tracking)**
3. **Đánh giá sản phẩm (Product Review)**
4. **Chat trực tuyến Real-time (Customer Service Chat)**

Để hệ thống vận hành hoàn chỉnh, các Fragment không hoạt động cô lập mà luôn phối hợp chặt chẽ với các lớp dữ liệu nền tảng (**Model**) và bộ điều phối hiển thị giao diện (**Adapter**). Dưới đây là phân tích chi tiết cho từng chức năng.

---

## PHÂN HỆ 1: THANH TOÁN (PAYMENT FLOW)

### 1. Màn hình Checkout & Thuật toán Giữ kho (Stock Reservation)

*   **Sự kiện UI (Click nút):** Người dùng nhấn nút **"Place Order"** (`btnPlaceOrder`) trên màn hình Checkout.
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `preLoadCheckoutItems(String userId, int index, List<OrderItem> loadedItems, OnItemsLoadedListener listener)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
        *   **Chức năng:** Tải danh sách chi tiết các mặt hàng cần thanh toán từ database Firestore.
        *   **Vai trò:** Đảm bảo dữ liệu về giá bán, số lượng thực tế trong giỏ của người dùng trước khi tiến hành thanh toán là hoàn toàn khớp với máy chủ, tránh gian lận hoặc sai lệch dữ liệu.
    2.  `performStockReservation(String userId, List<OrderItem> items)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
        *   **Chức năng:** Thực hiện kiểm tra tồn kho và trừ kho tạm thời trong một Firestore Transaction.
        *   **Vai trò:** Giữ hàng cho người dùng trong **5 phút** bằng cách đẩy Model `StockReservation` lên Firestore, tránh việc người khác mua mất sản phẩm trong lúc khách hàng đang điền thông tin thanh toán (tránh lỗi bán quá đà - overselling).
*   **Tệp tin xử lý chính:** `CheckoutFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_checkout.xml`
    *   **Layout gốc (Root Layout):** `ConstraintLayout` làm gốc (chứa `NestedScrollView` cuộn nội dung và nút `MaterialButton` dán cố định dưới đáy màn hình).
    *   **Layout con & Cấu trúc:** `LinearLayout` (định hướng Vertical) lồng bên trong `NestedScrollView` chứa các `RelativeLayout` (cho Toolbar), `MaterialCardView` (cho phần địa chỉ và chi tiết thanh toán).
    *   **Thành phần UI sử dụng:** `RecyclerView` (danh sách checkout items), `TextView` (thông tin hóa đơn, tổng tiền, địa chỉ), `ImageView` (nút Back, icon bản đồ/thẻ), `MaterialButton` (`btnPlaceOrder` - đặt hàng).
    *   **Mô tả thiết kế:** Sử dụng `ConstraintLayout` lồng trong `NestedScrollView` hỗ trợ cuộn màn hình. Bố cục chia rõ 3 phần: Thông tin địa chỉ nhận hàng (Top) bọc trong `MaterialCardView`, danh sách sản phẩm tóm tắt (RecyclerView ở giữa), chi tiết tiền thanh toán & phương thức thanh toán kèm nút Place Order (Bottom).
*   **Sự liên kết với Model và Adapter:**
    *   **Giai đoạn Giỏ hàng trước đó:** Tại `CartFragment`, `CartAdapter.java` quản lý danh sách giỏ hàng thông qua Model `CartItem.java`. Khi bấm thanh toán, `CartFragment` sẽ thu thập các sản phẩm được chọn và truyền sang `CheckoutFragment`.
    *   **Sử dụng Model tại Checkout:**
        *   `OrderItem.java` và `OrderSummary.java`: Dùng để đóng gói danh sách sản phẩm và tổng hợp tài chính (subtotal, shipping, vat, total).
        *   `StockReservation.java`: Dùng để ánh xạ dữ liệu giữ chỗ lưu vào collection `stock_reservations` gồm các thông tin: `reservationId`, `userId`, mảng items, trạng thái `pending`, thời gian tạo và thời gian hết hạn (5 phút).
*   **Mô tả hoạt động:**
    1.  `CheckoutFragment` gọi `preLoadCheckoutItems` để kiểm tra thông tin giỏ hàng hiện tại, đảm bảo thông tin sản phẩm và giá cả mới nhất.
    2.  Hàm `performStockReservation` khởi tạo một phiên giao dịch Firestore Transaction truy cập vào danh mục `products/{productId}` của từng mặt hàng được mua.
    3.  Trong transaction, hệ thống đọc `stockQuantity` (số lượng tồn kho khả dụng). Nếu tồn kho lớn hơn hoặc bằng số lượng đặt hàng (`quantity`), hệ thống tiến hành trừ kho tương ứng và tạo tài liệu giữ kho bằng cách đẩy đối tượng `StockReservation` lên Firestore.
    4.  Nếu hết hàng, Transaction sẽ bị hủy (Rollback) và kích hoạt Dialog báo lỗi thất bại thông qua hàm `showFailureDialog`.
*   **Lý thuyết liên quan:**
    *   **Firestore Transactions (ACID):** Đảm bảo các thao tác đọc và ghi diễn ra nguyên tử (atomic). Giúp chống tranh chấp tài nguyên (Race Condition) khi nhiều người mua sản phẩm cuối cùng cùng lúc.
    *   **Pessimistic Locking / Stock Reservation Pattern:** Giữ chỗ tồn kho tạm thời để đảm bảo hàng hóa được giữ lại cho khách hàng trong thời gian họ thực hiện thanh toán trực tuyến.

---

### 2. Luồng Thanh toán bằng Stripe (Stripe Payment Flow)

*   **Sự kiện UI (Click nút):** Kích hoạt tự động ngay sau khi bước giữ kho thành công trả về `OnSuccessListener` (nhận `reservationId`).
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `startStripePaymentFlowWithReservation(String userId, List<OrderItem> items)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
        *   **Chức năng:** Điều phối luồng thanh toán Stripe sau khi đã giữ kho thành công.
        *   **Vai trò:** Khởi tạo tiến trình tạo Payment Intent, khóa nút Place Order tránh nhấn đúp và đảm bảo các điều kiện cấu hình Stripe đã sẵn sàng.
    2.  `stripePaymentApiClient.createPaymentIntent(CreatePaymentIntentRequest request, Callback callback)`
        *   **Vị trí file:** Định nghĩa tại `app/src/main/java/com/example/tech_store_mobile/utils/StripePaymentApiClient.java` (được gọi từ `CheckoutFragment.java`)
        *   **Chức năng:** Gửi yêu cầu HTTP POST tạo Payment Intent kèm ID thẻ và số tiền lên backend Node.js.
        *   **Vai trò:** Kết nối ứng dụng Android với cổng thanh toán Stripe API thông qua API backend, đảm bảo bảo mật thông tin giao dịch.
    3.  `persistOrderInvoiceAndHistoryWithReservation(String userId, String orderId, CreatePaymentIntentResponse response, List<OrderItem> items)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
        *   **Chức năng:** Ghi nhận đồng thời Đơn hàng (`Order`), Hóa đơn (`HoaDon`), Lịch sử thanh toán (`LichSuThanhToan`) và cập nhật trạng thái giữ kho thành `completed`.
        *   **Vai trò:** Lưu trữ vết giao dịch thành công vào database một cách nhất quán (Atomic Write) và dọn sạch giỏ hàng của người dùng.
    4.  `releaseReservationImmediately(String reservationId)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
        *   **Chức năng:** Cộng trả lại số lượng tồn kho cho sản phẩm và giải phóng bản ghi giữ kho.
        *   **Vai trò:** Hoàn trả lại hàng cho kho ngay lập tức khi thanh toán thất bại để người dùng khác có thể mua, tránh lãng phí tồn kho.
*   **Tệp tin xử lý chính:** `CheckoutFragment.java` & `StripePaymentApiClient.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_checkout.xml` (phần tích hợp chọn phương thức thanh toán) và `app/src/main/res/layout/fragment_payment_method.xml`.
    *   **Layout gốc (Root Layout):** `ConstraintLayout` làm gốc cho cả 2 fragment (giúp tối ưu hóa giao diện phẳng, giảm phân cấp View).
    *   **Layout con & Cấu trúc:**
        - Trong `fragment_checkout.xml`: `MaterialCardView` chứa `LinearLayout` ngang hiển thị thông tin thẻ.
        - Trong `fragment_payment_method.xml`: `NestedScrollView` chứa `LinearLayout` dọc bao gồm `RecyclerView` danh sách thẻ và nút `AppCompatButton` dán đáy.
    *   **Thành phần UI sử dụng:** `TextView` (hiển thị số thẻ che cuối `**** **** **** 4242`), `ImageView` (icon nhãn hiệu Visa, Mastercard).
    *   **Mô tả thiết kế:** Được tích hợp gọn gàng trong khu vực Bottom của màn hình checkout. Khi người dùng click sẽ hiển thị phương thức thanh toán hiện tại, cho phép chuyển sang chọn thẻ lưu sẵn.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `CreatePaymentIntentRequest.java` và `CreatePaymentIntentResponse.java`: Lớp Model để đóng gói dữ liệu JSON gửi lên backend Node.js (bao gồm `userId`, `orderId`, `total`, `paymentMethodId`) và tiếp nhận dữ liệu phản hồi trả về từ cổng Stripe (chứa trạng thái giao dịch, ID giao dịch).
        *   `Order.java` & `ShippingAddressSnapshot.java`: Dùng để dựng thực thể đơn hàng lưu vào collection `orders` kèm snapshot địa chỉ giao hàng tại thời điểm đặt.
        *   `HoaDon.java` & `LichSuThanhToan.java`: Bản ghi hóa đơn và lịch sử giao dịch Stripe lưu trữ để quản lý đối soát tài chính sau này.
*   **Mô tả hoạt động:**
    1.  Khởi tạo đối tượng `CreatePaymentIntentRequest` truyền ID của thẻ đã lưu (`selectedPaymentMethod.getPaymentId()`) và số tiền cần thanh toán.
    2.  Gửi yêu cầu POST tới API backend (`/api/payment-intents/create`) thông qua Retrofit Client (`stripePaymentApiClient`).
    3.  Backend Node.js dùng SDK Stripe phía máy chủ để gửi xác nhận thanh toán trực tiếp lên Stripe API bằng phương thức thanh toán đã chọn.
    4.  Nếu kết quả trả về là `succeeded`: Gọi hàm `persistOrderInvoiceAndHistoryWithReservation(...)` ghi đồng loạt dữ liệu `Order`, `HoaDon` và `LichSuThanhToan` vào Firestore, giải phóng giỏ hàng và chuyển sang màn hình thành công.
    5.  Nếu kết quả thất bại: Gọi hàm `showFailureDialog` hiển thị lỗi cho người dùng và tự động gọi `releaseReservationImmediately(currentReservationId)` để hoàn lại số lượng tồn kho ngay lập tức.
*   **Lý thuyết liên quan:**
    *   **Stripe Tokenization & PCI-DSS Compliance:** Sử dụng thẻ đại diện (Payment Method Token) giúp ứng dụng không phải tiếp xúc với số thẻ thô của người dùng, đảm bảo tiêu chuẩn bảo mật PCI-DSS.

---

### 3. Quản lý Địa chỉ nhận hàng (Address Book / New Address)

*   **Sự kiện UI (Click nút):** 
    *   Nhấn nút **"Change Address"** (`btnChangeAddress`) $\rightarrow$ Mở danh sách địa chỉ.
    *   Nhấn nút **"Add Address"** (`btnAdd`) trên màn hình thêm địa chỉ mới.
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `setupLocationSpinners()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddAddressFragment.java`
        *   **Chức năng:** Cấu hình và nạp dữ liệu các Spinner Tỉnh/Huyện/Xã hành chính.
        *   **Vai trò:** Liên kết API địa chính Việt Nam để cập nhật động danh sách đơn vị hành chính theo lựa chọn của người dùng, đảm bảo nhập địa chỉ chính xác.
    2.  `saveAddress()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddAddressFragment.java`
        *   **Chức năng:** Xử lý logic kiểm tra thông tin và lưu địa chỉ vào Firestore.
        *   **Vai trò:** Đóng gói thông tin thành Model `Address` và thực thi ghi dữ liệu (đồng thời vô hiệu hóa cờ default của các địa ít cũ nếu địa chỉ mới là mặc định).
*   **Tệp tin xử lý chính:** `AddressFragment.java` & `AddAddressFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_address.xml`, `app/src/main/res/layout/fragment_add_address.xml`, `app/src/main/res/layout/item_address.xml`
    *   **Layout gốc (Root Layout):**
        - `fragment_address.xml` & `fragment_add_address.xml`: `ConstraintLayout` làm gốc (chứa `NestedScrollView` cuộn trang).
        - `item_address.xml` (tệp giao diện từng dòng địa chỉ): `MaterialCardView` làm gốc (thiết kế nổi khối và bo góc tròn).
    *   **Layout con & Cấu trúc:** `LinearLayout` (Vertical) làm xương sống cho form nhập liệu trong `fragment_add_address.xml`.
    *   **Thành phần UI sử dụng:** `RecyclerView` (danh sách địa chỉ), `Spinner` (tỉnh/thành, quận/huyện, xã/phường), `EditText` (nhập họ tên, SĐT, số nhà/tên đường), `SwitchCompat`/`CheckBox` (thiết lập mặc định), `MaterialButton` (Lưu địa chỉ).
    *   **Mô tả thiết kế:**
        - `fragment_address.xml` hiển thị danh sách các thẻ địa chỉ dạng CardView (`item_address.xml`) sử dụng `RecyclerView`.
        - `fragment_add_address.xml` sử dụng `LinearLayout` (Vertical) bọc trong `NestedScrollView` cho phép nhập liệu dễ dàng. Các `Spinner` hành chính được xếp động nối tiếp nhau theo cấp từ Tỉnh $\rightarrow$ Huyện $\rightarrow$ Xã.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `Address.java` và `Address.AddressLocation`: Ánh xạ cấu trúc dữ liệu địa chỉ. Đối tượng `AddressLocation` lưu chi tiết phân cấp hành chính (mã/tên tỉnh, quận, phường).
    *   **Sử dụng Adapter:**
        *   `AddressAdapter.java`: Được Fragment sử dụng để đổ dữ liệu từ Firestore vào RecyclerView hiển thị danh sách địa chỉ.
        *   **Cơ chế gọi Callback:** `AddressAdapter` định nghĩa Interface `OnAddressSelectedListener` (hoặc click callback). Khi người dùng nhấn vào một địa chỉ trong danh sách, Adapter sẽ bắt sự kiện click và gọi phương thức `onAddressSelected(Address address)` của Fragment để truyền ngược lại địa chỉ được chọn về màn hình Checkout.
*   **Mô tả hoạt động:**
    1.  `VietnamProvincesApiClient` gửi yêu cầu nạp dữ liệu Tỉnh/Thành phố lên Spinner. Khi người dùng chọn Tỉnh, Spinner Quận/Huyện sẽ được nạp tiếp theo cơ chế phân cấp dữ liệu hành chính.
    2.  Khi nhấn nút Lưu địa chỉ, hàm `saveAddress` khởi tạo đối tượng `Address` chứa thông tin từ các Spinner và trường nhập văn bản chi tiết.
    3.  Nếu địa chỉ là mặc định (`isDefault = true`), hệ thống dùng `WriteBatch` để cập nhật các địa chỉ cũ thành `isDefault = false`, ghi địa chỉ mới này và cập nhật `defaultAddressId` trong tài liệu `users/{userId}`.
*   **Lý thuyết liên quan:**
    *   **Firebase WriteBatch:** Thực thi nhiều lệnh ghi Firestore như một hoạt động đơn lẻ để duy trì tính nhất quán.

---

### 4. Quản lý thẻ thanh toán (Payment Method / New Card)

*   **Sự kiện UI (Click nút):** Người dùng điền đầy đủ thông tin thẻ và nhấn nút **"Add Card"** (`btnAddCard`).
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `saveCardToFirestore()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddCardFragment.java`
        *   **Chức năng:** Thu thập thông tin thẻ từ UI, kiểm tra tính hợp lệ của đầu vào đầu vào và kích hoạt quy trình mã hóa Stripe.
        *   **Vai trò:** Điểm khởi đầu kiểm soát dữ liệu trước khi gửi đi, đảm bảo định dạng số thẻ, mã bảo mật và ngày hết hạn hợp lệ.
    2.  `stripe.createCardToken(CardParams cardParams, ApiResultCallback<Token> callback)`
        *   **Vị trí file:** SDK của Stripe, được gọi tại `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddCardFragment.java`
        *   **Chức năng:** Gửi thông tin thẻ thô lên máy chủ Stripe trực tiếp từ client để đổi lấy token bảo mật.
        *   **Vai trò:** Bảo vệ thông tin nhạy cảm của khách hàng bằng cách mã hóa ở cấp độ thiết bị, không để lộ thông tin thẻ cho máy chủ của ứng dụng.
    3.  `stripeCardApiClient.createCardPaymentMethod(CreateCardPaymentMethodRequest request, Callback callback)`
        *   **Vị trí file:** Định nghĩa tại `app/src/main/java/com/example/tech_store_mobile/utils/StripeCardApiClient.java` (được gọi từ `AddCardFragment.java`)
        *   **Chức năng:** Gửi token Stripe vừa tạo lên backend để đăng ký và lưu phương thức thanh toán cho khách hàng trên Stripe Customer.
        *   **Vai trò:** Lưu giữ phương thức thanh toán an toàn để tái sử dụng cho các lần thanh toán sau mà không cần nhập lại thẻ.
    4.  `persistPaymentMethod(String userId, CreateCardPaymentMethodResponse response, ...)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddCardFragment.java`
        *   **Chức năng:** Lưu thông tin thẻ đã che mặt nạ (`last4`, `brand`, `expiryDate`) vào Firestore collection `payment_methods`.
        *   **Vai trò:** Giúp ứng dụng hiển thị danh sách các thẻ đã lưu của người dùng trên giao diện để họ chọn lựa thanh toán nhanh.
*   **Tệp tin xử lý chính:** `AddCardFragment.java` & `PaymentMethodFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_payment_method.xml`, `app/src/main/res/layout/fragment_add_card.xml`, `app/src/main/res/layout/item_payment_method.xml`
    *   **Layout gốc (Root Layout):**
        - `fragment_payment_method.xml` & `fragment_add_card.xml`: `ConstraintLayout` làm gốc.
        - `item_payment_method.xml` (từng dòng item thẻ): `RelativeLayout` làm gốc (cho phép chồng văn bản số thẻ, ngày hết hạn và logo hãng thẻ lên trên hình nền thẻ).
    *   **Layout con & Cấu trúc:** `LinearLayout` (Vertical) bọc trong `NestedScrollView` của `fragment_add_card.xml` giúp tổ chức form nhập liệu.
    *   **Thành phần UI sử dụng:** `RecyclerView` (danh sách thẻ đã lưu), `CardInputWidget` (hoặc các `EditText` cho số thẻ, hạn dùng, CVC từ Stripe SDK), `MaterialButton` (`btnAddCard` - thêm thẻ).
    *   **Mô tả thiết kế:**
        - `fragment_payment_method.xml` có cấu trúc danh sách cuộn dọc. Mỗi thẻ trong `item_payment_method.xml` được thiết kế giả lập hình ảnh chiếc thẻ tín dụng mini trực quan.
        - `fragment_add_card.xml` sử dụng các trường nhập liệu thẻ an toàn chuẩn Stripe, kết hợp `TextInputLayout` hiển thị gợi ý và thông báo lỗi.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `PaymentMethod.java`: Model lưu thông tin thẻ Stripe đã được mã hóa an toàn (gồm `paymentId`, `cardNumber` đã che, `expiryDate`, `cardType`, `cardHolderName`, `isDefault`).
        *   `CreateCardPaymentMethodRequest.java` & `CreateCardPaymentMethodResponse.java`: Gửi token thẻ lên API server và đón nhận thông tin phản hồi từ Server.
    *   **Sử dụng Adapter:**
        *   `PaymentMethodAdapter.java`: Liên kết hiển thị danh sách các thẻ tín dụng đã lưu của người dùng lên RecyclerView.
        *   **Cơ chế gọi Callback:** Adapter định nghĩa Interface `OnPaymentMethodSelectedListener`. Khi khách hàng nhấn chọn thẻ mong muốn trên danh sách, Adapter sẽ truyền Model `PaymentMethod` được click về Fragment để làm phương thức thanh toán chính.
*   **Mô tả hoạt động:**
    1.  `saveCardToFirestore` thu thập thông tin thẻ từ giao diện.
    2.  Gọi `stripe.createCardToken` đổi thông tin thẻ thô lấy mã Token an toàn đại diện (`tok_...`) từ Stripe server.
    3.  Gửi token này tới backend API `/api/payment-methods/create-card` (`createCardPaymentMethod`). Backend tiến hành tạo Payment Method chính thức.
    4.  Khi backend thành công, hàm `persistPaymentMethod` sẽ lưu đối tượng `PaymentMethod` chứa số thẻ đã che mặt nạ (`**** **** **** 4242`) vào Firestore collection `payment_methods` để hiển thị an toàn trên giao diện.

---

### 5. Màn hình Thành công (Success Dialog)

*   **Sự kiện UI (Kích hoạt tự động):** Gọi ngay khi các tác vụ thanh toán, lưu địa chỉ hoặc thêm thẻ thành công.
*   **Chi tiết các hàm được gọi & Vai trò:**
    *   `showSuccessDialog()` / `showPaymentSuccessDialog()`
        *   **Vị trí file:**
            *   `showPaymentSuccessDialog()`: `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CheckoutFragment.java`
            *   `showSuccessDialog()`: `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddAddressFragment.java` và `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/AddCardFragment.java`
        *   **Chức năng:** Khởi tạo và thiết lập thông số hiển thị cho popup Dialog thành công.
        *   **Vai trò:** Cung cấp phản hồi thị giác rõ ràng cho người dùng rằng hành động của họ đã được hệ thống lưu trữ thành công, đồng thời hướng dẫn bước điều hướng tiếp theo (Ví dụ: bấm để quay về trang chủ).
*   **Tệp tin xử lý chính:** `CheckoutFragment.java`, `AddAddressFragment.java`, `AddCardFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/dialog_success.xml`, `app/src/main/res/layout/dialog_failure.xml`
    *   **Layout gốc (Root Layout):** `ConstraintLayout` làm gốc (giúp căn giữa hộp thoại popup trên toàn màn hình).
    *   **Layout con & Cấu trúc:** `LinearLayout` (Vertical) lồng trong `ConstraintLayout` để xếp chồng các đối tượng thẳng hàng chính giữa.
    *   **Thành phần UI sử dụng:** `ImageView` (icon checkmark xanh hoặc icon cảnh báo đỏ), `TextView` (thông điệp thành công/thất bại chi tiết), `MaterialButton` (nút xác nhận/đóng).
    *   **Mô tả thiết kế:** Thiết kế popup dạng thẻ nổi (CardView) được bo tròn 4 góc, căn chỉnh nội dung vào giữa bằng `ConstraintLayout` để hiển thị trên nền mờ của màn hình gốc.
*   **Sự liên kết với Model và Adapter:**
    *   Màn hình này không sử dụng Adapter do nó là một `Dialog` đơn lẻ hiển thị thông báo trạng thái tĩnh, nhưng nó nhận các dữ liệu ID đơn hàng hoặc Địa chỉ từ Model vừa được lưu thành công để hiển thị lên thông điệp (ví dụ: hiển thị mã hóa đơn, mã đơn hàng).
*   **Mô tả hoạt động:**
    1.  Khởi tạo đối tượng `Dialog` tùy chỉnh sử dụng layout giao diện `R.layout.dialog_success`.
    2.  Cập nhật thông tin text tùy biến và thiết lập nút bấm hành động buộc người dùng phải xác nhận bấm nút đóng để chuyển hướng fragment hợp lệ.

---

## PHÂN HỆ 2: THEO DÕI ĐƠN HÀNG (TRACKING FLOW)

### 1. Danh sách đơn hàng (My Orders - Ongoing & Completed)

*   **Sự kiện UI (Mở màn hình):** Người dùng truy cập tab **"My Orders"** từ Fragment tài khoản cá nhân.
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `setupViewPager()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/MyOrdersFragment.java`
        *   **Chức năng:** Khởi tạo bộ chuyển đổi tab bằng ViewPager2.
        *   **Vai trò:** Quản lý vòng đời và chuyển đổi mượt mà giữa hai phân mảnh: Ongoing (Đang thực hiện) và Completed Orders (Đơn hàng đã hoàn thành).
    2.  `loadOrders()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/OrdersListFragment.java`
        *   **Chức năng:** Thực hiện truy vấn danh sách đơn hàng từ Firestore.
        *   **Vai trò:** Tải toàn bộ đơn hàng của user hiện tại, tiến hành lọc phân loại theo thuộc tính `status` và sắp xếp theo ngày đặt giảm dần để hiển thị lên RecyclerView.
*   **Tệp tin xử lý chính:** `MyOrdersFragment.java` & `OrdersListFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_my_orders.xml`, `app/src/main/res/layout/fragment_orders_list.xml`, `app/src/main/res/layout/item_order.xml`
    *   **Layout gốc (Root Layout):**
        - `fragment_my_orders.xml`: `LinearLayout` (Vertical) làm gốc (giúp định vị Toolbar $\rightarrow$ TabLayout $\rightarrow$ ViewPager2 tuần tự hoàn hảo).
        - `fragment_orders_list.xml`: `ConstraintLayout` làm gốc (chứa `RecyclerView` toàn màn hình).
        - `item_order.xml`: `MaterialCardView` làm gốc (bo góc tròn 18dp, viền mờ `#EDEDED`).
    *   **Layout con & Cấu trúc:** `LinearLayout` ngang/dọc kết hợp lồng ghép bên trong `item_order.xml` để bố trí hình ảnh sản phẩm và nút hành động.
    *   **Thành phần UI sử dụng:** `TabLayout` (chuyển tab Ongoing/Completed), `ViewPager2` (chứa các trang danh sách), `RecyclerView` (danh sách đơn hàng), `TextView` (mã đơn, ngày đặt, trạng thái, tổng tiền), `ImageView` (ảnh sản phẩm đầu tiên), `MaterialButton` (`btnTrackOrder`, `btnLeaveReview` - hành động).
    *   **Mô tả thiết kế:**
        - `fragment_my_orders.xml` sử dụng `TabLayout` nằm ngay dưới Toolbar hành trình, kết hợp với `ViewPager2` chiếm toàn bộ phần thân để trượt ngang chuyển tab.
        - `item_order.xml` được bao bọc trong CardView hiện đại, bố trí ảnh sản phẩm bên trái, thông tin chi tiết (mã, giá, ngày đặt) ở giữa, và các nút bấm hành động tùy biến theo trạng thái (Track Order hoặc Leave Review) được xếp gọn góc dưới bên phải.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `Order.java` & `OrderItem.java`: Ứng dụng đọc dữ liệu thô từ collection `orders` trên Firestore và ánh xạ trực tiếp thành các đối tượng `Order.class`.
    *   **Sử dụng Adapter:**
        *   `OrderAdapter.java`: Bộ điều phối danh sách đơn hàng. Do mỗi đơn hàng (`Order`) có thể chứa nhiều sản phẩm (`OrderItem`), nên Adapter sử dụng một lớp gom nhóm dữ liệu hiển thị `OrderAdapter.DisplayItem` (chứa 1 `Order` cha và 1 `OrderItem` con).
        *   **Quy trình phẳng hóa (Flat-mapping):** `OrdersListFragment` sẽ duyệt qua danh sách các đơn hàng thỏa mãn điều kiện lọc, bóc tách từng mặt hàng con và thêm các cặp `new DisplayItem(order, item)` vào mảng dữ liệu. Sau đó gọi `adapter.notifyDataSetChanged()` để hiển thị từng mặt hàng độc lập lên RecyclerView.
        *   **Cơ chế Callback:** `OrderAdapter` định nghĩa Interface `OnOrderActionListener` có 2 phương thức: `onTrackOrder(Order order)` và `onLeaveReview(Order order, OrderItem item)`. Khi click vào các nút tương ứng trên item, Adapter sẽ gọi các hàm callback này trên Fragment được triển khai để mở tiếp màn hình theo dõi hành trình hoặc viết đánh giá.
*   **Mô tả hoạt động:**
    1.  `MyOrdersFragment` dùng `ViewPager2` để chuyển đổi giữa hai Fragment con `OrdersListFragment` (một cho đơn hàng Ongoing, một cho Completed).
    2.  `OrdersListFragment` truy vấn danh sách đơn hàng có `userId` khớp với tài khoản hiện tại, sắp xếp theo thời gian mới nhất.
    3.  Lọc trạng thái: Đơn hàng hoàn thành (`status = "Completed"`) đưa vào tab Completed, các trạng thái khác đưa vào tab Ongoing.

---

### 2. Theo dõi hành trình đơn hàng (Track Order)

*   **Sự kiện UI (Click nút):** Người dùng nhấn nút **"Track Order"** trên thẻ đơn hàng.
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `loadOrderData()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/TrackOrderFragment.java`
        *   **Chức năng:** Tải thông tin chi tiết của đơn hàng cụ thể từ Firestore theo `orderId`.
        *   **Vai trò:** Cung cấp nguồn dữ liệu để hiển thị thông tin người nhận, địa chỉ giao hàng và trạng thái hiện tại trên UI.
    2.  `updateTimeline(String currentStatus)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/TrackOrderFragment.java`
        *   **Chức năng:** Cập nhật trạng thái hiển thị của các bước trên timeline dựa trên trạng thái của đơn hàng.
        *   **Vai trò:** Điều khiển giao diện timeline (đường nối, màu sắc nút chấm tròn) phản ánh chính xác tiến độ vận chuyển thực tế (Packing $\rightarrow$ Picked $\rightarrow$ In Transit $\rightarrow$ Delivered $\rightarrow$ Completed).
*   **Tệp tin xử lý chính:** `OrdersListFragment.java` & `TrackOrderFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_track_order.xml`, `app/src/main/res/layout/item_tracking_step.xml`
    *   **Layout gốc (Root Layout):**
        - `fragment_track_order.xml`: `ConstraintLayout` làm gốc (neo thông tin vận chuyển và RecyclerView).
        - `item_tracking_step.xml` (từng dòng bước timeline): `ConstraintLayout` làm gốc (rất quan trọng để căn giữa tuyệt đối cho chấm tròn trạng thái và đường thẳng nối timeline).
    *   **Layout con & Cấu trúc:** `ConstraintLayout` giúp cố định đường line đứng kết nối giữa các bước.
    *   **Thành phần UI sử dụng:** `RecyclerView` (vẽ timeline đứng), `TextView` (thông tin người giao, vận đơn, mô tả trạng thái bước), `ImageView` (icon trạng thái bước), `View` (đường kẻ đứng kết nối các bước timeline).
    *   **Mô tả thiết kế:**
        - `fragment_track_order.xml` sử dụng `ConstraintLayout` chia 2 khối: Khối trên tóm tắt vận đơn/thông tin shipper, khối dưới là `RecyclerView` chứa timeline.
        - `item_tracking_step.xml` thiết kế đặc biệt gồm 1 chấm tròn và 1 đường kẻ thẳng đứng (`View` chiều rộng 2dp). Trạng thái của bước (chưa tới/đang tới/đã qua) sẽ quyết định màu sắc chấm tròn và đường kẻ (màu xám nhạt hoặc màu đen/xanh).
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `Order.java` & `ShippingAddressSnapshot.java`: Cung cấp thông tin tiến trình (`status`), người nhận, số điện thoại và địa chỉ giao hàng Snapshot của đơn hàng đó.
    *   **Sử dụng Adapter:**
        *   `TrackingAdapter` (Lớp Adapter nội bộ trong `TrackOrderFragment.java`): Có vai trò hiển thị sơ đồ tiến trình 5 bước.
        *   Lớp Model nội bộ `TrackingStep`: Lưu trạng thái bước (ví dụ: "In Transit"), trạng thái đã đạt tới chưa (`isReached`), và bước này có phải là bước hiện tại không (`isCurrent`). Adapter liên kết danh sách `TrackingStep` này hiển thị lên RecyclerView kết nối dạng dòng thời gian (Timeline).

---

## PHÂN HỆ 3: ĐÁNH GIÁ SẢN PHẨM (PRODUCT REVIEW FLOW)

*   **Sự kiện UI (Click nút):** Người dùng nhấn nút **"Leave Review"** trên thẻ sản phẩm của đơn hàng đã hoàn thành.
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `onLeaveReview(Order order, OrderItem item)`
        *   **Vị trí file:** Được khai báo trong Interface `OnOrderActionListener` tại `app/src/main/java/com/example/tech_store_mobile/adapters/OrderAdapter.java` và triển khai tại `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/OrdersListFragment.java`
        *   **Chức năng:** Kiểm tra trạng thái đánh giá của sản phẩm trong đơn hàng.
        *   **Vai trò:** Quyết định xem sẽ mở BottomSheet viết đánh giá mới (nếu chưa đánh giá) hay mở màn hình xem danh sách các đánh giá cũ của sản phẩm đó.
    2.  `submitReview()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/LeaveReviewBottomSheet.java`
        *   **Chức năng:** Kiểm tra dữ liệu đầu vào của đánh giá (số sao, comment) và chuẩn bị ghi.
        *   **Vai trò:** Đảm bảo khách hàng phải chọn ít nhất 1 sao trước khi gửi đánh giá lên hệ thống.
    3.  `btnSubmitReview(String userId, float rating, String comment)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/LeaveReviewBottomSheet.java`
        *   **Chức năng:** Thực thi Firestore Transaction cập nhật cơ sở dữ liệu.
        *   **Vai trò:** Đóng gói Model `Review`, gọi Firestore Transaction tính điểm trung bình sản phẩm và cập nhật trạng thái đơn hàng.
*   **Tệp tin xử lý chính:** `OrdersListFragment.java` & `LeaveReviewBottomSheet.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/layout_leave_review_bottom_sheet.xml`, `app/src/main/res/layout/fragment_review.xml`, `app/src/main/res/layout/item_review.xml`
    *   **Layout gốc (Root Layout):**
        - `layout_leave_review_bottom_sheet.xml`: `LinearLayout` (Vertical) làm gốc để nội dung của BottomSheet được hiển thị tuần tự xếp chồng.
        - `fragment_review.xml`: `ConstraintLayout` làm gốc.
        - `item_review.xml` (từng bình luận đánh giá): `ConstraintLayout` làm gốc để dễ định vị avatar góc trái, rating bar sao nhỏ và nội dung văn bản bên dưới.
    *   **Layout con & Cấu trúc:** Lồng các `LinearLayout` ngang để hiển thị điểm số và sao.
    *   **Thành phần UI sử dụng:** `RatingBar` (chọn sao đánh giá), `EditText` (nhập bình luận), `TextView` (tên sản phẩm), `ImageView` (icon close), `MaterialButton` (nút Submit review), `RecyclerView` (danh sách review trong `fragment_review.xml`).
    *   **Mô tả thiết kế:**
        - `layout_leave_review_bottom_sheet.xml` kế thừa từ `BottomSheetDialogFragment`, trượt nhẹ lên từ dưới đáy màn hình với các góc trên được bo tròn mềm mại.
        - Bố cục sử dụng `LinearLayout` (Vertical) sắp xếp lần lượt: Tiêu đề $\rightarrow$ RatingBar trung tâm dạng sao lớn $\rightarrow$ Khung EditText nhập ý kiến có giới hạn dòng $\rightarrow$ Nút gửi đánh giá rộng ngang màn hình ở chân trang.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `Review.java`: Model lưu thông tin đánh giá được khởi tạo bao gồm `reviewId`, `productId`, `userId`, `userName`, `rating`, `comment`, và thời gian tạo.
        *   `Product.java`: Được sử dụng để đọc điểm số trung bình hiện tại của sản phẩm trước khi cập nhật điểm mới.
    *   **Sử dụng Adapter:**
        *   `ReviewAdapter.java`: Dùng trong màn hình chi tiết sản phẩm hoặc xem danh sách đánh giá (`ReviewFragment`). Sau khi lưu đánh giá thành công, Adapter này sẽ tải dữ liệu mới từ Firestore để hiển thị danh sách bình luận kèm số sao lên RecyclerView.

---

## PHÂN HỆ 4: CHAT HỖ TRỢ TRỰC TYẾN REAL-TIME

*   **Sự kiện UI (Mở màn hình & Nhập tin nhắn):** Người dùng truy cập phân hệ CSKH, nhập tin nhắn văn bản và nhấn nút gửi (`btnAction`).
*   **Chi tiết các hàm được gọi & Vai trò:**
    1.  `listenForMessages()`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CustomerServiceFragment.java`
        *   **Chức năng:** Đăng ký Snapshot Listener lắng nghe các tin nhắn trong phòng chat cụ thể.
        *   **Vai trò:** Duy trì kết nối realtime để nhận tin nhắn mới từ Admin ngay lập tức và tự động xóa số tin nhắn chưa đọc của User.
    2.  `sendMessage(String content)`
        *   **Vị trí file:** `app/src/main/java/com/example/tech_store_mobile/ui/fragments/main/CustomerServiceFragment.java`
        *   **Chức năng:** Thực hiện gửi tin nhắn lên Firestore.
        *   **Vai trò:** Dùng WriteBatch tạo tin nhắn mới và cập nhật thông tin tổng hợp của phòng chat (tin nhắn cuối, thời gian cập nhật, tăng bộ đếm tin chưa đọc của Admin).
*   **Tệp tin xử lý chính:** `CustomerServiceFragment.java`
*   **Thiết kế giao diện (Layout XML & UI Components):**
    *   **Tệp tin layout liên quan:** `app/src/main/res/layout/fragment_customer_service_chat.xml`
    *   **Layout gốc (Root Layout):** `ConstraintLayout` làm gốc (giúp ghim chặt Toolbar ở đầu trang, InputBar ở đáy trang và vùng chat co giãn linh hoạt ở giữa).
    *   **Layout con & Cấu trúc:**
        - Vùng hiển thị tin nhắn: `NestedScrollView` chứa `LinearLayout` xếp dọc (`chatMessagesContainer`) có thuộc tính `animateLayoutChanges="true"` để chạy animation khi thêm tin nhắn mới.
        - Thanh soạn thảo dưới đáy: `LinearLayout` ngang chứa `EditText` và `ImageButton` gửi.
    *   **Thành phần UI sử dụng:** `NestedScrollView` / `ScrollView` (`chatScroll` - cuộn tin nhắn), `LinearLayout` (`chatMessagesContainer` - chứa động các TextView bong bóng tin nhắn), `EditText` (`edtChatMessage` - khung nhập liệu), `ImageButton` (`btnSendVoice` / `btnAction` - gửi hoặc ghi âm).
    *   **Mô tả thiết kế:** Bố cục chia thành 3 phần cố định bằng `ConstraintLayout`:
        - Toolbar (Top): Chứa nút quay lại và tiêu đề hỗ trợ.
        - Vùng tin nhắn (Middle): Một `NestedScrollView` chiếm trọn vùng giữa, chứa container `LinearLayout` có thuộc tính `animateLayoutChanges="true"` giúp tin nhắn mới trồi lên mượt mà. Bong bóng chat được tạo động: Chat của tôi là `TextView` nền đen, chữ trắng nằm góc phải (`bg_button_black`); chat của đối phương là `TextView` nền xám nhạt, chữ đen nằm góc trái (`bg_search_bar`).
        - Thanh nhập liệu (Bottom): Một `LinearLayout` ngang chứa `EditText` bo góc tròn và nút gửi/mic dạng tròn đặt bên phải.
*   **Sự liên kết với Model và Adapter:**
    *   **Sử dụng Model:**
        *   `ChatMessage.java`: Ánh xạ cấu trúc tin nhắn gồm `senderId`, `receiverId`, `content`, `type`, `fileUrl`, và `createdAt`.
        *   `ChatRoom.java`: Ánh xạ thông tin phòng chat ngoài cùng, gồm ID phòng (UID người dùng), tin nhắn cuối cùng để hiển thị bản xem trước, và bộ đếm tin nhắn chưa đọc của Admin/User.
    *   **Không sử dụng Adapter truyền thống (Tối ưu hóa UI):**
        *   Khác với các danh sách dài, ở phần giao diện Chat trực tuyến này, `CustomerServiceFragment` sinh các bóng tin nhắn (Message Bubble) hoàn toàn động bằng cách lập trình (Programmatic View Generation) thông qua hàm `appendMessageBubble(...)` và nạp trực tiếp vào `LinearLayout` (`chatMessagesContainer`) được bọc trong một `ScrollView`.
        *   **Lý tạo thiết kế:** Giúp giao diện chat cập nhật tức thì khi Listener của Firestore bắt được thay đổi, giảm thiểu độ trễ dựng hình và loại bỏ việc khởi tạo ViewHolder phức tạp khi số lượng tin nhắn trong phiên chat ngắn.
*   **Mô tả hoạt động:**
    1.  `listenForMessages` đăng ký `addSnapshotListener` lắng nghe trực tiếp trên Firestore: `rooms/{userId}/messages` sắp xếp tăng dần theo thời gian gửi.
    2.  Khi có tin nhắn mới, Listener tự động cập nhật, xóa các view cũ và dựng lại các bóng bong bóng chat tương ứng với dữ liệu `ChatMessage` mới nhất.
    3.  Khi gửi tin nhắn, hệ thống dùng `WriteBatch` ghi đồng thời tài liệu `ChatMessage` mới vào Firestore và cập nhật thông tin tổng hợp của `ChatRoom` cha.
