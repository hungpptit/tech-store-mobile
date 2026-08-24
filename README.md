<div align="center">

# 📱 TECH STORE MOBILE & BACKEND ECOSYSTEM
### *Enterprise-Grade E-Commerce Solution for Mobile with Distributed Inventory Locking & Secure Stripe Payments*

[![Android SDK](https://img.shields.io/badge/Android%20SDK-API%2024%2B%20%7C%20Target%2036-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Java-11-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Node.js](https://img.shields.io/badge/Node.js-18%2B-339933?style=for-the-badge&logo=nodedotjs&logoColor=white)](https://nodejs.org/)
[![Express.js](https://img.shields.io/badge/Express.js-4.21-000000?style=for-the-badge&logo=express&logoColor=white)](https://expressjs.com/)
[![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20Auth%20%7C%20FCM-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)
[![Stripe](https://img.shields.io/badge/Stripe-Payment%20Intents%20%7C%20PCI--DSS-635BFF?style=for-the-badge&logo=stripe&logoColor=white)](https://stripe.com/)
[![Cloudinary](https://img.shields.io/badge/Cloudinary-Media%20CDN-3448C5?style=for-the-badge&logo=cloudinary&logoColor=white)](https://cloudinary.com/)

<p align="center">
  <b>Dự án Thương mại Điện tử Đa nền tảng (Android Client + Node.js Micro-Backend + Firebase Cloud Infrastructure)</b><br>
  Tập trung giải quyết các bài toán kỹ thuật chuyên sâu về <b>Concurrency Control (Chống Overselling)</b>, <b>ACID Transactions</b>, <b>PCI-DSS Compliant Payment Pipeline</b>, và <b>Hệ thống Chat CSKH Real-time</b>.
</p>

---

</div>

## 📌 Executive Summary (Tổng quan dự án)

**Tech Store Mobile** là một giải pháp thương mại điện tử hoàn chỉnh cho thiết bị di động, được xây dựng theo chuẩn kiến trúc phân lớp hiện đại (**Clean Architecture & MVVM**). Dự án mô phỏng môi trường kinh doanh bán lẻ thiết bị công nghệ cao cấp với lưu lượng người dùng đồng thời, đòi hỏi tính toàn vẹn dữ liệu tài chính tuyệt đối, độ trễ thấp và trải nghiệm người dùng mượt mà theo chuẩn **Google Material Design 3**.

Dự án không dừng lại ở mức ứng dụng Client đơn thuần mà tích hợp một **Node.js/Express Backend Service** chuyên trách xử lý các tác vụ nhạy cảm: bảo mật cổng thanh toán Stripe, quản lý Customer Vault, và chạy **Worker định kỳ (Cron Reconciliation)** giải phóng kho tự động khi hết hạn thanh toán.

---

## 🏗 System Architecture (Kiến trúc hệ thống)

Hệ thống được thiết kế theo mô hình lai (**Hybrid Cloud & Micro-Backend**), tận dụng sức mạnh xử lý thời gian thực của Firebase song song với Backend chuyên dụng cho thanh toán:

```mermaid
flowchart TB
    subgraph MobileClient["📱 Android Client (Java 11 - MVVM)"]
        UI["UI Layer (Material 3 / ViewPager2 / RecyclerView)"]
        VM["Business & State Controllers (Fragments & Helpers)"]
        APIClients["Network Clients (StripeApiClient, VietnamProvincesApi)"]
        GlideCDN["Glide Engine & Cloudinary SDK"]
    end

    subgraph BackendService["⚙️ Node.js / Express Backend"]
        PaymentAPI["/api/payments/create-payment-intent"]
        CardVaultAPI["/api/payment-methods/create-card"]
        CronJob["Cron Reconciliation Worker (1-min interval)"]
    end

    subgraph CloudServices["☁️ Cloud & Third-Party Infrastructure"]
        FirebaseAuth["Firebase Authentication (Scrypt Hashing)"]
        FirestoreDB[("Cloud Firestore (NoSQL ACID Transactions)")]
        StripeServer["Stripe Payment Gateway (PCI-DSS Vault)"]
        FCMServer["Firebase Cloud Messaging (FCM Push)"]
        CloudinaryCDN["Cloudinary Media CDN"]
        ProvincesAPI["Vietnam Provinces Open API"]
    end

    %% Client Interactions
    UI --> VM
    VM --> APIClients
    VM --> GlideCDN
    VM --> FirebaseAuth
    VM --> FirestoreDB

    %% Network & Backend
    APIClients -->|REST API (HTTPS)| BackendService
    PaymentAPI -->|Stripe SDK| StripeServer
    CardVaultAPI -->|Stripe SDK| StripeServer
    CronJob -->|Admin SDK ACID Transactions| FirestoreDB

    %% External Services
    GlideCDN -->|Upload/Fetch Avatars| CloudinaryCDN
    APIClients -->|Fetch Administrative Units| ProvincesAPI
    FirestoreDB -.->|Snapshot Listeners (Chat & Inventory)| VM
    FCMServer -.->|Order & Promo Push| UI
```

---

## 💡 Key Engineering Highlights (Điểm sáng Kỹ thuật & Backend)

### 1. 🛡️ Cơ chế Khóa tồn kho Chống Overselling (Stock Reservation Pattern)
*   **Vấn đề:** Khi nhiều người dùng cùng nhấn mua một sản phẩm có số lượng tồn kho giới hạn ($N=1$), tình trạng **Race Condition** dễ dẫn đến việc bán vượt quá tồn kho thực tế (Overselling).
*   **Giải pháp:** 
    *   Triển khai thuật toán **Pessimistic Stock Reservation**: Trước khi chuyển sang cổng thanh toán, hệ thống thực thi một **Firestore ACID Transaction** để kiểm tra `stockQuantity` khả dụng. Nếu đủ, trừ trực tiếp tồn kho và tạo bản ghi `stock_reservations` với trạng thái `pending` và **TTL 5 phút** (`expiresAt`).
    *   Xây dựng **Background Cron Reconciliation Service** chạy mỗi 60 giây trên Node.js Backend sử dụng `Firebase Admin SDK`. Worker tự động quét các reservation quá hạn chưa thanh toán, mở Transaction hoàn trả số lượng (`stockQuantity = stockQuantity + N`) và chuyển trạng thái sang `released`.
    *   Nếu người dùng hủy đơn hoặc gặp lỗi thanh toán từ Stripe, Client kích hoạt `releaseReservationImmediately()` để hoàn hàng về kho ngay lập tức.

### 2. 💳 Pipeline Thanh toán An toàn chuẩn PCI-DSS (Stripe Integration)
*   **Zero Raw Card Exposure:** Android Client sử dụng `Stripe Android SDK` thu thập thông tin thẻ và đổi lấy token đại diện một lần (`tok_...`) trực tiếp từ máy chủ Stripe. Mã thẻ CVV/CVC và số thẻ thô **tuyệt đối không đi qua Backend hoặc Database của ứng dụng**.
*   **Customer & PaymentMethod Vault:** Backend Node.js nhận Token, khởi tạo/liên kết thực thể `Stripe Customer` tương ứng với `userId`, sau đó gắn thẻ vào Vault và trả về thông tin thẻ đã che (`**** **** **** 4242`, `brand`, `expiry`).
*   **Two-Phase Order Finalization (Atomic Write):** Khi Stripe xác nhận thanh toán `succeeded`, hệ thống sử dụng **Firebase `WriteBatch`** để ghi đồng thời 4 tài liệu liên kết trong một thao tác nguyên tử:
    1. Tạo Đơn hàng (`orders`) kèm Snapshot địa chỉ và trạng thái vận chuyển ban đầu.
    2. Xuất Hóa đơn điện tử (`hoa_dons`) phục vụ đối soát tài chính.
    3. Lưu Lịch sử giao dịch chi tiết (`lich_su_thanh_toans`) bao gồm `stripePaymentIntentId`.
    4. Xóa sạch các mặt hàng tương ứng trong giỏ (`cart`) và cập nhật reservation thành `completed`.

### 3. 💬 Hệ thống Chat Trực tuyến Hai chiều (Real-Time Customer Service)
*   **Low-Latency Sync:** Sử dụng cơ chế `addSnapshotListener` của Firestore lắng nghe sự kiện luồng tin nhắn `rooms/{userId}/messages` theo thời gian thực.
*   **Programmatic UI Rendering:** Loại bỏ việc khởi tạo ViewHolder adapter phức tạp cho phiên chat hỗ trợ ngắn; thay vào đó, ứng dụng sinh động các bong bóng chat (`appendMessageBubble`) trên `LinearLayout` lồng trong `NestedScrollView` với cờ `animateLayoutChanges="true"` giúp chuyển cảnh mượt mà 60fps.
*   **Admin-Ready Multi-Channel Architecture:** Hỗ trợ đồng bộ đa trạng thái (Đánh dấu gắn sao `isStarredByAdmin`, lọc tin nhắn spam `isSpamByAdmin`, và theo dõi số tin chưa đọc độc lập `userUnreadCount` / `adminUnreadCount`).

### 4. 📍 Phân cấp Dữ liệu Địa chính Động (Vietnam Provinces Cascading Engine)
*   Tích hợp RESTful API từ **Vietnam Provinces Open API** với cơ chế nạp dữ liệu phân cấp 3 tầng (**Tỉnh/Thành phố $\rightarrow$ Quận/Huyện $\rightarrow$ Phường/Xã**).
*   Lưu trữ địa chỉ người dùng theo định dạng chuẩn hóa NoSQL Map (`location: { provinceCode, districtCode, wardCode, detail, fullAddress }`), đảm bảo khả năng snapshot bất biến vào đơn hàng tại thời điểm mua.

---

## 🗄 Database Design & Data Modeling (Firestore Schema)

Cấu trúc cơ sở dữ liệu NoSQL được thiết kế tối ưu hóa cho tốc độ đọc và khả năng chịu tải:

| Collection / Sub-collection | Mục đích sử dụng | Key Fields & Indexing Strategy |
| :--- | :--- | :--- |
| `users` | Hồ sơ tài khoản & Cấu hình mặc định | `userId` (PK/Auth UID), `email`, `role`, `fcmToken`, `defaultAddressId`, `defaultPaymentId` |
| `products` | Danh mục hàng hóa & Tìm kiếm | `productId`, `categoryId`, `productName`, `basePrice`, `finalPrice`, `stockQuantity`, `rating`, `searchKeywords` (Array Index) |
| `categories` | Phân loại danh mục công nghệ | `categoryId`, `categoryName`, `imageUrl`, `displayOrder` |
| `cart` | Trạng thái giỏ hàng theo User | `userId` (Doc ID), `items: [{ productId, quantity, selectedColor, priceAtAdded }]`, `updatedAt` |
| `stock_reservations` | Quản lý giữ kho chống Race Condition | `reservationId`, `userId`, `items`, `status` (`pending` \| `completed` \| `released`), `expiresAt`, `createdAt` |
| `orders` | Đơn hàng & Lộ trình giao vận | `orderId`, `userId`, `status` (`Packing` $\rightarrow$ `Picked` $\rightarrow$ `In Transit` $\rightarrow$ `Delivered` $\rightarrow$ `Completed`), `summary`, `shippingAddressSnapshot` |
| `hoa_dons` | Hóa đơn thanh toán đối soát | `hoaDonId`, `orderId`, `userId`, `paymentGateway` (`Stripe`), `transactionId`, `paymentStatus` (`Paid`) |
| `lich_su_thanh_toans` | Lịch sử giao dịch cổng thanh toán | `paymentHistoryId`, `orderId`, `stripePaymentIntentId`, `amount`, `currency`, `paidAt` |
| `addresses` | Sổ địa chỉ giao nhận của User | `addressId`, `userId`, `location` (Map 3 cấp), `isDefault` (Boolean Index) |
| `payment_methods` | Danh sách thẻ thanh toán đã mã hóa | `paymentId`, `userId`, `cardType`, `cardNumber` (Masked), `expiryDate`, `isDefault` |
| `rooms/{userId}/messages` | Hệ thống tin nhắn CSKH Real-time | Sub-collection: `senderId`, `receiverId`, `content`, `type`, `createdAt` (ServerTimestamp) |
| `reviews` | Đánh giá & Nhận xét sản phẩm | `reviewId`, `productId`, `userId`, `rating`, `comment`, `createdAt` |
| `notifications` | Hộp thư thông báo cá nhân (FCM) | `notificationId`, `userId`, `orderId`, `title`, `content`, `type`, `isRead` |
| `global_announcements` | Thông báo toàn hệ thống từ Admin | `announcementId`, `adminId`, `title`, `content`, `imageUrl`, `targetGroup`, `scheduledAt` |

---

## 💻 Tech Stack & Engineering Tools

### Client-Side (Android Native)
*   **Core:** Java 11, Android SDK (minSdk 24, targetSdk 36, compileSdk 36).
*   **Architecture & UI:** MVVM Pattern, Google Material Design 3, ViewPager2, TabLayout, ConstraintLayout, NestedScrollView, BottomSheetDialogFragment.
*   **Networking & Async:** OkHttp / Retrofit API Client, Gson serialization, Firebase Firestore Asynchronous Tasks.
*   **Media & CDN:** Cloudinary Android SDK `v3.1.2`, Glide `v4.15.1` (với Cache Strategy & Transformations).
*   **Payment & Security:** Stripe Android SDK `v20.48.0` (Tokenization, CardInputWidget).
*   **Real-time & Auth:** Firebase BoM `v34.12.0` (Authentication, Cloud Firestore, Cloud Messaging FCM, Firebase Storage).

### Server-Side (Micro-Backend)
*   **Runtime & Framework:** Node.js 18+, Express.js 4.21.
*   **SDKs & Integrations:** Stripe Server SDK `v17.6.0`, Firebase Admin SDK `v12.1.0`.
*   **Security & Networking:** CORS middleware, Dotenv environment configuration, HTTPS proxying via ngrok/Cloud.
*   **Workers & Automation:** In-memory Cron Reconciler cho tác vụ giải phóng tồn kho định kỳ.

---

## 📱 Detailed Functional Modules (Phân hệ tính năng chi tiết)

```
tech-store-mobile/
├── 🔐 Authentication & Identity Management
│   ├── Google Sign-In & Email/Password Authentication (Firebase Auth)
│   ├── Password Security with Scrypt One-way Cryptographic Hashing
│   └── Profile Management, Avatar Upload via Cloudinary CDN
│
├── 🛍️ Catalog, Smart Search & Filter
│   ├── Category-driven dynamic catalog (Laptop, Phone, Watch, Audio, Screen)
│   ├── Advanced Search with Tokenized Keyword Matching (NoSQL Array-Contains)
│   ├── Real-time Search History persistence & quick suggestions
│   └── Wishlist / Saved Items quick toggle with instantaneous sync
│
├── 🛒 Cart & Stock Reservation Engine
│   ├── Dynamic quantity adjustment with boundary checks
│   ├── 5-minute Pessimistic Stock Reservation before checkout
│   └── Automatic inventory rollback on checkout cancellation or expiration
│
├── 💳 Checkout & Financial Accounting
│   ├── Saved Payment Method Vault (Masked Visa/MasterCard)
│   ├── Stripe Payment Intent creation and confirmation
│   ├── Cascading Vietnam Provinces administrative address builder
│   └── Atomic generation of Order, Invoice (HoaDon), and Payment History (LichSuThanhToan)
│
├── 📦 Order Fulfillment & Real-time Tracking
│   ├── 5-stage timeline visualizer (Packing ➔ Picked ➔ In Transit ➔ Delivered ➔ Completed)
│   ├── Ongoing vs. Completed tab management via ViewPager2
│   └── Interactive Rating & Review BottomSheet with dynamic average calculation
│
├── 💬 Real-Time Customer Support
│   ├── Bi-directional live chat with Admin via Firestore Snapshot Listeners
│   ├── Zero-latency UI message bubble generation
│   └── Admin control flags (Spam filter, Star priority, Unread badge tracking)
│
└── 🔔 Notifications & Broadcast Messaging
    ├── Transactional FCM Push Notifications for Order milestones
    └── Admin Global Announcements synchronized into user activity stream
```

---

## ⚡ Getting Started & Installation (Hướng dẫn cài đặt)

### 1. Prerequisites (Yêu cầu môi trường)
*   **Android Studio:** Jellyfish / Koala / Ladybug hoặc mới hơn (JDK 11+).
*   **Node.js:** `v18.x` hoặc `v20.x` LTS.
*   **Firebase Project:** Đã kích hoạt Authentication (Email + Google), Firestore Database, và Cloud Messaging.
*   **Stripe Account:** Kích hoạt chế độ **Test Mode (Sandbox)**.

---

### 2. Backend Setup (Khởi chạy Server Node.js)

1. Mở terminal và chuyển vào thư mục backend:
   ```bash
   cd backend
   npm install
   ```

2. Cấu hình file `backend/.env`:
   ```env
   PORT=3000
   STRIPE_SECRET_KEY=sk_test_51xxxxxxxxxxxxxxxxxxxxxxxxxxxx
   ```

3. Đặt file chứng chỉ Firebase `serviceAccountKey.json` vào thư mục `backend/`.

4. Khởi chạy server backend:
   ```bash
   npm start
   ```

5. *(Tùy chọn khi chạy máy ảo Android / thiết bị thật qua mạng ngoài)* Chạy ngrok để tạo tunnel public:
   ```bash
   ngrok http 3000
   ```

---

### 3. Android Client Setup (Cấu hình Ứng dụng Android)

1. Đặt file `google-services.json` từ Firebase Console vào thư mục `app/`.

2. Cấu hình `local.properties` tại thư mục gốc của dự án:
   ```properties
   STRIPE_PUBLISHABLE_KEY=pk_test_51xxxxxxxxxxxxxxxxxxxxxxxxxxxx
   STRIPE_BACKEND_BASE_URL=https://your-ngrok-domain.ngrok-free.app
   ```

3. Mở dự án trong **Android Studio**, đồng bộ Gradle (`Sync Project with Gradle Files`).

4. Chọn thiết bị giả lập (Emulator API 24+) hoặc thiết bị thật (bật USB Debugging) và nhấn **Run (`Shift + F10`)**.

---

## 🔌 Backend API Reference

| Endpoint | Method | Payload / Params | Mô tả chức năng |
| :--- | :---: | :--- | :--- |
| `/health` | `GET` | `None` | Kiểm tra trạng thái hoạt động của Backend Service |
| `/api/payment-methods/create-card` | `POST` | `{ userId, cardToken, cardHolderName }` | Gắn token thẻ Stripe (`tok_...`) vào Customer Vault và trả về Masked Card Data |
| `/api/payments/create-payment-intent` | `POST` | `{ totalAmount, currency, userId, orderId, paymentMethod }` | Tạo và tự động xác nhận PaymentIntent trên cổng thanh toán Stripe |

---

## 👥 Development Team (Đội ngũ thực hiện)

*   **Phạm Tuấn Hưng** (*Lead Software Engineer & Backend Developer*) - MSSV: `N22DCCN037`
    *   *Trách nhiệm chính:* Thiết kế kiến trúc tổng thể (MVVM + Micro-Backend), xây dựng Node.js Service, xử lý thuật toán Khóa kho (Stock Reservation), cổng thanh toán Stripe API, cơ chế ACID Transactions, hệ thống Chat Real-time và tối ưu hóa Firestore NoSQL Schema.
*   **Nguyễn Tấn Quý** (*Software Engineer*) - MSSV: `N22DCCN066`
    *   *Trách nhiệm chính:* Phát triển giao diện người dùng Material Design 3, tích hợp Vietnam Provinces API, quản lý giỏ hàng, luồng đánh giá sản phẩm và tối ưu hóa trải nghiệm khách hàng.
*   **Hồ Thuận Kiều** (*Software Engineer*) - MSSV: `N22DCCN046`
    *   *Trách nhiệm chính:* Xây dựng phân hệ Tracking đơn hàng, quản lý hồ sơ cá nhân (Cloudinary Avatar Sync), danh mục sản phẩm và xử lý thông báo đẩy FCM.

---

## 📜 License & Portfolio Disclaimer

Dự án được xây dựng phục vụ mục đích nghiên cứu học thuật, trình diễn kỹ năng lập trình phần mềm chuyên sâu (**Software Engineering Portfolio**) và áp dụng các tiêu chuẩn phát triển ứng dụng di động - dịch vụ backend thương mại điện tử thực tế.

---

<div align="center">
  <sub>Built with ❤️ by Pham Tuan Hung and the Engineering Team.</sub>
</div>
