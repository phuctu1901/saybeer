# Rà soát sản phẩm SayBeer Android — 28/10/2026

Phạm vi: source 1.1.1/code 4, các test hiện có, ảnh chụp Compose lưu trong docs/design/screenshots/store và hồ sơ phát hành local. Không thay đổi mã ứng dụng, không chạy lại bộ test, không kiểm tra trực tiếp thiết bị thật hay trạng thái Console trong lần rà soát này. Phân biệt phát hiện từ source với hành vi cần tái hiện trên máy.

## Ưu tiên trước đợt kiểm thử rộng

| ID | Phát hiện | Việc cần làm và tiêu chí hoàn tất |
|---|---|---|
| P1-01 | Home vẫn ghi “Đang khóa N app” khi quyền tắt, dù có banner cảnh báo; notification chỉ xem session và có thể tiếp tục ghi “đang bảo vệ”. Kiểm tra enabled không chứng minh service đang kết nối. | Tách trạng thái phiên và trạng thái bảo vệ: chưa chạy, đang bảo vệ, mở tạm, mất kết nối/quyền, kết thúc. Home, notification, shield và thống kê dùng cùng nguồn trạng thái; không khẳng định đang bảo vệ khi không có service kết nối. |
| P1-02 | Record.survived chỉ dựa reason done/auto; hours cộng toàn bộ thời gian phiên. Tắt Trợ năng cả phiên vẫn có thể được ghi trọn phiên; 15 phút mở tạm vẫn được tính vào “Tổng giờ khóa”. | Tách thời gian phiên, thời gian mở tạm và khoảng gián đoạn đã quan sát. Đổi tên thành “Trọn thời gian đã hẹn” khi chưa có bằng chứng bảo vệ liên tục; không suy diễn khoảng mất process là đã bảo vệ. Dữ liệu cũ đánh dấu chưa đo. |
| P1-03 | Ngân hàng có thể yêu cầu tắt Trợ năng; hiện chưa có luồng tạm dừng rõ ràng. | Nút tạm dừng có giải thích, gọi disableSelf khi người dùng chọn, giữ deadline và ghi gián đoạn. Bật lại qua Cài đặt. Nêu rõ ảnh hưởng tới lịch tự động; không hứa tự bật lại. Kiểm thử TPBank thật trước/sau tắt quyền và sau khởi động lại ngân hàng. |
| P1-04 | Chọn “6h sáng” trong phút cuối trước 06:00: UI cho phép bấm nhưng SessionEngine.start từ chối end <= now + 1 phút, không có phản hồi. Đây là suy luận trực tiếp từ hai điều kiện trong source, chưa tái hiện UI. | Chốt quy tắc thời lượng tối thiểu cho mọi preset, kiểm tra cùng nguồn tại UI/engine; trả kết quả có lý do lỗi. Không tự chuyển sang ngày mai mà thiếu giải thích. Test 05:58:59, 05:59:30 và 06:00. |
| P1-05 | Các khoảng 15 giây, 90 giây, 5 phút và phiên +4/+8h dựa wall clock; đổi đồng hồ làm thay đổi thời gian chờ. | Dùng elapsedRealtime cho thời lượng trong cùng lần boot; lưu mốc UTC cho lịch và phục hồi reboot. Quy định rõ thay đổi giờ/múi giờ, test tiến/lùi đồng hồ. Không tuyên bố chống mọi cách né khóa. |
| P1-06 | Deadline/câu hỏi của bài kiểm tra nằm ở rememberSaveable; rời rồi vào lại màn tạo lượt mới. Hết giờ khi màn không còn tồn tại không được engine xử lý. | Quy định đóng màn là hủy hay thất bại. Nếu 90 giây là giới hạn lượt thực, lưu attempt ID/deadline vào repository, gắn với session ID; engine quyết định hết giờ và chỉ ghi thất bại một lần. |
| P1-07 | JSON hỏng khiến repository ném exception lúc khởi tạo; commit thất bại cũng ném exception. Có thể khiến app không mở được trong lúc cần kết thúc phiên. | Màn phục hồi dữ liệu có lựa chọn rõ ràng; giữ bản gốc, báo lỗi lưu, không âm thầm reset. Có lối dẫn tới Cài đặt tắt dịch vụ nếu không phục hồi được. Có migration theo version, validation và test dữ liệu thiếu/hỏng/phiên bản mới. |
| P1-08 | Bộ test đã có không thay thế kiểm tra bản release ký thật trên thiết bị dùng ngân hàng. Lỗi “App not installed” của chủ dự án chưa có mã cài đặt chi tiết. | Test cài mới/cập nhật qua Play, phân biệt upload key và app signing key; lấy lỗi package installer/ADB trên thiết bị được người dùng cho phép. Không suy đoán lỗi chữ ký là kết luận. Không yêu cầu gỡ app mất dữ liệu trước khi chẩn đoán. |

## UI/UX nên hoàn thiện tiếp

1. Home: thêm thẻ trạng thái bảo vệ, icon/tên app đã chọn, lịch kế tiếp có ngày giờ và trạng thái quyền. Giữ bảng màu Amber và CTA lớn; giảm khoảng trống trang trí trên màn nhỏ.
2. Trước khi bật phiên: tóm tắt số app, thời điểm kết thúc có ngày, quyền đang thiếu và phạm vi mở tạm. Cho chọn preset quen dùng; các thông số chính hiển thị nhất quán.
3. Đổi “Kiểm tra tỉnh táo” thành “Thử thách mở sớm”, “Tỉnh rồi” thành “Mở khóa sớm”. Giữ thông báo không đo nồng độ cồn; không gọi kết quả là xác nhận tỉnh táo.
4. Cửa sổ nhắn việc: hiện rõ hiện tại mở TOÀN BỘ app đã chọn. Nên bổ sung mở một app do người dùng chọn và nút “Khóa lại ngay” để không phải chờ đủ 15 phút. Hiển thị thời gian thực nếu phiên còn dưới 15 phút.
5. App picker: ưu tiên app đã chọn, tìm không dấu, có mục đã chọn và nút bỏ chọn; ẩn package kỹ thuật khỏi hàng mặc định. Dùng selectable/toggleable semantics để TalkBack đọc trạng thái chọn. Phân biệt “không còn hiển thị trong hồ sơ” với khẳng định app đã bị gỡ.
6. Trạng thái nút bị vô hiệu hóa: giải thích ngay lý do như chưa chọn app, thiếu quyền, đủ 3 lần; cho người dùng đi tới bước khắc phục.
7. Quyền: cho xem và cấu hình app trước khi bắt buộc bật Trợ năng. Chỉ xin quyền cần cho tác vụ đang làm; quyền exact alarm ưu tiên ở lịch. Có màn hướng dẫn theo hãng và quay lại kiểm tra kết nối dịch vụ thật.
8. Khẩn cấp: giữ dễ tìm, không phạt thống kê/mỉa mai người dùng; dùng phản hồi rung và tiến trình rõ. Kiểm thử màn nhỏ, phông lớn, TalkBack và màn chắn thật. Không thêm tính năng gây cản trở khẩn cấp.
9. Lịch: hiển thị “lần tới: Thứ Sáu 18:00 → Thứ Bảy 06:00”, bỏ qua một lần, nhắc trước giờ chạy; giải thích rõ khi tắt Trợ năng thì lịch chỉ có thể lưu/chạy trạng thái chứ không bảo vệ được.
10. Typography: rà các nhãn 11–12sp, phông 150–200%, màn ngang/320dp và bàn phím. Giữ vùng chạm ít nhất 48dp. Màn chắn native hiện dùng layout cố định, cần kiểm tra riêng vì không dùng cùng layout Compose.

## Logic và kiến trúc bổ sung

- Snapshot danh sách app theo session và cấp session ID; không để thao tác cũ áp dụng nhầm phiên kế tiếp.
- Tập trung điều kiện mở tạm/mở sớm tại domain layer; UI chỉ gửi intent. Hiện openWindow không kiểm tra lockout: cần quyết định đây là ngoại lệ có chủ ý và giải thích cho người dùng.
- Ngừng ticker mỗi giây khi không có phiên cần xử lý; dùng alarm/deadline và lifecycle. Kiểm tra pin/Doze trước khi kết luận mức tiêu thụ.
- Khi service kết nối lại, foreground ban đầu rỗng và safePackages chỉ lấy một lần. Test khi app bị khóa đang ở trước màn hình, khi đổi launcher/Phone mặc định, reboot và reconnect; không giả định sẽ luôn có event mới.
- Không nhận thêm loại sự kiện/nội dung Trợ năng. Nghiên cứu giới hạn packageNames nhưng phải chứng minh vẫn gỡ màn chắn đúng khi chuyển sang app khác, Cài đặt và màn hình chính.
- Thêm chẩn đoán cục bộ: phiên bản, model/Android, kết nối service, quyền báo thức/thông báo, lần alarm gần nhất. Xuất theo hành động người dùng, cho xem trước, không gồm tin nhắn/thông tin ngân hàng; không tự gửi telemetry.
- Thêm xóa lịch sử/xóa dữ liệu qua xác nhận rõ. Bản xuất/sao lưu thủ công có thể cân nhắc sau, không làm thay đổi âm thầm cam kết riêng tư hiện tại.

## Tính năng sau khi phần lõi ổn định

- Ghép đôi với một thiết bị: đồng ý ở cả hai máy, QR/mã ngắn có hạn, thu hồi ghép đôi và quy trình đổi/mất máy.
- Xin phép: ID yêu cầu, chống gửi lặp, hết hạn, chống phát lại câu trả lời, quy tắc offline và xung đột nhiều yêu cầu. Quyết định trên điện thoại người dùng phải có điểm kết thúc rõ ràng.
- Không có phản hồi khác với từ chối; mất mạng không đồng nghĩa đã đồng ý.
- Tính năng mạng đòi hỏi thiết kế bảo mật, xác thực, backend hoặc kênh truyền phù hợp; phải cập nhật onboarding, chính sách riêng tư và Data Safety. Cam kết “Không mạng/Không tài khoản” hiện tại sẽ phải thay đổi cho chế độ này.
- Ý tưởng “từ chối 3 lần thì block vợ”: chỉ nên là thông điệp vui hoặc chế độ im thông báo xin phép do hai bên đồng ý; không biến thành tự động chặn liên lạc hay cản khẩn cấp. Cơ chế khóa app hiện tại không cung cấp khả năng chặn riêng một người trong Zalo.

## Thứ tự triển khai đề nghị

1. Bản sửa độ tin cậy: P1-01..08, thử cài release qua Play và TPBank; đóng các lỗi được tái hiện.
2. Bản UX: Home có trạng thái thật, đổi tên thử thách, mở tạm từng app/khóa ngay, picker và lịch rõ ràng.
3. Mở rộng kiểm thử: máy thật Samsung và Xiaomi/Oppo, Android tối thiểu và mới, reboot/Doze/thu hồi quyền/đổi giờ, font lớn/TalkBack; bài test hồi quy theo từng lỗi, không chỉ tăng số lượng test.
4. Trước public: làm mới video đúng UI, hoàn tất khai báo Trợ năng, đối chiếu listing/Data Safety với bản thực tế. Kiểm tra trực tiếp Console để biết các mục đang thiếu; hồ sơ local không phải trạng thái live.
5. Sau đó mới thiết kế ghép đôi và xin phép.

## Nguồn nền tảng

- https://developer.android.com/reference/android/os/SystemClock — wall clock và elapsed time.
- https://developer.android.com/reference/android/accessibilityservice/AccessibilityService — lifecycle, disableSelf và người dùng bật dịch vụ.
- https://developer.android.com/guide/topics/ui/accessibility/apps — vùng chạm và khả năng tiếp cận.
- https://support.google.com/googleplay/android-developer/answer/10964491 — khai báo và phạm vi sử dụng Accessibility API.

Đây là đề xuất sản phẩm/kỹ thuật, không phải chứng nhận an toàn, đo nồng độ cồn hoặc cam kết được mọi ngân hàng chấp nhận.
