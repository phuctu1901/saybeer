# Chuẩn bị Android để duyệt trong repository gốc

## Phạm vi thay đổi

- Bổ sung bản Android 1.1.1 / code 4 vào `android/`, package `com.tem2t.saybeer`.
- Giữ nguyên source iOS và cấu trúc thư mục hiện có ở root.
- Thêm README ở root và workflow `.github/workflows/android.yml` chạy từ `android/`.
- CI chỉ định Ubuntu 24.04 và các gói SDK cần thiết; không dùng mặc định cài gói `tools` cũ đã bị Google gỡ khỏi repository SDK.
- Không đưa nested Git repository, khóa ký, mật khẩu, cấu hình máy, email tester hoặc APK/AAB phát hành vào Git.
- Source ứng dụng giữ nguyên bản nội bộ; các đề xuất trong PRODUCT-REVIEW-2026-10-28.md chưa được triển khai trong PR này.

## Kiểm tra checkout sạch

- Gradle offline: `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease` — BUILD SUCCESSFUL, 135 task được thực thi.
- Unit tests: 16, không lỗi hoặc bỏ qua.
- Lint debug: 0 errors, 8 warnings tại thời điểm kiểm tra.
- Checkout sạch không có upload key. Release APK/AAB chỉ dùng để kiểm tra build, chưa ký để phát hành; debug APK dùng khóa debug của môi trường build.
- Instrumentation: **OK (13 tests)**, 222,02 giây trên emulator API 36. Gồm màn chắn thật, app có ACTION_DIAL, các luồng Compose, onboarding và chụp ảnh. Báo cáo: [instrumentation-repository-review.txt](design/instrumentation-repository-review.txt).
- Lượt đầu có timeout ở bài khẩn cấp; lượt chạy riêng gặp timeout chụp ảnh. Đã tách chụp ảnh khỏi các bài kiểm thử logic (giữ test chụp ảnh chuyên biệt), điều khiển countdown bằng MainTestClock và xác nhận chưa mở ở 4 giây, mở sau mốc 5 giây. Bộ đầy đủ sau chỉnh fixture đã qua cả 13 bài; không thay code ứng dụng để làm test qua.

## Điểm cần reviewer chú ý

- App tự cam kết dùng Accessibility overlay, không đình chỉ app ở cấp hệ điều hành, không ngăn tắt quyền hoặc gỡ app.
- Xung đột ứng dụng ngân hàng còn tồn tại; phân phối qua Play không bảo đảm ngân hàng chấp nhận.
- Thống kê hiện dựa thời gian phiên, chưa đo độ bao phủ bảo vệ thực tế khi quyền bị tắt. Notification cũng chưa theo dõi đầy đủ kết nối service.
- Chính sách thời gian dựa đồng hồ thiết bị và phục hồi bài thử thách cần cải thiện theo bản rà soát sản phẩm.
- Tình huống lỗi cài APK trên điện thoại thật chưa có mã lỗi chi tiết, chưa kết luận nguyên nhân.
- Chưa có backend, ghép đôi thiết bị hoặc gửi xin phép.
- Cần hoàn tất khai báo Trợ năng/video đúng giao diện, kiểm thử thiết bị thật và quy trình closed testing trước production. Trạng thái ghi trong play-store/STATUS.md là hồ sơ lịch sử, không phải truy vấn Console trực tiếp.

## Quy trình duyệt

Đề nghị duyệt và merge qua pull request vào `main`. Việc push nhánh đưa code lên repository để xem xét; chưa thay đổi nội dung `main`. Draft PR không làm code ở nhánh trở thành riêng tư. Không tự merge và không bật auto-merge.
