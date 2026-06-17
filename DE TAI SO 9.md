|<p>**HỌC VIỆN CÔNG NGHỆ BƯU CHÍNH VIỄN THÔNG**</p><p>**KHOA: CÔNG NGHỆ THÔNG TIN 2**</p><p></p>|||
| :-: | :- | :- |
|<p>Học phần: **Phát triển ứng dụng cho các thiết bị di động**</p><p>Trình độ đào tạo: **Đại học**    </p><p>Hình thức đào tạo: **Chính quy**  </p>| ||

**THÔNG TIN ĐỀ TÀI DỰ ÁN**

**ĐỀ TÀI SỐ 9**

**1. Tên đề tài:** Xây dựng ứng dụng thương mại điện tử

**2. Số lượng sinh viên:** 3 sinh viên
* **Phạm Tuấn Hưng** - MSSV: N22DCCN037 (Trưởng nhóm)
* **Hồ Thuận Kiều** - MSSV: N22DCCN046 (Thành viên)
* **Nguyễn Tấn Quý** - MSSV: N22DCCN066 (Thành viên)

**3. Mô tả đề tài**

Các yêu cầu chính của đề tài:

**Phân hệ Xác thực và Bảo mật**

Tại màn hình đăng ký và đăng nhập, ứng dụng tích hợp thư viện Google Sign-In để tối giản thao tác cho người dùng. Đối với việc đăng nhập truyền thống, hệ thống áp dụng cơ chế xác thực thông qua Firebase Authentication. Mật khẩu của người dùng không bao giờ được lưu trữ dưới dạng văn bản thuần túy (Plain text); thay vào đó, hệ thống tự động mã hóa một chiều (One-way Hash) bằng các thuật toán bảo mật tiên tiến (Scrypt) trước khi lưu trữ lên hệ thống Cloud. Mọi dữ liệu truyền tải giữa thiết bị và Server đều được mã hóa bằng giao thức SSL/TLS, đảm bảo an toàn tuyệt đối ngay cả khi dữ liệu bị đánh chặn. Ngoài ra, ứng dụng còn tích hợp trình xác thực dữ liệu (Input Validation) thời gian thực, yêu cầu mật khẩu phải đạt độ dài tối thiểu và định dạng email @gmail.com hợp lệ.

**Phân hệ Quản lý Mua sắm (Dashboard)**

Giao diện chính (Dashboard) được thiết kế với triết lý cung cấp thông tin quan trọng ngay lập tức. Danh sách sản phẩm được hiển thị thông qua RecyclerView với khả năng cuộn vô tận (Infinite scroll) và tải dữ liệu bất đồng bộ mượt mà. Nhóm đã cài đặt hệ thống tìm kiếm nâng cao (Advanced Search) cho phép tìm kiếm theo từ khóa và gợi ý kết quả. Tại màn hình "Yêu thích" (Saved Items), người dùng có thể dễ dàng quản lý danh sách bằng cách nhấn vào biểu tượng trái tim để thêm hoặc xóa nhanh sản phẩm ra khỏi danh sách lưu trữ, mang lại trải nghiệm tiện lợi và mượt mà.

**Phân hệ Thanh toán và Đơn hàng**

Đây là tính năng thể hiện rõ khả năng xử lý nghiệp vụ phức tạp của ứng dụng. Nhóm tích hợp cổng thanh toán trực tuyến Stripe API cho phép lưu trữ và quản lý thẻ tín dụng an toàn qua cơ chế Stripe Tokenization (mã hóa thông tin thẻ thành Token). Về mặt xử lý logic, hệ thống sử dụng thuật toán WriteBatch của Firebase để đảm bảo tính toàn vẹn dữ liệu khi cập nhật giỏ hàng và thực hiện giao dịch thanh toán đồng thời. Người dùng có thể theo dõi toàn bộ lộ trình đơn hàng từ lúc đặt cho đến khi hoàn thành thông qua hệ thống thông báo đẩy (FCM).

**Phân hệ Hỗ trợ Khách hàng (Real-time Chat)**

Tính năng hỗ trợ trực tuyến là một điểm nhấn công nghệ của dự án. Nhóm sử dụng thư viện Firestore Real-time để xây dựng kênh giao tiếp tức thì giữa người dùng và Admin. Hệ thống chat cho phép gửi tin nhắn văn bản, hiển thị trạng thái đã xem, và đồng bộ hóa tin nhắn ngay lập tức trên mọi thiết bị. Phía Web Admin dành cho người bán cũng được tích hợp đồng bộ, cho phép quản lý danh sách khách hàng cần hỗ trợ, đánh dấu tin nhắn ưu tiên và xử lý spam hiệu quả.

**Phân hệ Quản lý Tìm kiếm và Danh mục**

Project Tech Store Mobile trao quyền chủ động cho người dùng thông qua việc lọc sản phẩm theo từng danh mục riêng biệt (Laptop, Mobile, Audio, Smartwatch...). Nhóm đã xây dựng bộ sưu tập Icon và phối màu đồng bộ cho từng loại thiết bị, tạo nên sự nhất quán về mặt thị giác (Visual Consistency). Mỗi khi chọn một danh mục, hệ thống sẽ thực hiện thuật toán lọc dữ liệu động, giúp hiển thị chính xác các sản phẩm tương ứng, tối ưu hóa thời gian mua sắm của khách hàng.

**Phân hệ Giỏ hàng và Kiểm soát Kho (Smart Checkout)**

Chức năng này đóng vai trò giám sát tài chính và hàng hóa trong quá trình mua sắm. Người dùng có thể thiết lập số lượng tối đa cho từng mặt hàng. Hệ thống tích hợp thuật toán kiểm tra hàng tồn kho thời gian thực mỗi khi người dùng mở màn hình Checkout. Nếu số lượng hàng trong giỏ vượt quá lượng hàng khả dụng trong kho, hệ thống sẽ tự động hiển thị cảnh báo đỏ và khóa tính năng thanh toán để tránh xảy ra lỗi "vỡ kế hoạch" khi xử lý đơn hàng thực tế.

**Phân hệ Quản lý Hồ sơ cá nhân**

Người dùng có thể cập nhật thông tin cá nhân, địa chỉ nhận hàng và thay đổi ảnh đại diện (Avatar). Ảnh đại diện được lưu trữ trên Server Cloudinary và được tải về hiển thị mượt mà thông qua thư viện CircleImageView kết hợp với Glide, giúp tối ưu dung lượng bộ nhớ và tăng tốc độ trải nghiệm.

**Phân hệ Chuẩn hóa Ngôn ngữ (Localization-ready)**

Ứng dụng chuẩn hóa ngôn ngữ giao diện tiếng Anh (English) làm ngôn ngữ chính thức (phù hợp định hướng cửa hàng công nghệ quốc tế). Đồng thời, toàn bộ chuỗi ký tự hiển thị được quản lý tập trung trong file tài nguyên `strings.xml`, tạo điều kiện tối ưu cho mã nguồn sạch và giúp hệ thống sẵn sàng dịch thuật/bản địa hóa (Localization) sang các ngôn ngữ khác như tiếng Việt khi cần mở rộng.

**Tài khoản người dùng:**

•	Đăng ký, đăng nhập, cập nhật thông tin cá nhân, quản lý danh sách địa chỉ nhận hàng và thiết lập địa chỉ mặc định (tích hợp API Tỉnh/Thành Việt Nam).

**Giao diện người dùng:**

•	Giao diện thiết kế theo phong cách Material Design 3 chuyên nghiệp.

•	Hỗ trợ đầy đủ Light/Dark mode thích ứng với môi trường ánh sáng.



