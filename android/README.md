# SayBeer · Android

Ứng dụng Android native bằng **Kotlin + Jetpack Compose + Material 3**, chuyển từ bản iOS tại thư mục gốc repository. Giữ giao diện tối nâu–hổ phách, nội dung tiếng Việt và các bước tự cam kết của SayBeer.

Giao diện được triển khai theo thiết kế Stitch của chủ dự án: [nguồn thiết kế và ánh xạ Compose](docs/design/STITCH-REDESIGN.md). Bản Play internal mới nhất là `1.1.1` / code `4`, gồm sửa lỗi danh sách app nhắn tin như Zalo và tự làm mới danh sách khi quay lại.

**Hãy say bia. Xin đừng “say” bậy.**

## Chạy dự án

- Mở thư mục `android` bằng Android Studio, chọn JDK 17 trở lên (JBR của Android Studio cũng dùng được).
- Cài Android SDK 36; Android tối thiểu 8.0 / API 26.
- Để Android Studio tạo `local.properties`, hoặc đặt biến `ANDROID_HOME` tới SDK.
- Chạy cấu hình `app` trên điện thoại / emulator.

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat testDebugUnitTest lintDebug
.\gradlew.bat connectedDebugAndroidTest
.\gradlew.bat assembleRelease bundleRelease
```

macOS/Linux: dùng `bash gradlew` thay cho `gradlew.bat`.

APK cài thử: `app/build/outputs/apk/debug/app-debug.apk`. Bản release bật R8 và tự ký khi máy có `.signing/signing.properties`; khi không có cấu hình này, build release chưa ký. Upload key và mật khẩu được giữ trong `.signing/`, bị loại khỏi Git. Xem [hồ sơ Play Store](play-store/STATUS.md).

### Thiết lập trên thiết bị

1. Đi qua 3 màn giới thiệu, đọc và đồng ý phần giải thích quyền.
2. Trong Cài đặt → Trợ năng → Ứng dụng đã cài đặt, bật **SayBeer**. Tên mục có thể khác giữa các hãng. Với APK cài ngoài, Android có thể yêu cầu cho phép “Cài đặt bị hạn chế” trong trang thông tin app trước.
3. Chọn app cần khóa. Điện thoại mặc định, màn hình chính, System UI và Cài đặt được loại khỏi danh sách để luôn sử dụng được. Các app nhắn tin có chức năng gọi như Zalo vẫn có thể được chọn.
4. Nếu dùng lịch tuần, cho phép “Báo thức và lời nhắc” để lịch nền chạy đúng giờ. Khi không có quyền, app dùng báo thức không chính xác và giải thích giới hạn ngay trong giao diện.
5. Quyền thông báo là tùy chọn, được hỏi sau khi bắt đầu phiên trên Android 13+.

## Các tính năng đã triển khai

| Luồng iOS | Android |
| --- | --- |
| Onboarding 3 bước | Có, giải thích quyền Trợ năng và yêu cầu đồng ý rõ ràng |
| Chọn app | Danh sách app launcher thật, icon, tìm kiếm, chọn nhiều, lưu lựa chọn |
| Khóa đến 6h sáng / +4h / +8h | Có; tùy chọn thêm 30–1440 phút |
| Screen Time shield | Accessibility overlay trên đúng app đã chọn |
| Đồng hồ phiên | Đếm ngược trực tiếp, lưu deadline thực tế |
| Nhắn việc | Gõ cam kết, chờ 15 giây, mở 15 phút, tối đa 3 lần / phiên |
| Kiểm tra tỉnh táo | 5 câu tiếng Việt ngẫu nhiên có dấu; tính nhẩm tùy chọn; 90 giây / lượt |
| Sai 3 lần | Chờ 5 phút, lưu cả khi đóng / mở lại app |
| Khẩn cấp | Giữ 5 giây, nhả tay hủy; có hành động hỗ trợ TalkBack |
| Lịch tuần | Nhiều ngày, qua đêm, theo múi giờ máy; phục hồi sau reboot / cập nhật app |
| Thống kê | Phiên an toàn, số giờ, chuỗi, lý do mở, 30 phiên gần nhất; lưu tối đa 200 |
| Chia sẻ | Ảnh PNG 1080×1350 qua Android Sharesheet và FileProvider |
| Riêng tư | Không Internet, tài khoản, analytics, quảng cáo; tắt backup và chuyển dữ liệu |

### Những khác biệt do nền tảng

- Android thông thường **không cho khóa gỡ cài đặt** như ManagedSettings trên iOS. Không dùng Device Admin, không chặn Cài đặt, không cản người dùng tắt dịch vụ.
- Khóa từng app có thể mở từ launcher. Chưa có chọn nhóm app hoặc domain web như FamilyActivityPicker. Có thể chọn trình duyệt để khóa toàn bộ trình duyệt.
- Màn chắn là lớp phủ Trợ năng khi mở app, không phải đình chỉ package ở cấp hệ điều hành. Thông báo và cuộc gọi vẫn hoạt động.
- Sau khi **buộc dừng**, Android ngừng nhận alarm cho app cho tới khi người dùng mở lại. Khi tắt Trợ năng thì màn chắn không hoạt động. Giao diện hiển thị trạng thái quyền thực tế.
- Lịch dùng exact alarm khi có quyền; nếu không, Android có thể trì hoãn alarm nền. Dịch vụ đang kết nối cũng kiểm tra deadline mỗi giây. Tiết kiệm pin của từng hãng có thể ảnh hưởng; cần kiểm thử thêm trên thiết bị thật.
- Bài kiểm tra là bước tự cam kết, không xác nhận nồng độ cồn hoặc khả năng lái xe.

## Cấu trúc

```text
app/src/main/java/com/tem2t/saybeer/
  MainActivity.kt                Activity Compose, edge-to-edge
  SayBeerApplication.kt          Repository dùng chung toàn process
  data/SessionEngine.kt          State machine Kotlin thuần, lịch tuần và thống kê
  data/SessionRepository.kt      JSON trong SharedPreferences, lưu trước khi phát state
  platform/                     Accessibility shield, alarm/boot receiver, notification,
                                danh sách app, ảnh chia sẻ
  ui/                           Theme, onboarding, home, chọn app, lịch, kiểm tra,
                                nhắn việc, khẩn cấp, thống kê, riêng tư
app/src/test/                   Unit tests logic và lịch
app/src/androidTest/            Compose flows + kiểm thử Accessibility thật
docs/                           Ghi chú phát hành, kiểm thử, ảnh từ emulator
```

Không có backend. `StateFlow` đồng bộ activity, receiver và service trong cùng process. SessionEngine dùng timestamp tuyệt đối, alarm phục hồi sau boot; một mốc lịch đã xử lý không khởi động lại phiên đã mở sớm. Đổi múi giờ được tính lại bằng `java.time`.

## Kiểm thử

Xem [docs/verification.md](docs/verification.md) để biết kết quả và các bước cần thử trên điện thoại thật. CI tại `../.github/workflows/android.yml` chạy từ `android/`, build APK/AAB, chạy unit tests và lint.

Trước khi đưa lên Play Store, xem [docs/release.md](docs/release.md). Việc chấp thuận Accessibility API cần được Google Play xem xét; source đã có disclosure và mô tả quyền, chưa có submission.
