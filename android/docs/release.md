# Phát hành Android

## Danh tính và bản build

- Application ID và namespace: `com.tem2t.saybeer`, theo lựa chọn của chủ dự án.
- Bản nội bộ đầu tiên: `1.0.0` / code `2`; tăng versionCode cho mỗi lần upload tiếp theo. Code `1` chỉ là bundle chuẩn bị và không được dùng trong bản nội bộ cuối.
- Bản thiết kế lại theo Stitch: `1.1.0` / code `3`, đã chuẩn bị ở máy. Cần cập nhật ảnh cửa hàng/video theo giao diện mới trước khi gửi bản này xét duyệt.
- Bản nội bộ hiện tại: `1.1.1` / code `4`, phát hành ngày 28/10/2026 lúc 08:56; gồm giao diện Stitch và sửa danh sách app có chức năng gọi như Zalo. Code `3` không upload. Cần làm mới video khai báo quyền trước khi gửi xét duyệt.
- `assembleRelease` và `bundleRelease` tự ký bằng cấu hình máy trong `.signing/signing.properties` nếu có. Khi clone trên máy khác chưa có cấu hình này, build release chưa ký.
- Upload key được tạo riêng cho SayBeer tại `.signing/upload.jks`; mật khẩu và cấu hình đều nằm trong `.signing/`, bị loại khỏi Git. Chủ dự án cần sao lưu riêng thư mục này vào nơi an toàn để có thể upload các bản cập nhật. Không đưa private key hoặc mật khẩu lên Git hay trang cửa hàng.

## Khai báo quyền

App dùng Accessibility API cho chức năng khóa app do người dùng tự chọn. App **không** khai là công cụ trợ năng cho người khuyết tật (`isAccessibilityTool=false`).

Trong Play Console, khai đúng việc nhận tên package foreground và hiển thị lớp phủ. Chuẩn bị video thể hiện: onboarding → disclosure → chủ động cấp quyền trong Cài đặt → chọn app → bắt đầu phiên → màn chắn → tự mở lại / khẩn cấp. Không tuyên bố đọc tin nhắn, quản lý trẻ em hoặc chống gỡ app.

Không yêu cầu QUERY_ALL_PACKAGES, Usage Access, Device Admin, quyền vẽ trên app khác hay Internet. Truy vấn app giới hạn vào launcher bằng `<queries>`. Chỉ Điện thoại mặc định được bảo vệ, các app nhắn tin có chức năng gọi vẫn có thể được chọn.

Quyền `SCHEDULE_EXACT_ALARM` là quyền người dùng tự cấp; kiểm tra trước khi dùng và có fallback. Không dùng `USE_EXACT_ALARM`.

## Dữ liệu và quyền riêng tư

- Danh sách package đã chọn, cài đặt, phiên và lịch sử lưu riêng trên máy.
- Không thu thập nội dung màn hình, thông báo, tin nhắn, mật khẩu hay phím gõ.
- Không gửi dữ liệu tới máy chủ. Ảnh thành tích chỉ được chuyển tới app chia sẻ do người dùng chọn qua URI cấp quyền đọc tạm.
- Backup đám mây và device transfer đã tắt. Gỡ app xóa lịch sử.
- Chính sách Android đã được chủ dự án đưa lên `https://saybeer.1labs.space/android-privacy.html`; source ở `play-store/privacy.html`. Ứng dụng có liên kết tới trang này trong mục Quyền & riêng tư.

## Các bước trước khi submit

1. Chạy unit tests, lint, instrumentation và kiểm tra trên thiết bị thật của ít nhất hai hãng.
2. Thử khóa màn hình, cuộc gọi, reboot, Doze, thu hồi quyền, đổi múi giờ và phiên qua đêm theo `verification.md`.
3. Ký AAB, thiết lập listing, ảnh chụp, Data Safety và khai báo Accessibility trong Play Console bằng tài khoản chủ dự án.
4. Chạy internal testing và kiểm tra Pre-launch report trước production.

Tài liệu nền tảng: [AccessibilityService](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService), [Alarms](https://developer.android.com/develop/background-work/services/alarms), [Compose compiler setup](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler).
