# BÁO CÁO BÀI TẬP LỚN

**Đề tài:** Ứng dụng Quản lý Câu lạc bộ Bóng đá

**Sinh viên thực hiện:** [Tên sinh viên]

**Mã sinh viên:** [Mã sinh viên]

**Lớp:** [Lớp]

**Giảng viên hướng dẫn:** [Tên giảng viên]

**Ngày hoàn thành:** [Ngày]

---

## MỤC LỤC

1. [Giới thiệu](#1-giới-thiệu)
2. [Phân tích yêu cầu](#2-phân-tích-yêu-cầu)
3. [Thiết kế hệ thống](#3-thiết-kế-hệ-thống)
4. [Cài đặt](#4-cài-đặt)
5. [Kiểm thử](#5-kiểm-thử)
6. [Kết luận](#6-kết-luận)
7. [Tài liệu tham khảo](#7-tài-liệu-tham-khảo)

---

## 1. GIỚI THIỆU

### 1.1 Lý do chọn đề tài

Trong bối cảnh bóng đá ngày càng phát triển, việc quản lý cầu thủ và đội bóng trở nên quan trọng. Ứng dụng Quản lý Câu lạc bộ Bóng đá giúp quản lý thông tin cầu thủ, thống kê, và bảo mật dữ liệu một cách hiệu quả.

### 1.2 Mục tiêu

- Xây dựng ứng dụng Java console để quản lý cầu thủ.
- Hỗ trợ các chức năng thêm, xóa, sửa, tìm kiếm, sắp xếp, thống kê.
- Tích hợp hệ thống xác thực người dùng.
- Lưu trữ dữ liệu vào file CSV.

### 1.3 Phạm vi

Ứng dụng bao gồm:

- Quản lý cầu thủ với các thuộc tính: tên, số áo, vị trí, tình trạng, lương, chiều cao, cân nặng.
- Xác thực người dùng.
- Thống kê và báo cáo.

---

## 2. PHÂN TÍCH YÊU CẦU

### 2.1 Yêu cầu chức năng

- **Quản lý cầu thủ:** Thêm, xóa, chỉnh sửa thông tin cầu thủ.
- **Tìm kiếm và lọc:** Theo tên, số áo, vị trí, tình trạng.
- **Sắp xếp:** Theo tên, số áo, lương, vị trí.
- **Thống kê:** Số lượng cầu thủ theo vị trí, tình trạng, quỹ lương.
- **Xác thực:** Đăng ký, đăng nhập, đăng xuất.
- **Lưu trữ:** Đọc/ghi dữ liệu từ file CSV.

### 2.2 Yêu cầu phi chức năng

- Giao diện console đơn giản, dễ sử dụng.
- Bảo mật thông tin người dùng bằng mã hóa mật khẩu.
- Xử lý lỗi và validation dữ liệu.
- Hiệu suất tốt với danh sách cầu thủ lớn.

### 2.3 Các actor và use case

- **Người dùng:** Đăng ký, đăng nhập, quản lý cầu thủ, thống kê.
- Use case chính: Quản lý cầu thủ, Xác thực, Thống kê.

---

## 3. THIẾT KẾ HỆ THỐNG

### 3.1 Kiến trúc tổng quan

Ứng dụng sử dụng kiến trúc MVC đơn giản:

- **Model:** Các lớp CauThu, User, TinhTrang.
- **View:** Giao diện console trong Main.java.
- **Controller:** QuanLyCauThu, AuthService.

### 3.2 Thiết kế lớp

- **CauThu:** Lớp cơ sở cho cầu thủ, với các thuộc tính và phương thức validation.
- **QuanLyCauThu:** Quản lý danh sách cầu thủ, cung cấp các phương thức CRUD, tìm kiếm, sắp xếp, thống kê.
- **AuthService:** Xử lý xác thực người dùng, mã hóa mật khẩu với SHA-256 và salt.
- **Main:** Lớp chính, chứa menu và logic giao diện.

### 3.3 Cơ sở dữ liệu

Dữ liệu lưu trữ trong file CSV:

- cau_thu.csv: Thông tin cầu thủ.
- users.csv: Thông tin người dùng.

### 3.4 Thuật toán

- Sắp xếp: Sử dụng Comparator với Collator cho tiếng Việt.
- Mã hóa: SHA-256 với salt ngẫu nhiên.
- Parse CSV: Xử lý escape và quote.

---

## 4. CÀI ĐẶT

### 4.1 Công nghệ sử dụng

- Ngôn ngữ: Java 8+
- IDE: Visual Studio Code
- Thư viện: java.nio, java.security

### 4.2 Cấu trúc mã nguồn

```
src/
├── qlclb/
│   ├── auth/
│   │   ├── AuthService.java
│   │   └── User.java
│   ├── model/
│   │   ├── CauThu.java
│   │   ├── HauVe.java
│   │   ├── ThuMon.java
│   │   ├── TienDao.java
│   │   ├── TienVe.java
│   │   └── TinhTrang.java
│   ├── service/
│   │   ├── QuanLyCauThu.java
│   │   └── QuanLyDoiBong.java
│   └── ui/
│       ├── GUIMain.java
│       └── Main.java
out/ : File .class
data/ : File CSV
build_and_run.bat : Script build và run
```

### 4.3 Chi tiết cài đặt

- **Lớp CauThu:** Sử dụng enum TinhTrang, validation cho thuộc tính.
- **QuanLyCauThu:** Sử dụng Stream API cho lọc và thống kê.
- **AuthService:** Mã hóa mật khẩu an toàn.
- **Main:** Menu console với switch-case.

### 4.4 Cách chạy

1. Chạy build_and_run.bat để biên dịch và chạy.
2. Ứng dụng khởi động GUI hoặc console menu.

---

## 5. KIỂM THỬ

### 5.1 Kiểm thử đơn vị

- Test validation trong CauThu.
- Test thêm/xóa cầu thủ trong QuanLyCauThu.
- Test đăng nhập trong AuthService.

### 5.2 Kiểm thử tích hợp

- Test luồng đầy đủ: Đăng nhập -> Thêm cầu thủ -> Lưu file.
- Test thống kê và sắp xếp.

### 5.3 Kết quả kiểm thử

Ứng dụng hoạt động ổn định, xử lý lỗi tốt, dữ liệu chính xác.

---

## 6. KẾT LUẬN

### 6.1 Kết quả đạt được

- Hoàn thành các yêu cầu chức năng.
- Ứng dụng dễ sử dụng, bảo mật.
- Mã nguồn rõ ràng, có comment.

### 6.2 Hạn chế

- Giao diện console đơn giản, chưa có GUI đầy đủ.
- Chưa hỗ trợ database ngoài CSV.

### 6.3 Hướng phát triển

- Phát triển GUI hoàn chỉnh.
- Tích hợp database SQL.
- Thêm tính năng quản lý trận đấu.

---

## 7. TÀI LIỆU THAM KHẢO

1. Oracle Java Documentation
2. Java Stream API Guide
3. CSV Parsing Best Practices
