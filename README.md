# SayBeer

Ứng dụng giúp người dùng tự cam kết hạn chế mở các app đã chọn trong cuộc nhậu.

- **iOS:** mã nguồn hiện có tại `SayBeer/`, `SayBeerMonitor/`, `SayBeerShield/` và `Shared/`; cấu hình XcodeGen ở `project.yml`.
- **Android:** Kotlin, Jetpack Compose và Material 3 tại [`android/`](android/README.md). Android 8.0 trở lên; package `com.tem2t.saybeer`.

## Android

Mở `android/` bằng Android Studio, dùng JDK 17 trở lên và Android SDK 36.

```sh
cd android
bash gradlew testDebugUnitTest lintDebug assembleDebug
```

Các khóa ký, cấu hình máy, danh sách email tester và bản build phát hành không nằm trong repository. Build release trên checkout mới chưa có khóa riêng sẽ không được ký để phát hành.

Xem [kiểm chứng](android/docs/verification.md), [hướng dẫn phát hành](android/docs/release.md) và [các vấn đề cần hoàn thiện](android/docs/PRODUCT-REVIEW-2026-10-28.md).

SayBeer dùng AccessibilityService để nhận tên app đang mở và hiển thị màn chắn. Dịch vụ có thể xung đột với ứng dụng ngân hàng. Đây là công cụ tự cam kết; bài thử thách không đánh giá nồng độ cồn hoặc khả năng lái xe.
