# Tech-Store-Mobile Application

Tech-Store-Mobile là dự án ứng dụng thương mại điện tử trên nền tảng Android, được thiết kế để mang đến trải nghiệm mua sắm mượt mà, bảo mật và hiện đại cho người dùng. Dự án tập trung vào việc quản lý sản phẩm, giỏ hàng, thanh toán trực tuyến và hệ thống hỗ trợ khách hàng thời gian thực.

## 👥 Đội ngũ phát triển
*   **Phạm Tuấn Hưng** (Trưởng nhóm)
*   **Nguyễn Tấn Quý**
*   **Hồ Thuận Kiều**

---

## 🚀 Công nghệ & Kiến trúc
Dự án được xây dựng dựa trên các tiêu chuẩn hiện đại của Android development:
*   **Ngôn ngữ**: Java.
*   **Kiến trúc**: MVVM (Model-View-ViewModel) đảm bảo code dễ bảo trì và mở rộng.
*   **Backend & Database**: Firebase (Firestore, Authentication, Storage, Cloud Messaging).
*   **Thanh toán**: Tích hợp Stripe API (Payment Intents, Webhooks) để đảm bảo an toàn giao dịch.
*   **UI/UX**: Material Design 3, sử dụng Glide để tối ưu hóa việc tải hình ảnh.
*   **Real-time**: Kết nối Firestore Real-time để thực hiện tính năng chat hỗ trợ khách hàng ngay lập tức.

---

## ✨ Các tính năng chính
*   **Quản lý tài khoản**: Đăng nhập/Đăng ký qua Email và Google Auth.
*   **Tìm kiếm & Lọc**: Hệ thống tìm kiếm nâng cao dựa trên từ khóa và danh mục sản phẩm.
*   **Giỏ hàng thông minh**: Quản lý trạng thái giỏ hàng đồng bộ trên mọi thiết bị.
*   **Thanh toán bảo mật**: Tích hợp cổng thanh toán Stripe với tính năng lưu trữ phương thức thanh toán an toàn.
*   **Hỗ trợ trực tuyến**: Hệ thống Chat thời gian thực (Real-time) giữa App Android và Web Admin, hỗ trợ đánh dấu tin nhắn quan trọng, quản lý spam, và trạng thái xóa.
*   **Thông báo**: Hệ thống thông báo đẩy (FCM) cập nhật trạng thái đơn hàng và tin nhắn từ admin.

---

## 🛠 Giải quyết vấn đề kỹ thuật (Highlights)
*   **Tối ưu dữ liệu**: Thiết kế Database NoSQL tối ưu cho Firestore, giảm thiểu số lần truy vấn (Read/Write) để tiết kiệm chi phí và tăng tốc độ phản hồi của ứng dụng.
*   **Xử lý bất đồng bộ**: Sử dụng hiệu quả `WriteBatch` của Firebase để đảm bảo tính toàn vẹn dữ liệu khi cập nhật giỏ hàng và thực hiện giao dịch thanh toán.
*   **Tính sẵn sàng**: Xây dựng cơ chế Chat 2 chiều, cho phép Admin từ Web Admin quản lý danh sách khách hàng, đánh dấu tin nhắn quan trọng/spam và trả lời trực tiếp đến App người dùng.
*   **Thanh toán**: Triển khai luồng thanh toán Stripe an toàn, xử lý các kịch bản lỗi thanh toán và lưu trữ thông tin thẻ an toàn thông qua mã hóa Token.

---

## 📦 Hướng dẫn cài đặt
1. Clone dự án: `git clone [URL_CUA_BAN]`
2. Cấu hình file `google-services.json` vào thư mục `app/`.
3. Thiết lập biến môi trường Stripe trong file `.env` phía backend.
4. Build dự án bằng Android Studio (Gradle).

---
*Dự án là sản phẩm thực nghiệm, phục vụ mục đích nghiên cứu và triển khai ứng dụng TMĐT thực tế.*
