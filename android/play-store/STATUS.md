# Hồ sơ phát hành SayBeer Android

## Bản đã upload

- Tên: SayBeer; miễn phí; danh mục Công cụ; ngôn ngữ mặc định tiếng Việt.
- Package: `com.tem2t.saybeer`.
- Bản nội bộ mới nhất: `1.1.1`, versionCode `4`; Console xác nhận “Đã phát hành cho những người thử nghiệm nội bộ” ngày 28/10/2026 lúc 08:56. Thay thế code `2`.
- Link tham gia: https://play.google.com/apps/internaltest/4701492703751155149
- Bản này chưa được Google xét duyệt; trước khi xét duyệt, Play có thể hiện tên tạm `com.tem2t.saybeer (unreviewed)`.
- AAB đã ký: `artifacts/saybeer-1.1.1-code4-com.tem2t.saybeer.aab` (output local, không đưa vào Git).
- SHA-256: `E9BFD017AAD51ED3BB1FE244CAFCDAE33529A05BB3910CAE60068019CC28050E`.
- Danh sách `SayBeer · Nội bộ` đã lưu 8 email, gồm hai địa chỉ mới do chủ dự án yêu cầu ngày 28/10. Ảnh xác nhận: `evidence/internal-testers-8.png`, `evidence/internal-release-code4-active.png` (local, không đưa vào Git).
- Google Play có một cảnh báo không chặn phát hành: thiếu native debug symbols từ thư viện đóng gói sẵn. Mapping ReTrace đã có trong bundle.

## Hồ sơ đã lưu trên Console

- Trang cửa hàng: tên, mô tả ngắn/đầy đủ, icon 512×512, ảnh đầu trang 1024×500 và 8 ảnh điện thoại tỉ lệ 9:16.
- Chính sách riêng tư: https://saybeer.1labs.space/android-privacy.html (đã kiểm tra công khai và có liên kết trong app).
- Quyền truy cập: không cần tài khoản hoặc trả phí.
- Không quảng cáo; không phải ứng dụng chính phủ, tài chính hay sức khỏe.
- Đối tượng mục tiêu: từ 18 tuổi.
- An toàn dữ liệu: không thu thập hoặc chia sẻ dữ liệu theo định nghĩa Google Play; dữ liệu lưu trên máy và chia sẻ ảnh qua hành động chủ động của người dùng.
- Email hỗ trợ công khai: feedback@1labs.space.
- IARC: Console hiển thị “Đã hoàn tất”, gửi ngày 27/10/2026 lúc 12:44, email duconmang43@gmail.com. Mức hiển thị PEGI 3 / ESRB Everyone; đối tượng mục tiêu của ứng dụng vẫn là 18+. Console hiện chưa hiển thị mã chứng chỉ (dấu `-`); kiểm tra lại trước khi gửi xét duyệt. Bản sao câu trả lời đã khai nằm trong `evidence/iarc-answers.json`.
- Mã nhận dạng quảng cáo: đã lưu “Không”; không dùng SDK quảng cáo hay quyền AD_ID.
- Link nội bộ đã kiểm tra bằng tài khoản được chỉ định: trang hiển thị “You’re a tester” và “Download test app”.

## Ký và cập nhật

Bản giao diện Stitch `1.1.0` / code `3` chỉ được chuẩn bị ở máy. Bản `1.1.1` / code `4` gồm giao diện này và sửa lỗi danh sách Zalo, đã phát hành nội bộ. APK và AAB trong `artifacts/`; source/ảnh tham chiếu ở `../docs/design/`. Cần dùng ảnh chụp và video mới khi gửi xét duyệt.

Upload key nằm trong `.signing/upload.jks`; các file mật khẩu và `signing.properties` nằm cùng thư mục. `.signing/` bị loại khỏi Git. Chủ dự án cần sao lưu riêng thư mục này. Không tạo lại hoặc thay upload key cho bản cập nhật nếu chưa thực hiện quy trình reset key trên Console.

## Đường tới phát hành công khai

Tài khoản này yêu cầu ít nhất 12 người tham gia kiểm thử khép kín liên tục 14 ngày rồi mới xin quyền production. Chủ dự án đã chọn dùng một tài khoản cho thử nghiệm nội bộ trước và bổ sung nhóm 12 người sau. Kiểm thử nội bộ không thay thế điều kiện này.

Console hiện yêu cầu khai báo AccessibilityService. Đã quay video thật trên máy ảo với công bố nổi bật, đồng ý, bật dịch vụ trong Cài đặt, chọn Clock và màn chặn. Video đã ghép/rút đoạn chờ, dài 167,7 giây: `evidence/saybeer-accessibility-review.mp4`. Chủ dự án chọn đăng YouTube không công khai và đã đồng ý điều khoản tải lên; đang chờ chủ tài khoản hoàn tất xác minh danh tính của YouTube trước khi upload. Chưa nộp khai báo quyền khi chưa có URL video. Bản mô tả quyền ở `accessibility-declaration.md`; không khai SayBeer là công cụ hỗ trợ người khuyết tật. Kiểm thử trên điện thoại thật vẫn cần thiết.

Nguồn: [Yêu cầu kiểm thử tài khoản cá nhân](https://support.google.com/googleplay/android-developer/answer/14151465), [AccessibilityService API](https://support.google.com/googleplay/android-developer/answer/10964491).
