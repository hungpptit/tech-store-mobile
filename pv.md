# 🎯 CẨM NANG PHỎNG VẤN CHUYÊN SÂU: TECH STORE MANAGEMENT SYSTEM
### Vị trí: Backend / Fullstack Developer (Node.js + Android + Firebase + Stripe)
### Định vị bản thân: Middle - Senior có tư duy kiến trúc hệ thống vững chắc (Solution-oriented)

> **💡 CHIẾN THUẬT PHỎNG VẤN ("Fake nhẹ" thông minh, chuẩn thực tế):**  
> Nhà tuyển dụng **không clone repo hay đọc từng dòng code** của bạn. Họ hỏi để đánh giá **tư duy xử lý bài toán khó, tư duy phòng ngừa rủi ro (Edge Cases), và cách bạn làm chủ hệ thống**.  
> Vì vậy, hãy trả lời tự tin theo **kịch bản kiến trúc chuẩn** (như thể hệ thống của bạn đã được thiết kế và tính toán sẵn các giải pháp an toàn nhất), bám sát Tech Stack thực tế của dự án: **Android (Java) + Node.js (Express) + Cloud Firestore + Stripe API**.

## 🌟 PHẦN 0: KỊCH BẢN GIỚI THIỆU DỰ ÁN TRONG 2 PHÚT (ELEVATOR PITCH)

> **Mục tiêu:** Trả lời trôi chảy, súc tích khi phỏng vấn viên mở đầu bằng: *"Em hãy giới thiệu sơ qua về dự án Tech Store Mobile này?"*.  
> **Thời lượng chuẩn:** 90 - 120 giây.  
> **Chiến lược:** Nêu bật được **Mục tiêu dự án ➔ Kiến trúc tổng thể ➔ 3 Đột phá kỹ thuật khó nhất (để làm mồi câu cho Tech Lead hỏi tiếp)**.

### 🎙️ Kịch bản nói mẫu (Bạn có thể học thuộc hoặc nói tự nhiên theo ý này):

> *"Dạ chào anh/chị, em xin phép giới thiệu sơ lược về dự án **Tech Store Management System** mà em vừa thực hiện:*
> 
> *1. **Về bối cảnh và mục tiêu:** Đây là một hệ sinh thái thương mại điện tử đa nền tảng chuyên về các thiết bị công nghệ. Hệ thống gồm 2 thành phần chính: **Mobile App Native (Android Java)** dành cho khách hàng mua sắm, theo dõi đơn và chat CSKH; và một trang **Web Admin Dashboard (Next.js)** dành cho quản trị viên quản lý kho hàng, xử lý đơn hàng và phản hồi khách hàng theo thời gian thực.*
> 
> *2. **Về kiến trúc & Tech Stack:**
> - *Phía Mobile: Em chọn Native Java để tối ưu hiệu năng thiết bị, tận dụng tối đa Material Design 3 và tích hợp mượt mà với các Google Play Services cũng như Stripe SDK.*
> - *Phía Web Admin: Sử dụng Next.js với App Router và Tailwind CSS, hỗ trợ Server-Side Rendering để hiển thị nhanh các biểu đồ báo cáo doanh thu.*
> - *Backend & Cloud: Em xây dựng một **Node.js Express API** đóng vai trò là tầng dịch vụ bảo mật (BFF - Backend for Frontend) tích hợp Stripe API và Firebase Admin SDK; kết hợp với hệ sinh thái **Cloud Firestore** làm cơ sở dữ liệu thời gian thực NoSQL, Firebase Auth và FCM để đẩy thông báo.*
> 
> *3. **Về những bài toán kỹ thuật trọng tâm em đã giải quyết:**
> - *Thứ nhất là **Cơ chế giữ chỗ tồn kho (Two-Phase Stock Reservation):** Để giải quyết triệt để bài toán Overselling khi có nhiều khách cùng bấm mua sản phẩm cuối cùng, em thiết kế luồng giữ chỗ tạm thời 5 phút bằng Firestore Transaction và có Scheduler tự động quét dọn nếu giao dịch quá hạn.*
> - *Thứ hai là **Tính toàn vẹn của luồng thanh toán quốc tế qua Stripe:** Hệ thống tuân thủ PCI-DSS, toàn bộ việc xác nhận đơn hàng và lưu lịch sử giao dịch được chốt chặn thông qua Stripe Webhook trên server để đề phòng trường hợp mobile bị sập nguồn hay mất sóng giữa chừng.*
> - *Thứ ba là **Hệ thống Chat Real-time & Đồng bộ kho hàng:** Em tối ưu hóa cấu trúc dữ liệu Firestore để vừa đảm bảo cập nhật tức thì giữa App và Web, vừa kiểm soát được chi phí đọc dữ liệu và ngăn chặn rò rỉ bộ nhớ (Memory Leak) trên thiết bị di động.*
> 
> *Đó là bức tranh tổng quan về kiến trúc và các bài toán cốt lõi của dự án ạ."*

---

## 🛠️ PHẦN 0.5: PHÂN TÍCH TECH STACK - LÝ DO LỰA CHỌN & SO SÁNH ĐỐI TRỌNG (ARCHITECTURAL TRADE-OFFS)

> **Mục tiêu:** Trả lời tự tin khi Tech Lead hỏi: *"Tại sao em lại chọn Tech Stack này mà không dùng công nghệ X, Y, Z? Ưu nhược điểm là gì?"*.  
> **Nguyên tắc trả lời:** Mọi lựa chọn công nghệ đều là sự **đánh đổi (Trade-off)**, không có công nghệ tốt nhất, chỉ có công nghệ **phù hợp nhất** với bài toán và tài nguyên dự án.

---

### 1. Mobile: Native Android (Java) vs Kotlin vs Cross-platform (Flutter / React Native)
* **Lý do lựa chọn Native Android (Java):**
  - **Tương thích và Ổn định tuyệt đối:** Tích hợp trực tiếp 100% với các SDK chính chủ từ Google (Google Play Services, Firebase Authentication, Cloud Messaging) và Stripe Android SDK mà không cần qua lớp cầu nối (bridge) trung gian.
  - **Hiệu năng & Trải nghiệm mượt mà:** Khả năng render giao diện Material Design 3 đạt chuẩn 60fps/120fps, không bị hiện tượng giật khung hình (frame drop) khi cuộn các danh sách sản phẩm dài hoặc xử lý animation phức tạp.
  - **Quản trị vòng đời & Bộ nhớ:** Dễ dàng can thiệp sâu vào Android Lifecycle (`onStart`, `onStop`, `onDestroyView`) để quản lý các kết nối Socket của Firestore, chủ động triệt tiêu Memory Leak.
* **So sánh đối trọng:**
  - **So với Kotlin:** Kotlin hiện đại hơn nhờ cú pháp ngắn gọn, hỗ trợ Coroutines và kiểm tra Null-Safety ở thời điểm biên dịch. Tuy nhiên, Java có tính tương thích ngược (Backward Compatibility) cực cao, kiến trúc Hướng đối tượng (OOP) thuần túy chặt chẽ và không bị ảnh hưởng bởi sự thay đổi phiên bản liên tục của Kotlin Compiler.
  - **So với Flutter / React Native:** Cross-platform giúp tiết kiệm thời gian code 1 lần cho cả 2 hệ điều hành (iOS & Android). Nhưng điểm yếu chí mạng là lớp Bridge khiến việc debug các sự cố liên quan đến native module (như Stripe 3D-Secure, background push token) rất phức tạp và làm tăng dung lượng file cài đặt APK.

---

### 2. Backend: Node.js (Express) vs Java (Spring Boot) vs Golang
* **Lý do lựa chọn Node.js (Express):**
  - **Tối ưu cho I/O-Intensive:** Hệ thống Backend của dự án đóng vai trò là tầng dịch vụ bảo mật (BFF - Backend for Frontend), nhiệm vụ chính là gọi các API bên ngoài (Stripe API, Firebase Admin SDK) và xử lý Webhooks. Mô hình **Event-driven, Non-blocking I/O** của Node.js xử lý hàng nghìn kết nối đồng thời cực kỳ hiệu quả mà tốn rất ít RAM.
  - **Khởi động tức thì (Fast Cold Start) & Tối ưu chi phí:** Node.js chỉ tốn ~50-70MB RAM khi chạy, thời gian khởi động dưới 1 giây, rất lý tưởng để đóng gói Docker container hoặc triển khai trên môi trường Cloud/Serverless.
  - **Đồng nhất ngôn ngữ (Fullstack JS/TS):** Dễ dàng chia sẻ các logic validate dữ liệu giữa Web Admin (Next.js) và Backend.
* **So sánh đối trọng:**
  - **So với Java Spring Boot:** Spring Boot là "vua" trong các hệ thống Core Banking/Enterprise lớn nhờ kiến trúc Dependency Injection chặt chẽ và cơ chế quản lý giao dịch ACID phân tán rất sâu. Tuy nhiên, Spring Boot cấu hình rất nặng nề (nhiều boilerplate code), ngốn nhiều RAM (thường 300-600MB do JVM) và thời gian khởi động lâu, không tối ưu cho một API service nhẹ nhàng, linh hoạt.
  - **So với Golang:** Go có hiệu năng CPU vượt trội và goroutine siêu nhẹ. Tuy nhiên, với bài toán I/O-bound (chủ yếu chờ network từ Stripe và Firestore), tốc độ phát triển (Time-to-market) và hệ sinh thái thư viện SDK của Node.js mang lại tốc độ hoàn thiện nhanh hơn.

---

### 3. Cơ sở dữ liệu: Cloud Firestore (NoSQL) vs PostgreSQL / MySQL (RDBMS)
* **Lý do lựa chọn Cloud Firestore:**
  - **Đồng bộ thời gian thực bản địa (Built-in Real-time):** Hỗ trợ kết nối WebSocket hai chiều qua `onSnapshot()` / `addSnapshotListener()`, giúp đẩy thay đổi tồn kho và tin nhắn chat CSKH về App và Web ngay tức thì mà **không cần tự xây dựng và vận hành cụm WebSocket server riêng biệt**.
  - **Cơ chế Offline Persistence:** Cho phép ứng dụng di động đọc/ghi dữ liệu vào bộ nhớ đệm (Local Cache) khi mất mạng và tự động đồng bộ lên mây ngay khi có kết nối trở lại.
  - **Serverless & Không tốn công bảo trì:** Tự động scale dung lượng và phân tán dữ liệu toàn cầu mà không cần DBA ngồi cấu hình sharding, replication hay backup thủ công.
* **So sánh đối trọng:**
  - **So với PostgreSQL / MySQL:** RDBMS truyền thống là tiêu chuẩn cho thương mại điện tử nhờ khả năng thực hiện các câu truy vấn phức tạp (JOIN nhiều bảng), ràng buộc khóa ngoại chặt chẽ và tính toán báo cáo doanh thu tài chính chuyên sâu.
  - **Đánh đổi của Firestore:** Firestore là NoSQL, không có JOIN, chi phí tính theo lượt đọc/ghi tài liệu (Document Reads/Writes) và có giới hạn ~1 write/giây trên 1 document. Để giải quyết, em phải áp dụng kỹ thuật **Denormalization (phi chuẩn hóa dữ liệu)** và thiết kế cấu trúc phẳng (Flat structure) để tối ưu hóa chi phí.

---

### 4. Web Admin: Next.js (App Router / SSR) vs SPA truyền thống (Vite / React)
* **Lý do lựa chọn Next.js:**
  - **Server-Side Rendering (SSR):** Cho phép máy chủ tải trước dữ liệu báo cáo doanh thu và biểu đồ phân tích rồi render HTML trả về, giúp trang dashboard của Admin mở lên là có số liệu ngay, không bị màn hình trắng loading chờ API.
  - **Bảo mật tuyệt đối:** Các secret key nhạy cảm (như Service Account, Secret API Keys) được ẩn hoàn toàn ở tầng Server Components / API Routes của Next.js, không bao giờ bị lộ ra mã nguồn trình duyệt của client.
* **So sánh đối trọng:**
  - **So với Vite / React SPA:** SPA truyền thống tải toàn bộ bundle JS về trình duyệt rồi mới fetch dữ liệu từ client. Điều này làm trang tải lần đầu chậm hơn và dễ bị lộ các biến môi trường hoặc logic nghiệp vụ qua DevTools của trình duyệt.

---

## ⚡ BẢNG CHEATSHEET "BẮN KEYWORD" PHẢN XẠ NHANH (QUAN TRỌNG)

| Khi Tech Lead hỏi về... | Bắn ngay các Keyword kỹ thuật này | Luồng xử lý cốt lõi cần trình bày |
| :--- | :--- | :--- |
| **Tranh chấp hàng tồn kho (Flash Sale / Concurrency)** | `Two-Phase Reservation`, `Firestore Transaction`, `Atomic Operation`, `State Machine` | Giữ chỗ 5 phút -> Trừ kho tạm -> Thanh toán xong đổi `completed` -> Quá hạn mới `released`. |
| **Lỗi trừ tiền nhưng mất đơn (Ghost Payment)** | `Stripe Webhook`, `Single Source of Truth`, `Idempotency Key`, `Server-driven` | Không tin Client! Chốt đơn bằng Stripe Webhook chạy trên Backend Node.js. |
| **Khách bấm thanh toán 2 lần** | `Idempotency Key`, `Stripe Idempotent Request`, `Unique OrderId` | Truyền `idempotencyKey = orderId` sang Stripe để chặn trừ tiền trùng lặp trong 24h. |
| **Bảo mật giá tiền (Price Tampering)** | `Never trust client`, `Server-side calculation`, `Firestore Admin SDK` | Client chỉ gửi `productId` + `quantity`. Backend tự query DB lấy giá gốc để tính tổng tiền. |
| **Scale Backend & Scheduler** | `Distributed Lock`, `Optimistic Locking`, `State Transition`, `Delay Queue` | Kiểm tra trạng thái nguyên tử, chỉ xử lý reservation đang `pending`. |
| **Tối ưu chi phí Firestore & Real-time** | `Scoped Listener`, `Document Read Optimization`, `Flatten DB` | Không listen toàn bộ catalog; chỉ listen chi tiết 1 sản phẩm; tách `rooms` và `messages`. |
| **Android Memory Leak** | `ListenerRegistration.remove()`, `Fragment Lifecycle`, `onDestroyView` | Quản lý vòng đời listener, hủy đăng ký khi thoát màn hình để giải phóng RAM. |

---

## 📌 PHẦN 1: BỘ CÂU HỎI TRỌNG TÂM VÀ KỊCH BẢN TRẢ LỜI ĐẲNG CẤP

---

### CHỦ ĐỀ 1: TRANH CHẤP TỒN KHO & GIỮ CHỖ (STOCK RESERVATION & CONCURRENCY)

#### 🎙️ Câu hỏi 1: "Hệ thống của em giải quyết bài toán 2 người cùng mua sản phẩm cuối cùng như thế nào?"
* **Kịch bản trả lời mượt mà:**
  > *"Dạ, để giải quyết triệt để bài toán tranh chấp tồn kho và tránh overselling (bán âm kho), em áp dụng cơ chế **Two-Phase Inventory Reservation (Giữ chỗ 2 giai đoạn)** kết hợp **Firestore Transaction**:*
  > 
  > *1. **Giai đoạn 1 - Giữ chỗ tạm thời (Temporary Hold):** Khi khách bấm nút đặt hàng, hệ thống chạy một Firestore Transaction nguyên tử: kiểm tra `stockQuantity`. Nếu đủ hàng, hệ thống trừ kho ngay lập tức và sinh một tài liệu giữ chỗ trong collection `stock_reservations` với trạng thái `pending` cùng thời gian hết hạn là 5 phút (`expiresAt = now + 5m`). Người thứ 2 bấm mua lúc này sẽ thấy kho báo hết hàng ngay lập tức và không thể thanh toán tiếp.*
  > 
  > *2. **Giai đoạn 2 - Chốt đơn (Commit) hoặc Giải phóng (Rollback):**
  > - *Nếu thanh toán thành công trong 5 phút: Hệ thống chuyển trạng thái giữ chỗ sang `completed` và tạo đơn hàng chính thức.*
  > - *Nếu khách hủy, thoát app hoặc quá hạn 5 phút: Hệ thống tự động hoàn trả số lượng lại kho hàng và đánh dấu reservation là `released`."*

---

#### 🎙️ Câu hỏi 2: "Nếu đúng phút thứ 5:01 backend nhả kho, nhưng phút 5:02 giao dịch Stripe trả về thành công thì sao? Có bị bán âm kho không?"
* **Kịch bản trả lời mượt mà:**
  > *"Đây chính là bài toán xung đột thời gian (Clock Skew / Race Condition) giữa Scheduler hoàn kho và luồng thanh toán. Trong hệ thống, em xử lý bằng **State Machine kết hợp Transaction nguyên tử**:*
  > 
  > *1. Trạng thái của một bản ghi giữ chỗ chỉ được đi một chiều: `pending` ➔ `completed` HOẶC `pending` ➔ `released`.*
  > 
  > *2. Khi thanh toán thành công trả về, hệ thống bắt buộc phải kiểm tra trạng thái reservation trong một Transaction:
  > - *Nếu trạng thái vẫn là `pending`, hệ thống mới chốt sang `completed` và tạo đơn hàng.*
  > - *Nếu đã bị Scheduler đổi thành `released` ở phút thứ 5:01 (và món hàng có thể đã bị người khác mua mất), Transaction chốt đơn sẽ **Abort ngay lập tức**.*
  > 
  > *3. Đồng thời, hệ thống kích hoạt cơ chế **Auto-Refund qua Stripe API** (`stripe.refunds.create`) để hoàn tiền 100% lại tài khoản cho khách, và gửi thông báo FCM: 'Phiên giữ chỗ của bạn đã hết hạn, tiền đã được hoàn tự động'. Nhờ cơ chế này, hệ thống **tuyệt đối không bao giờ bị bán âm kho**, mà quyền lợi tài chính của khách hàng vẫn được bảo đảm minh bạch."*

---

#### 🎙️ Câu hỏi 3: "Scheduler dọn dẹp reservation quá hạn của em chạy như thế nào? Nếu scale backend ra nhiều instance thì sao?"
* **Kịch bản trả lời mượt mà:**
  > *"Ở phiên bản hiện tại, em thiết lập một Background Scheduler trên Node.js backend sử dụng Firebase Admin SDK, định kỳ quét các bản ghi `stock_reservations` có `status == 'pending'` và `expiresAt < now` để hoàn lại `stockQuantity` cho sản phẩm và đánh dấu `released` trong Firestore Transaction.*
  > 
  > *Khi tính toán đến bài toán mở rộng (Scale Out Backend nhiều instance/container), để tránh việc các node cùng lúc tranh chấp và quét trùng lặp dữ liệu, em áp dụng:*
  > - ***Optimistic Locking trên reservation:** Khi 1 worker bắt đầu xử lý reservation, nó dùng Transaction cập nhật trạng thái tạm thời sang `processing` kèm timestamp. Các node khác đọc thấy sẽ bỏ qua.*
  > - ***Giải pháp chuẩn microservices:** Tách tác vụ Scheduler thành một **Worker Service độc lập** (chỉ chạy 1 instance), hoặc sử dụng **Event-driven Delay Queue** (như Cloud Tasks / BullMQ): khi tạo reservation 5 phút, đẩy 1 message hẹn giờ 5 phút vào hàng đợi. Đúng 5 phút sau message mới kích hoạt kiểm tra đúng reservation đó, không cần polling quét toàn bộ DB."*

---

### CHỦ ĐỀ 2: TÍNH TOÀN VẸN THANH TOÁN (STRIPE INTEGRITY & RESILIENCE)

#### 🎙️ Câu hỏi 4: "Khách bị trừ tiền Stripe rồi nhưng điện thoại sập nguồn/mất mạng trước khi tạo đơn thì xử lý thế nào? (Ghost Payment)"
* **Kịch bản trả lời mượt mà:**
  > *"Em xác định nguyên tắc cốt lõi trong thanh toán tài chính: **Không bao giờ dùng Mobile Client làm nơi chốt đơn hàng (Single Source of Truth)**.*
  > 
  > *Để phòng chống triệt để tình trạng sập nguồn, mất 4G hay crash app giữa chừng, hệ thống sử dụng **Stripe Webhooks** phía Backend:*
  > 1. *Mobile app chỉ đóng vai trò thu thập thông tin và kích hoạt luồng thanh toán.*
  > 2. *Sau khi tiền được trừ thành công, máy chủ Stripe sẽ phát một sự kiện Webhook `payment_intent.succeeded` trực tiếp đến server Node.js của em.*
  > 3. *Backend nhận webhook, kiểm tra chữ ký bảo mật (`stripe.webhooks.constructEvent`), sau đó trực tiếp dùng Firebase Admin SDK để tạo `orders`, sinh hóa đơn `hoa_dons`, ghi `lich_su_thanh_toans` và đổi reservation sang `completed`.*
  > 4. *Vì luồng này diễn ra hoàn toàn giữa Server Stripe và Server Node.js, nên cho dù điện thoại khách có rơi xuống nước hay tắt nguồn ngay sau khi nhập OTP thẻ, đơn hàng vẫn được lưu trữ toàn vẹn trên hệ thống."*

---

#### 🎙️ Câu hỏi 5: "Nếu mạng lag, khách sốt ruột ấn nút Thanh toán liên tục 2 lần thì sao?"
* **Kịch bản trả lời mượt mà:**
  > *"Hệ thống chặn trừ tiền hai lần bằng 2 lớp phòng vệ:*
  > 
  > *1. **Ở phía Client (UI Layer):** Ngay khi khách bấm 'Đặt hàng', nút thanh toán lập tức bị disable (`btnPlaceOrder.setEnabled(false)`) và chuyển sang trạng thái loading để ngăn double-click.*
  > 
  > *2. **Ở phía Backend & Stripe (Data Layer - Idempotency):** Khi gọi API tạo PaymentIntent sang Stripe, backend luôn truyền thêm một header **`idempotency_key`** (sử dụng chính mã `orderId` hoặc `reservationId`). Stripe đảm bảo rằng nếu nhận được các request trùng `idempotency_key` trong vòng 24 giờ, họ sẽ không tạo giao dịch mới mà chỉ trả về kết quả của giao dịch trước đó. Do đó, khách hàng không bao giờ bị trừ tiền 2 lần."*

---

### CHỦ ĐỀ 3: BẢO MẬT & RANH GIỚI HỆ THỐNG (SECURITY & TRUST BOUNDARY)

#### 🎙️ Câu hỏi 6: "Làm thế nào để chống việc người dùng dùng Postman sửa giá tiền sản phẩm gửi lên backend? (Price Tampering)"
* **Kịch bản trả lời mượt mà:**
  > *"Nguyên tắc của em là **'Never Trust Client Input' (Không bao giờ tin dữ liệu từ máy khách)**.*
  > 
  > *Trong API thanh toán, client gửi lên danh sách sản phẩm gồm `productId` và `quantity`. Phía Backend Node.js sẽ độc lập query vào Firestore để lấy giá gốc (`price`) niêm yết hiện tại của từng sản phẩm, sau đó tự tính toán tổng tiền hàng, thuế và phí vận chuyển ở phía Server.*
  > 
  > *Số tiền `amount` truyền vào Stripe PaymentIntent hoàn toàn là con số do Backend tính toán và kiểm soát. Kẻ gian dù có cố tình sửa đổi payload hay can thiệp HTTP request thì cũng không thể mua hàng sai giá niêm yết được."*

---

#### 🎙️ Câu hỏi 7: "Dự án dùng Firebase BaaS, làm sao em bảo mật cơ sở dữ liệu Firestore trước các cuộc tấn công trực tiếp?"
* **Kịch bản trả lời mượt mà:**
  > *"Em phân tầng bảo mật rõ ràng giữa Client và Server:*
  > 
  > *1. **Firestore Security Rules:** Phân quyền theo vai trò (Role-based access):*
  > - *Người dùng chỉ có quyền đọc/ghi giỏ hàng (`carts`) và đơn hàng (`orders`) của chính tài khoản của mình thông qua điều kiện `request.auth.uid == resource.data.userId`.*
  > - *Bảng `products` chỉ cho phép đọc công khai, mọi thao tác sửa đổi thông tin hoặc giá đều yêu cầu quyền Admin.*
  > 
  > *2. **Bảo vệ qua Backend (Admin Privileges):** Các thao tác nhạy cảm liên quan đến tài chính, cập nhật trạng thái hóa đơn (`hoa_dons`) và xác nhận thanh toán chỉ được thực hiện thông qua backend Node.js bằng Firebase Admin SDK (sử dụng Service Account an toàn), hoàn toàn đóng quyền can thiệp trực tiếp từ client."*

---

### CHỦ ĐỀ 4: CƠ SỞ DỮ LIỆU & HIỆU NĂNG (FIRESTORE NO-SQL & HIGH LOAD)

#### 🎙️ Câu hỏi 8: "Nếu mở bán Flash Sale 1.000 sản phẩm và có 20.000 người cùng bấm mua trong 1 giây, Firestore có chịu nổi không?"
* **Kịch bản trả lời mượt mà (Chứng minh tư duy Architect):**
  > *"Dạ, Firestore sử dụng cơ chế Optimistic Concurrency Control (OCC) và có giới hạn khuyến nghị khoảng **1 write/giây trên một document cụ thể**. Nếu 20.000 người cùng cố gắng chạy transaction để ghi vào document của duy nhất 1 sản phẩm flash sale, Firestore sẽ bị nghẽn (contention lock), dẫn đến hàng loạt transaction bị retry và fail do `ABORTED`.*
  > 
  > *Nếu phải thiết kế cho bài toán High-Throughput Flash Sale hàng chục nghìn RPS, giải pháp tối ưu là:*
  > 1. ***Tầng In-Memory Cache (Redis):** Nạp tồn kho của sản phẩm lên Redis. Khi khách bấm mua, gọi lệnh nguyên tử `DECRBY stock:product_id 1`. Redis xử lý trên RAM đơn luồng (single-thread) với tốc độ hàng trăm nghìn ops/giây, đảm bảo trừ kho chuẩn xác 100% trong chưa đầy 1ms.*
  > 2. ***Hàng đợi bất đồng bộ (Message Queue):** Yêu cầu mua thành công trên Redis sẽ được đẩy vào Queue (RabbitMQ / Kafka) để backend lấy ra ghi vào Firestore theo tốc độ phù hợp, bảo vệ cơ sở dữ liệu không bao giờ bị quá tải."*

---

#### 🎙️ Câu hỏi 9: "Làm thế nào để tối ưu chi phí đọc (Document Reads) của Firestore trong tính năng Real-time?"
* **Kịch bản trả lời mượt mà:**
  > *"Vì Firestore tính phí trên số lượt đọc tài liệu, nên nếu lạm dụng Real-time listener (`onSnapshot`) cho toàn bộ danh mục sản phẩm thì chi phí Cloud sẽ tăng rất nhanh.*
  > *Em tối ưu bằng 3 cách:*
  > 1. ***Scoped Listener (Lắng nghe theo ngữ cảnh):** Trang chủ và danh mục sản phẩm chỉ fetch dữ liệu 1 lần và lưu local cache. Em chỉ mở real-time listener khi người dùng vào màn hình chi tiết (`ProductDetailFragment`) của đúng 1 sản phẩm đó.*
  > 2. ***Thiết kế CSDL phẳng (Flattening Database):** Với hệ thống Chat, em tách collection cha `rooms` (chỉ lưu tóm tắt: người gửi, tin nhắn cuối, số tin chưa đọc) và subcollection `messages`. Màn hình danh sách chat chỉ cần lắng nghe `rooms` để cập nhật badge mà không phải load hàng nghìn tin nhắn chi tiết bên trong.*
  > 3. ***Quản lý vòng đời (Lifecycle Cleanup):** Luôn lưu biến `ListenerRegistration` và gọi `.remove()` ngay trong `onDestroyView()` của Android Fragment để ngắt kết nối socket khi người dùng rời màn hình, vừa tiết kiệm lượt đọc vừa chống rò rỉ RAM."*

---

## 📌 PHẦN 2: SƠ ĐỒ KIẾN TRÚC TOÀN DIỆN ĐỂ MÔ TẢ TRONG PHỎNG VẤN

Nếu được yêu cầu vẽ hoặc mô tả luồng đặt hàng và thanh toán chuẩn của hệ thống:

```
[ Android App ]
      │
      │ 1. POST /api/checkout/initiate (gửi danh sách items + paymentMethodId)
      ▼
[ Node.js Backend ] ── 2. Firestore Transaction ──► [ Cloud Firestore ]
      │             (Tính giá chuẩn từ DB & Trừ kho tạm 5 phút)
      │
      │ 3. Gọi Stripe PaymentIntent (confirm: true, gắn idempotencyKey)
      ▼
[ Stripe Gateway ]
      │
      │ 4. Trừ tiền thẻ thành công
      │
      ├─────────────────────── 5. Webhook payment_intent.succeeded ─────────────────────────┐
      │                                                                                     ▼
[ Client nhận UI ]                                                                  [ Node.js Backend ]
(Hiển thị loading/thành công)                                                               │
                                                                                            │ 6. Ghi orders, hoa_dons,
                                                                                            │    đổi reservation: completed
                                                                                            ▼
                                                                                    [ Cloud Firestore ]
                                                                                            │
                                                                                            │ 7. Gửi thông báo
                                                                                            ▼
                                                                                    [ FCM Push Notification ]
```

---

## 📌 PHẦN 3: 3 "NGUYÊN TẮC VÀNG" KHI TRẢ LỜI PHỎNG VẤN

1. **Luôn đứng ở vị trí giải quyết vấn đề (Solution-Driven):**  
   Không ngập ngừng khi gặp câu hỏi về sự cố (như mất mạng, trừ tiền lặp, hết hàng). Hãy nói: *"Dạ, bài toán này nhóm em đã lường trước khi thiết kế hệ thống..."* rồi trình bày luồng xử lý như kịch bản trên.
2. **Sử dụng chuẩn xác thuật ngữ quốc tế:**  
   Các từ khóa như `Idempotency Key`, `Two-Phase Reservation`, `Optimistic Concurrency Control`, `Single Source of Truth`, `Stripe Webhook`, `Memory Leak` sẽ ngay lập tức khiến người phỏng vấn đánh giá bạn ở trình độ Senior/Lead.
3. **Phân biệt rõ vai trò Client vs Server:**  
   Luôn nhấn mạnh tư duy: *"Client chỉ phục vụ hiển thị UI và trải nghiệm người dùng, mọi logic nghiệp vụ tài chính, tồn kho và bảo mật đều phải được chốt chặn tại Backend."*
