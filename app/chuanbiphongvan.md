# KHUNG SƯỜN CHUẨN BỊ PHỎNG VẤN: TECH STORE MANAGEMENT SYSTEM (ANDROID & WEB ADMIN)

---

## 📌 PHẦN 1: TỔNG QUAN DỰ ÁN (OVERVIEW)

* **Tên dự án:** Tech Store Management System (Hệ thống quản lý cửa hàng công nghệ - Android & Web Admin).
* **Mục tiêu cốt lõi:** Xây dựng giải pháp bán hàng đa nền tảng toàn diện. Cho phép người dùng trải nghiệm mua sắm thiết bị công nghệ mượt mà trên ứng dụng di động Android (Java) và hỗ trợ quản trị viên (Admin) quản lý tập trung toàn bộ kho hàng, đơn hàng, phản hồi khách hàng thông qua trang Web Admin (Next.js).
* **Mô hình kiến trúc:** Đa nền tảng (Cross-platform client) kết hợp Cloud Services làm Backend-as-a-Service (BaaS).
  * *Ứng dụng Native Android (Java) cho người dùng tương tác với Web Admin (Next.js) thông qua cơ sở dữ liệu Firebase như thế nào?*
    Cả hai nền tảng (Android App và Next.js Web Admin) đều kết nối trực tiếp đến một project Firebase chung. Khi người dùng thực hiện các hành động (như đặt hàng, gửi tin nhắn, cập nhật thông tin cá nhân), dữ liệu sẽ được ghi trực tiếp vào Cloud Firestore. Phía Web Admin sử dụng SDK Firebase cho Web để lắng nghe thay đổi và cập nhật trạng thái đơn hàng, kho hàng tức thì. Ngược lại, khi Admin cập nhật trạng thái đơn hàng hoặc trả lời chat, dữ liệu sẽ được đồng bộ ngay lập tức về Android App thông qua kết nối thời gian thực của Firestore.
  * *Làm sao để đảm bảo dữ liệu tồn kho luôn được đồng bộ theo thời gian thực giữa hai nền tảng?*
    Sử dụng cơ chế Real-time Listeners của Firestore (`addSnapshotListener` trên Android và `onSnapshot` trên Web). Bất kỳ thay đổi nào về số lượng tồn kho (`stock`) của sản phẩm trong collection `products` (do khách hàng mua hàng hoặc admin nhập hàng thêm) sẽ kích hoạt một cập nhật thời gian thực tức thì. Cả giao diện Web Admin (biểu đồ thống kê, bảng kho hàng) và Android App (chi tiết sản phẩm, nút Mua hàng) đều tự động cập nhật số lượng khả dụng mới nhất mà không cần tải lại trang/ứng dụng.

---

## 📌 PHẦN 2: CHI TIẾT TECH STACK (LÝ DO LỰA CHỌN CÔNG NGHỆ)

* **Mobile User (Java - Android Studio):** 
  Lựa chọn phát triển ứng dụng Native Java thay vì Cross-platform (Flutter/React Native) nhằm tối ưu hóa hiệu năng tối đa của thiết bị di động, đảm bảo độ mượt mà cao nhất của giao diện Material Design 3 và khả năng tương thích 100% với các dịch vụ Google (Google Play Services, Firebase SDK, Google Sign-In) mà không cần qua các lớp bridge trung gian (giảm thiểu rủi ro lỗi crash và tối ưu hóa bộ nhớ RAM). Native phát triển cũng giúp việc tích hợp trực tiếp Stripe Android SDK dễ dàng, trực quan hơn.
* **Web Admin (Next.js & Tailwind CSS):** 
  Next.js mang lại giải pháp xây dựng trang quản trị mạnh mẽ với cấu trúc App Router hiện đại, hỗ trợ Server-Side Rendering (SSR) giúp tải nhanh chóng các bảng dữ liệu báo cáo doanh thu và biểu đồ phân tích lớn từ phía server trước khi trả về client. Tailwind CSS giúp thiết kế giao diện dashboard đáp ứng (responsive), trực quan và hiện đại cực nhanh. Sự kết hợp này mang lại trải nghiệm mượt mà cho Admin khi quản lý sản phẩm và đơn hàng trên mọi kích thước màn hình.
* **Cloud Services (Firebase Suite):** 
  Sử dụng hệ sinh thái Firebase (Firestore, Authentication, Messaging, Storage) giúp giảm thiểu chi phí và thời gian tự vận hành một hệ thống server backend truyền thống. Firestore hỗ trợ đồng bộ dữ liệu thời gian thực vượt trội bằng WebSockets; Firebase Authentication xử lý an toàn luồng đăng nhập Email & Google Sign-In; Cloud Messaging (FCM) cung cấp giải pháp đẩy thông báo cự ly ngắn tin cậy; và Firebase Storage tối ưu hóa lưu trữ hình ảnh sản phẩm chất lượng cao với CDN toàn cầu.

---

## 📌 PHẦN 3: ĐỘT PHÁ KỸ THUẬT & LUỒNG NGHIỆP VỤ CHUYÊN SÂU

### 1. Luồng Đăng nhập & Xác thực Người dùng (Authentication Flow)

Phần chứng minh tư duy thiết kế hệ thống bảo mật và đồng bộ hồ sơ người dùng:

* **Luồng đăng nhập kép (Dual Auth Flow):** 
  Hệ thống hỗ trợ cả đăng nhập bằng Email/Mật khẩu truyền thống và Đăng nhập nhanh qua Google (Google Sign-In). 
  - Với Email/Password: Ứng dụng thực hiện validate dữ liệu đầu vào theo thời gian thực (định dạng Gmail thông qua `addTextChangedListener` và Patterns). Nếu đúng định dạng, gọi `mAuth.signInWithEmailAndPassword()`.
  - Với Google Sign-In: Ứng dụng sử dụng `GoogleSignInClient` để xác thực người dùng và nhận về một ID Token. ID Token này sau đó được chuyển đến Firebase Auth thông qua `GoogleAuthProvider.getCredential()` để tạo phiên đăng nhập an toàn.
* **Đồng bộ hồ sơ thông minh (Smart Profile Sync):** 
  Để đảm bảo dữ liệu cá nhân nhất quán, lớp trợ giúp `UserProfileSyncHelper` sẽ được gọi ngay sau khi đăng nhập thành công. Phương thức `syncCompleteUserProfile()` sẽ đọc thông tin cũ của người dùng trong Firestore và thực hiện merge thông tin mới thông qua `SetOptions.merge()`. Việc này giúp giữ lại các thông tin tùy biến do người dùng cập nhật trước đó (như số điện thoại, ngày sinh, giới tính, địa chỉ mặc định) mà không lo bị ghi đè bởi thông tin cơ bản từ tài khoản Google/Email.
* **Quản lý Token thông báo đẩy (FCM Token Lifecycle):** 
  Để gửi thông báo đẩy chính xác:
  - Khi đăng nhập thành công, ứng dụng gọi `FcmTokenSyncHelper.syncCurrentTokenIfLoggedIn()` để lấy Token thiết bị hiện tại từ `FirebaseMessaging.getInstance().getToken()` và lưu vào tài liệu người dùng (`users/{userId}/fcmToken`).
  - Khi người dùng đăng xuất (Logout), hàm `deactivateCurrentToken()` sẽ được gọi để thu hồi/xóa token này trên thiết bị bằng `deleteToken()`, đồng thời dùng `FieldValue.delete()` để xóa trường `fcmToken` khỏi Firestore. Điều này ngăn chặn việc thiết bị tiếp tục nhận thông báo của người dùng cũ, đảm bảo tính riêng tư của dữ liệu khách hàng.

### 2. Tích hợp thanh toán trực tuyến qua Stripe API

Đây là phần thể hiện tư duy xử lý giao dịch tài chính an toàn:

* **Luồng thanh toán an toàn (Payment Flow):**
  1. Người dùng tiến hành thanh toán từ màn hình `CheckoutFragment`. Một ID đơn hàng tạm thời được tạo (`orderId = "CHECKOUT-" + timestamp`).
  2. Ứng dụng Android gửi yêu cầu tạo PaymentIntent đến Node.js backend thông qua API `POST /api/payments/create-payment-intent`, truyền kèm thông tin: `userId`, `orderId`, `totalAmount`, `currency` (USD), và `paymentMethodId` (ID thẻ đã lưu trên Stripe có dạng `pm_xxx`).
  3. Node.js backend sử dụng `Stripe Secret Key` để xác thực với Stripe API, lấy thông tin khách hàng và tạo một yêu cầu xác nhận thanh toán tự động với Stripe qua tham số `confirm: true`.
  4. Stripe xử lý giao dịch và trả về trạng thái thanh toán cùng `clientSecret` cho backend. Backend chuyển kết quả này về ứng dụng di động.
  5. Nếu trạng thái trả về là `succeeded`, ứng dụng Android sử dụng `WriteBatch` của Firestore để thực hiện cập nhật đồng thời (Atomic Write): Lưu đơn hàng mới (`orders`), tạo hóa đơn điện tử (`hoa_dons`), ghi nhận lịch sử giao dịch (`lich_su_thanh_toans`), và xóa sạch các sản phẩm đã mua khỏi giỏ hàng của người dùng (`carts`).
* **Xử lý bảo mật thông tin thẻ (PCI-DSS Compliance):** 
  Ứng dụng tuân thủ nghiêm ngặt tiêu chuẩn bảo mật PCI-DSS. Chúng tôi tuyệt đối không lưu trữ các thông tin thẻ thô nhạy cảm (như Card Number, CVV, expiry date) trên cơ sở dữ liệu Firestore hay server backend. Khi người dùng thêm thẻ mới, Stripe SDK trên ứng dụng sẽ mã hóa trực tiếp thông tin thẻ và gửi lên Stripe Server để nhận lại một token thẻ hoặc Payment Method ID (`pm_xxx`). Hệ thống chỉ lưu trữ và xử lý ID an toàn này dưới DB. Ngoài ra, khóa bí mật `Stripe Secret Key` được bảo mật tuyệt đối tại server backend và không bao giờ xuất hiện ở mã nguồn ứng dụng client.
* **Xử lý sự cố (Edge Cases - Khả năng chịu lỗi):** 
  Nếu người dùng đã bị trừ tiền phía Stripe nhưng ứng dụng Android đột ngột bị sập nguồn hoặc mất mạng trước khi kịp thực hiện `WriteBatch` lưu đơn hàng vào Firestore, hệ thống sẽ xử lý qua hai cơ chế phòng vệ:
  - *Stripe Webhooks:* Một endpoint webhook trên Node.js backend được cấu hình để lắng nghe sự kiện `payment_intent.succeeded` phát đi từ Stripe Server. Khi nhận được sự kiện thành công, backend sẽ tự động kiểm tra xem đơn hàng tương ứng đã được ghi nhận trong Firestore chưa. Nếu chưa, backend sử dụng quyền admin để tạo đơn hàng và cập nhật trạng thái "Đã thanh toán", đảm bảo quyền lợi khách hàng không bao giờ bị ảnh hưởng.
  - *Pre-creation Order Status:* Trước khi gọi Stripe, ứng dụng có thể tạo một tài liệu đơn hàng ở trạng thái "Chờ thanh toán" (Pending Payment). Nếu giao dịch thành công, cập nhật đơn hàng thành "Đã thanh toán". Nếu sập nguồn, hệ thống có thể đối chiếu mã `paymentIntentId` của đơn hàng Pending đó với Stripe để tự động hoàn thành đơn hàng khi kết nối lại hoặc thông qua batch cron job.

### 3. Hệ thống Real-time Chat (Firebase Firestore & FCM)

Phần chứng minh khả năng tối ưu trải nghiệm tương tác trực tuyến:

* **Cấu trúc dữ liệu phòng chat (Data Modeling dưới Firestore):** 
  Để tối ưu hóa chi phí đọc/ghi và tránh tải thừa dữ liệu, cấu trúc DB được thiết kế như sau:
  - Collection cha `rooms`: Mỗi tài liệu (Document) đại diện cho một kênh chat giữa một Khách hàng và Admin, với Document ID chính là `userId` của Khách hàng. Thiết kế này giúp Admin dễ dàng truy vấn danh sách phòng chat mà không bị lặp. Các trường thông tin gồm: `userId`, `userName`, `lastMessage`, `updatedAt`, `adminUnreadCount`, `userUnreadCount`.
  - Subcollection `messages` (nằm trong mỗi document room: `rooms/{userId}/messages`): Lưu trữ danh sách tin nhắn chi tiết. Mỗi tin nhắn gồm các trường: `senderId` (UID người dùng hoặc `"admin"`), `receiverId`, `content`, `type` (text), và `createdAt` (timestamp).
  - Ưu điểm: Khi Admin tải danh sách phòng chat ở màn hình tổng quan, Firestore chỉ cần đọc các document nhỏ trong collection `rooms` để lấy tin nhắn cuối và số lượng tin nhắn chưa đọc mà không cần quét toàn bộ hàng nghìn tin nhắn con bên trong. Điều này giúp giảm số lượng Read Firestore cực kỳ lớn.
* **Cơ chế Real-time lắng nghe:** 
  Ứng dụng Android sử dụng phương thức `addSnapshotListener()` của Firestore, lắng nghe trực tiếp subcollection `messages` sắp xếp theo `createdAt` tăng dần. Web Admin cũng sử dụng cơ chế lắng nghe tương ứng `onSnapshot()`. Nhờ kết nối Socket hai chiều của Firestore duy trì liên tục, bất kỳ tin nhắn mới nào được ghi vào DB sẽ lập tức được đẩy về giao diện của cả hai bên mà không cần thực hiện bất kỳ lệnh reload hay request HTTP kéo (pull) nào.
* **Cơ chế thông báo đẩy (Push Notifications) qua FCM:** 
  Khi Admin phản hồi trên Web Admin và người dùng đang không mở ứng dụng chat (hoặc khóa màn hình):
  - Sự kiện thêm tin nhắn mới vào Firestore sẽ kích hoạt một backend service (hoặc Firebase Cloud Function).
  - Hệ thống lấy FCM token (`fcmToken`) đã được đồng bộ trong hồ sơ người dùng `users/{userId}`.
  - Backend gửi một payload thông báo đến Firebase Cloud Messaging (FCM) API.
  - FCM Server sẽ đẩy thông báo này đến thiết bị Android thông qua kênh Google Play Services chạy ngầm, hiển thị một thông báo nổi (Notification) trực quan trên thanh trạng thái giúp người dùng lập tức biết có tin nhắn mới từ Admin.

### 4. Đồng bộ hóa tồn kho & Quản lý đơn hàng (Inventory Control)

* **Luồng cập nhật tồn kho:**
  Khi một đơn hàng được thanh toán thành công, số lượng sản phẩm mua sẽ được khấu trừ trực tiếp khỏi trường `stockQuantity` (tồn kho) của từng sản phẩm trong collection `products`. Web Admin sử dụng `onSnapshot()` để lắng nghe sự thay đổi của collection `products`, từ đó tự động vẽ lại biểu đồ thống kê kho hàng và báo cáo doanh thu theo thời gian thực mà không cần quản trị viên F5 trình duyệt.
* **Bài toán tranh chấp hàng tồn kho & Thanh toán đồng thời (Stripe Concurrency & Overselling):**
  *Bài toán:* Nếu 2 user cùng bấm mua sản phẩm cuối cùng (stock = 1) và cả hai đều thanh toán thành công qua Stripe trước khi DB kịp cập nhật, hệ thống xử lý thế nào?
  Hệ thống kết hợp **Firestore Transactions (`db.runTransaction()`)** và cơ chế **Stripe Auto-Refund (Hoàn tiền tự động)** để xử lý triệt để:
  
  1. **Bước 1: Thực hiện giao dịch an toàn (Atomic Transaction) khi tạo đơn:**
     Sau khi nhận phản hồi Stripe thành công, app chạy một Firestore Transaction để lưu đơn hàng và cập nhật tồn kho:
     * Đọc `stockQuantity` hiện tại từ Firestore Server.
     * Kiểm tra điều kiện:
       - Nếu `stockQuantity >= quantity_khách_đặt`: Tiến hành cập nhật giảm kho (`stockQuantity = stockQuantity - quantity`) và ghi nhận đơn hàng (`orders`), hóa đơn (`hoa_dons`) trong cùng một Transaction. Giao dịch thành công hoàn toàn.
       - Nếu `stockQuantity < quantity_khách_đặt` (do user kia đã nhanh tay hoàn tất Transaction trước làm stock về 0): Transaction sẽ **chủ động hủy bỏ (Abort)**. Đơn hàng của user thứ 2 sẽ không được tạo trên DB.
  
  2. **Bước 2: Cơ chế Tự động hoàn tiền (Auto-Refund) qua Webhook/Backend:**
     * Khi Transaction của user thứ 2 bị Abort do hết hàng, client app (hoặc Firebase Cloud Function lắng nghe lỗi) sẽ gửi yêu cầu hoàn tiền đến Node.js backend, hoặc Stripe Webhook sẽ phát hiện giao dịch thành công nhưng không có đơn hàng tương ứng được tạo trong Firestore sau một khoảng thời gian chờ (timeout).
     * Backend gọi API hoàn tiền của Stripe:
       ```javascript
       const refund = await stripe.refunds.create({
         payment_intent: paymentIntentId,
         reason: 'requested_by_customer', // Hoặc lỗi hệ thống hết hàng
       });
       ```
     * Hệ thống tự động chuyển trạng thái giao dịch trong `lich_su_thanh_toans` thành `Refunded` và gửi thông báo (FCM) thông tin hoàn tiền cho user thứ 2: *"Rất tiếc, sản phẩm đã hết hàng vào phút chót. Giao dịch của bạn đã được hoàn tiền tự động."*

  3. **Giải pháp tối ưu hơn (Stock Reservation - Giữ chỗ tồn kho trước) - Đã hiện thực hóa:**
     Để tránh việc trừ tiền Stripe rồi mới hoàn lại (tạo trải nghiệm khách hàng tệ do mất phí/thời gian chờ hoàn trả), hệ thống áp dụng cơ chế **Giữ chỗ tạm thời (Temporary Hold) trong 5 phút**:
     * **Cơ chế Client-side (Android App):** Khi người dùng nhấn nút "Thanh toán", thay vì trực tiếp gọi Stripe Payment Intent ngay, app thực hiện một Firestore Transaction:
       - Đọc số lượng tồn kho thực tế (`stockQuantity`) của từng mặt hàng.
       - Kiểm tra nếu bất kỳ sản phẩm nào không đủ tồn kho, Transaction sẽ bị huỷ (`Abort`) và hiển thị thông báo lỗi cụ thể cho người dùng.
       - Nếu đủ hàng, Transaction sẽ đồng thời giảm tạm thời `stockQuantity` trong collection `products` và tạo một tài liệu giữ chỗ trong collection `stock_reservations` với trạng thái `pending` và `expiresAt = now + 5 phút`.
       - Tiến hành gọi Stripe Payment Flow. Nếu thanh toán thành công, trong Firestore WriteBatch lưu đơn hàng, hệ thống cập nhật trạng thái giữ chỗ thành `completed`.
       - Nếu thanh toán thất bại, bị huỷ bởi người dùng, hoặc khi người dùng quay lại/thoát màn hình Checkout (`onDestroyView`), app thực hiện giải phóng tồn kho ngay lập tức (`releaseReservationImmediately` qua Firestore Transaction) để trả lại hàng vào kho mà không phải đợi hết 5 phút.
     * **Cơ chế Backend-side (Node.js Express Server):** Để đề phòng trường hợp client bị sập nguồn, mất mạng đột ngột hoặc tắt ứng dụng giữa chừng khiến việc giải phóng tức thời không được gọi:
       - Node.js backend sử dụng `firebase-admin` thiết lập một Scheduler chạy định kỳ mỗi 1 phút (`setInterval`).
       - Scheduler truy vấn tìm kiếm các tài liệu giữ chỗ trong collection `stock_reservations` có trạng thái `pending` và `expiresAt` nhỏ hơn thời điểm hiện tại.
       - Chạy một Firestore Transaction cho mỗi giữ chỗ hết hạn để hoàn trả lại số lượng (`stockQuantity` + `quantity`) cho từng sản phẩm tương ứng và cập nhật trạng thái giữ chỗ thành `released`.


---

## 📌 PHẦN 4: KHÓ KHĂN & BÀI HỌC KINH NGHIỆM (CHỨNG MINH SỰ TRƯỞNG THÀNH)

* **Khó khăn kỹ thuật lớn nhất:**
  - *Rò rỉ bộ nhớ (Memory Leak) trong Android:* Khi sử dụng `addSnapshotListener()` của Firestore trong Fragment để cập nhật UI chat thời gian thực. Nếu người dùng thoát màn hình chat nhưng listener chưa được giải phóng, nó vẫn tiếp tục lắng nghe và giữ lại tham chiếu đến View/Fragment đó, khiến Garbage Collector không thể thu hồi bộ nhớ, dẫn đến rò rỉ RAM và crash app sau một thời gian dài sử dụng.
  - *Xử lý đồng bộ bất đồng bộ trong thanh toán:* Khi Stripe xử lý trừ tiền thành công nhưng mạng chập chờn khiến app không kịp ghi nhận đơn hàng lên Firestore.
* **Cách debug và khắc phục:**
  - *Khắc phục Memory Leak:* Quản lý chặt chẽ vòng đời của listener bằng cách lưu đối tượng `ListenerRegistration messagesListener`. Trong phương thức kết thúc view `onDestroyView()`, luôn thực hiện kiểm tra và hủy lắng nghe:
    ```java
    if (messagesListener != null) {
        messagesListener.remove();
    }
    ```
  - *Tối ưu hóa khả năng Offline của Firestore:* Tận dụng cơ chế Offline Persistence (tự động lưu bộ nhớ đệm) của Firestore. Khi mất mạng đột ngột, Firestore vẫn cho phép ứng dụng đọc ghi bình thường lên Local Cache và hiển thị UI cho người dùng. Ngay khi có kết nối trở lại, Firestore sẽ tự động đồng bộ hóa các thay đổi xếp hàng từ bộ nhớ tạm lên Cloud Firestore một cách an toàn.
* **Bài học rút ra:**
  - Hiểu sâu sắc tư duy thiết kế cơ sở dữ liệu NoSQL (Firestore): Không lạm dụng việc lồng ghép dữ liệu quá sâu, thiết kế cấu trúc phẳng (Flat design) kết hợp subcollection để tối ưu hóa chi phí vận hành (Read/Write pricing).
  - Ý thức cao về bảo mật giao dịch tài chính: Nắm giữ nguyên tắc phân chia trách nhiệm (Separation of Concerns) giữa client di động (chỉ giữ Publishable Key và xử lý Token thẻ an toàn) và backend server (giữ Secret Key để tạo các giao dịch an toàn).
  - Tầm quan trọng của việc quản lý tài nguyên hệ thống (Memory Management) trên thiết bị di động khi làm việc với các kết nối Socket thời gian thực liên tục.
