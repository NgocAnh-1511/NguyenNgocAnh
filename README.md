# Beer Adviser

## Mô tả cơ bản

**Beer Adviser** là một ứng dụng Android đơn giản được xây dựng bằng Kotlin, giúp người dùng tìm kiếm các loại bia dựa trên màu sắc. Ứng dụng cung cấp giao diện thân thiện với người dùng, cho phép:

- Chọn màu bia từ danh sách (Light, Amber, Brown, Dark)
- Tìm kiếm các thương hiệu bia phù hợp với màu đã chọn
- Hiển thị danh sách các loại bia được đề xuất

### Tính năng

- Giao diện đơn giản và dễ sử dụng
- Hỗ trợ 4 loại màu bia: Light, Amber, Brown, Dark
- Hiển thị danh sách bia theo màu đã chọn
- Ứng dụng native Android được phát triển bằng Kotlin

### Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **Android SDK**: 
  - Min SDK: 31 (Android 12)
  - Target SDK: 36
  - Compile SDK: 36
- **Thư viện chính**:
  - AndroidX Core KTX
  - AndroidX AppCompat
  - Material Design Components
  - AndroidX ConstraintLayout

## Cách cài đặt

### Yêu cầu hệ thống

Trước khi cài đặt, đảm bảo bạn có:

1. **Android Studio** (bản mới nhất được khuyến nghị)
   - Tải tại: https://developer.android.com/studio
   - Phiên bản hỗ trợ Android Gradle Plugin 8.13.0

2. **JDK 11** hoặc cao hơn
   - Android Studio thường đi kèm với JDK, nhưng bạn có thể cài đặt riêng nếu cần

3. **Android SDK** (sẽ được tải tự động khi mở project trong Android Studio)

### Các bước cài đặt

#### Bước 1: Clone hoặc tải project

```bash
# Nếu sử dụng Git
git clone <repository-url>
cd MyApplication

# Hoặc giải nén file ZIP nếu tải về dạng ZIP
```

#### Bước 2: Mở project trong Android Studio

1. Khởi động **Android Studio**
2. Chọn **File → Open** (hoặc **Open an Existing Project**)
3. Duyệt đến thư mục `MyApplication` và chọn nó
4. Nhấn **OK** để mở project

#### Bước 3: Đợi Gradle sync

- Android Studio sẽ tự động đồng bộ Gradle và tải các dependencies
- Quá trình này có thể mất vài phút lần đầu tiên
- Đảm bảo bạn có kết nối internet để tải các thư viện cần thiết

#### Bước 4: Cấu hình Android SDK (nếu cần)

1. Vào **File → Project Structure** (hoặc **File → Settings → Appearance & Behavior → System Settings → Android SDK**)
2. Đảm bảo đã cài đặt:
   - Android SDK Platform 36
   - Android SDK Build-Tools
   - Android Emulator (nếu muốn chạy trên emulator)

#### Bước 5: Chạy ứng dụng

**Cách 1: Chạy trên Emulator**

1. Tạo một Android Virtual Device (AVD):
   - Vào **Tools → Device Manager**
   - Nhấn **Create Device**
   - Chọn thiết bị và API level (tối thiểu API 31)
   - Hoàn tất quá trình tạo AVD

2. Chạy ứng dụng:
   - Chọn AVD từ danh sách thiết bị ở thanh toolbar
   - Nhấn nút **Run** (▶️) hoặc nhấn `Shift + F10`

**Cách 2: Chạy trên thiết bị thật**

1. Bật **Developer Options** trên thiết bị Android:
   - Vào **Settings → About Phone**
   - Nhấn 7 lần vào **Build Number**

2. Bật **USB Debugging**:
   - Vào **Settings → Developer Options**
   - Bật **USB Debugging**

3. Kết nối thiết bị với máy tính qua USB

4. Cho phép USB Debugging khi thiết bị hiển thị hộp thoại xác nhận

5. Chọn thiết bị từ danh sách và nhấn **Run**

### Build APK thủ công

Nếu muốn build file APK để cài đặt thủ công:

1. Vào **Build → Build Bundle(s) / APK(s) → Build APK(s)**
2. Đợi quá trình build hoàn tất
3. File APK sẽ được tạo tại: `app/build/outputs/apk/debug/app-debug.apk`
4. Copy file APK sang thiết bị Android và cài đặt

### Xử lý lỗi thường gặp

**Lỗi: Gradle sync failed**
- Kiểm tra kết nối internet
- Đảm bảo Android Studio đã cập nhật lên phiên bản mới nhất
- Thử **File → Invalidate Caches / Restart**

**Lỗi: SDK không tìm thấy**
- Vào **File → Project Structure → SDK Location**
- Đảm bảo đường dẫn Android SDK đúng
- Hoặc để Android Studio tự động cài đặt SDK

**Lỗi: Thiết bị không được nhận diện**
- Kiểm tra cáp USB
- Cài đặt USB drivers cho thiết bị (nếu cần)
- Thử kết nối qua WiFi Debugging

## Cấu trúc project

```
MyApplication/
├── app/
│   ├── build.gradle.kts          # Cấu hình module app
│   ├── src/
│   │   └── main/
│   │       ├── AndroidManifest.xml
│   │       ├── java/com/example/myapplication/
│   │       │   └── MainActivity.kt    # Activity chính
│   │       └── res/                    # Tài nguyên (layout, strings, drawable)
│   └── proguard-rules.pro
├── build.gradle.kts              # Cấu hình project root
├── gradle/
│   └── libs.versions.toml        # Quản lý phiên bản dependencies
├── settings.gradle.kts
└── gradle.properties
```

## Phiên bản

- **Version Code**: 1
- **Version Name**: 1.0

## Giấy phép

Project này là một ứng dụng mẫu. Bạn có thể tự do sử dụng và chỉnh sửa theo nhu cầu.

## Tác giả

Ứng dụng được phát triển như một project mẫu Android.

