# Kiểm chứng bản Android

## Checkout chuẩn bị pull request

- Build từ checkout sạch trong repository gốc: debug APK, test APK, release APK/AAB, 16 unit tests và lint đều thành công. Release checkout không có khóa ký phát hành.
- Lint debug: 0 errors, 8 warnings tại lần kiểm tra repository.
- Bộ instrumentation cuối: **OK (13 tests)** trong 222,02 giây, API 36. Xem [báo cáo](design/instrumentation-repository-review.txt) và [ghi chú kiểm chứng repository](REPOSITORY-REVIEW.md).
- Fixture khẩn cấp dùng đồng hồ Compose để kiểm tra countdown; các bài flow không còn phụ thuộc chụp ảnh. Test chụp ảnh chuyên biệt và test màn chắn thật vẫn được chạy.

## Sửa danh sách ứng dụng — 28/10/2026

- Version `1.1.1` / code `4`, package `com.tem2t.saybeer`.
- Build offline: unit tests, lint, debug APK/test APK, release APK và AAB đều thành công.
- **16 unit tests**, **13 instrumentation tests**: không lỗi hoặc bỏ qua. Máy ảo Pixel 9 Pro / API 36, bộ instrumentation hoàn tất trong 57,449 giây. Báo cáo: [instrumentation-code4.txt](design/instrumentation-code4.txt).
- Lint: **0 errors, 15 warnings**. AAB ký bằng upload key hiện có; `jarsigner` xác nhận chữ ký hợp lệ.
- Sửa nguyên nhân app nhắn tin bị ẩn: không bảo vệ toàn bộ app nhận ACTION_DIAL, chỉ bảo vệ Điện thoại mặc định qua TelecomManager cùng các package hệ thống cần thiết. Test APK có launcher nhận ACTION_DIAL để kiểm tra hồi quy; fixture không nằm trong bản phát hành.
- Picker tự tải lại danh sách khi quay lại và giải thích giới hạn hồ sơ Android. Chưa kiểm chứng trực tiếp với Zalo cài trên điện thoại thật.
- AAB SHA-256: `E9BFD017AAD51ED3BB1FE244CAFCDAE33529A05BB3910CAE60068019CC28050E`.
- Play Console xác nhận code `4` đã phát hành nội bộ ngày 28/10/2026 lúc 08:56; một cảnh báo thiếu native debug symbols, không có lỗi chặn phát hành. Danh sách nội bộ đã lưu 8 email.
- Chủ dự án báo Play Protect chặn APK cài ngoài. Bản này dùng kênh Play nội bộ để phân phối; chưa xác nhận cảnh báo trên máy thật đã hết. Không tắt hoặc né Play Protect. Nếu bị phân loại nhầm, dùng quy trình khiếu nại chính thức của Google.

## Bản thiết kế Stitch — 28/10/2026

- Package `com.tem2t.saybeer`, version `1.1.0`, code `3`.
- `testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest bundleRelease assembleRelease --offline`: **BUILD SUCCESSFUL**.
- **13 unit tests**: 0 lỗi, 0 bỏ qua.
- **12 instrumentation tests**: **OK (12 tests)**, chạy đủ bộ cuối trong 68,115 giây trên Pixel 9 Pro / API 36. Báo cáo: [instrumentation-code3.txt](design/instrumentation-code3.txt).
- Gồm màn chắn thật, cửa sổ nhắn việc/chờ 15 giây, ba lần sai/chờ, mở khẩn cấp, lịch tuần, picker giữ bản nháp, khôi phục state, thống kê/chia sẻ, onboarding/đóng công bố quyền và chụp 8 màn hình.
- Lint: **0 errors, 16 warnings**. APK release và AAB bật R8, ký bằng upload key hiện có.
- AAB SHA-256: `E1BD46F7F63E72812994FA8CB379B4B373BE3E18FB655563E3D740FDAE3A121A`.
- Ảnh mới: [design/screenshots/store](design/screenshots/store), [popup và các luồng](design/screenshots/flows). Tham chiếu gốc: [STITCH-REDESIGN.md](design/STITCH-REDESIGN.md).
- Đã khắc phục fixture Trợ năng của test bị treo sau instrumentation force-stop process: chỉ rebind service của package hiện tại, giữ nguyên và phục hồi các dịch vụ khác. Onboarding test xác nhận dismiss quay lại Home mà không phụ thuộc việc thiết bị đã bật quyền từ trước.
- Tại thời điểm kiểm tra code `3`, bản này chỉ được chuẩn bị ở máy. Giao diện sau đó được đưa vào code `4` và phát hành nội bộ như ghi ở trên. Cần làm mới video khai báo quyền trước khi gửi xét duyệt.

## Bản phát hành trước

Ngày kiểm tra: **27/10/2026**, múi giờ Asia/Saigon.

## Kết quả

- Clean build: `clean testDebugUnitTest lintDebug assembleDebug assembleDebugAndroidTest assembleRelease bundleRelease` → **BUILD SUCCESSFUL**.
- **13 unit tests**: 0 lỗi, 0 bỏ qua.
- **12 kịch bản instrumentation** trên **Pixel 9 Pro emulator, Android 16 / API 36**: lượt kiểm tra toàn bộ trên package `com.tem2t.saybeer`, code `2`, đã qua cả 12 test, không lỗi hoặc bỏ qua. Bao gồm 11 kịch bản chức năng và một luồng chụp 8 màn thật cho Play Store.
- Android Lint: **0 errors, 16 warnings**. Cảnh báo còn lại chủ yếu là bản dependency mới hơn và khuyến nghị Kotlin KTX; không tắt kiểm tra hay thêm lint baseline để bỏ qua lỗi.
- Release APK với R8 và AAB đã tạo, ký thành công bằng upload key riêng của SayBeer. Google Play đã nhận bundle code `2` và kênh thử nghiệm nội bộ đã hoạt động.
- Repo iOS không có thay đổi.

## Unit tests

`SessionEngineTest` kiểm tra:

1. Không bắt đầu khi danh sách app rỗng.
2. Chỉ khóa app đã chọn, tự mở ở deadline.
3. Cửa sổ nhắn việc mở tạm và tự khóa lại đúng mốc, không mở chồng.
4. Tối đa 3 lần và cắt cửa sổ ở giờ kết thúc phiên.
5. Sai 3 lần chờ 5 phút; không tự kéo dài thời gian chờ.
6. Đóng app lâu rồi mở lại ghi đúng giờ kết thúc dự kiến, không ghi lịch sử trùng.
7. Kết thúc sớm giữ lý do và giới hạn 200 bản ghi.
8. Lịch qua đêm; phiên mở sớm không bị lịch khởi động lại.
9. Lịch không kéo dài / thay thế phiên đang chạy.
10. Ngày ISO và lần chạy tuần kế tiếp.
11. Xử lý thời gian DST bị thiếu tại New York.
12. Unicode NFC, dấu tiếng Việt và chuẩn hóa câu cam kết.
13. Thống kê phiên an toàn và chuỗi liên tiếp.

## Kiểm thử trên emulator

- Onboarding giải thích và xin đồng ý trước khi mở Cài đặt.
- Bắt đầu phiên, tạo lại repository vẫn giữ deadline.
- Gõ cam kết đúng và chờ đủ trước khi mở cửa sổ.
- Bài kiểm tra sai 3 lần chuyển sang màn chờ.
- Nhả nút khẩn cấp hủy thao tác; countdown hỗ trợ tiếp cận kết thúc sau 5 giây.
- Lịch tuần lưu nhiều ngày và khung giờ qua đêm.
- Thống kê hiển thị và ảnh chia sẻ đọc được qua FileProvider, đúng 1080×1350.
- Màn home phiên đang chạy và đường vào khẩn cấp.
- Chọn app rồi quay lại vẫn giữ bản nháp lịch.
- State restoration giữ câu cam kết và deadline chờ.
- **Dịch vụ Trợ năng thật**: mở Chrome đã chọn → thấy màn chắn → mở cửa sổ → màn chắn biến mất → hết cửa sổ → màn chắn xuất hiện lại → kết thúc phiên → màn chắn biến mất. Test phục hồi thiết lập Trợ năng ban đầu của emulator.

Ảnh chụp nằm trong [screenshots](screenshots). Các màn biểu mẫu được chụp từ Compose test; home và màn chắn được chụp trong luồng tích hợp. Dữ liệu thống kê là fixture kiểm thử. Home trong ảnh hiển thị banner chưa cấp quyền vì test đã phục hồi quyền của emulator; đây là trạng thái có chủ ý.

## Chưa chứng minh bằng thiết bị thật

Trước khi phát hành, kiểm tra các điểm phụ thuộc hệ điều hành / hãng máy:

- Samsung, Xiaomi / Oppo: Trợ năng và giới hạn pin nền.
- Android 8, 12, 13, 14, 15; hiện đã chạy thực tế trên emulator API 36.
- Phiên qua đêm thực, reboot trong phiên / cửa sổ nhắn việc, Doze dài.
- Đổi múi giờ / chỉnh đồng hồ thủ công, cấp / thu hồi exact alarm trong lúc dùng.
- Cuộc gọi đến, màn hình khóa, đa cửa sổ, xoay ngang, font hệ thống lớn và TalkBack trên máy thật.
- Android Sharesheet gửi ảnh tới app nhắn tin thật; kiểm thử tự động hiện xác nhận tạo và đọc PNG qua URI.

Không coi kết quả emulator là cam kết app không thể bị né khóa. Người dùng vẫn kiểm soát quyền và có thể buộc dừng / gỡ app.
