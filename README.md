# quan-ly-clb-bong-da
Quản lý câu lạc bộ bóng đá

## Mô tả

Đây là một ứng dụng Java để quản lý câu lạc bộ bóng đá. Ứng dụng cho phép quản lý cầu thủ, bao gồm thêm, xóa, chỉnh sửa, tìm kiếm, sắp xếp và thống kê. Ngoài ra, có hệ thống xác thực người dùng để bảo mật các chức năng quan trọng.

## Tính năng

- **Xác thực người dùng**: Đăng ký, đăng nhập, đăng xuất.
- **Quản lý cầu thủ**: Thêm, xóa, chỉnh sửa thông tin cầu thủ.
- **Tìm kiếm và lọc**: Tìm theo tên hoặc số áo, lọc theo vị trí, tình trạng, số áo.
- **Sắp xếp**: Sắp xếp theo tên, số áo, lương, vị trí.
- **Thống kê**: Thống kê số lượng cầu thủ theo vị trí, tình trạng, quỹ lương, lương trung bình, cao nhất/thấp nhất.
- **Lưu trữ dữ liệu**: Đọc và ghi dữ liệu từ file CSV.

## Cài đặt và Chạy

1. Đảm bảo bạn có Java JDK đã cài đặt.
2. Chạy file `build_and_run.bat` trong file "src" để biên dịch và chạy ứng dụng.
3. Ứng dụng sẽ khởi động giao diện console với menu các chức năng.

## Cách sử dụng

Sau khi chạy, bạn sẽ thấy menu chính với các tùy chọn:

1. Đăng ký
2. Đăng nhập
3. Đăng xuất
4. Thêm cầu thủ (cần đăng nhập)
5. Hiển thị danh sách cầu thủ
6. Tìm kiếm cầu thủ
7. Phân loại cầu thủ
8. Thống kê
9. Sắp xếp
10. Xóa cầu thủ (cần đăng nhập)
11. Xem chi tiết cầu thủ (cần đăng nhập)
12. Chỉnh sửa chi tiết cầu thủ (cần đăng nhập)
13. Lưu vào file (cần đăng nhập)
14. Lưu và thoát (cần đăng nhập)
15. Tạo dữ liệu mẫu (cần đăng nhập)
16. Thoát

## Cấu trúc dự án

- `src/`: Mã nguồn Java.
- `out/`: File .class đã biên dịch.
- `data/`: File dữ liệu CSV (cau_thu.csv, troly.csv, users.csv).
- `build_and_run.bat`: Script để biên dịch và chạy.

## Ghi chú

- Dữ liệu cầu thủ được lưu trong `data/cau_thu.csv`.
- Một số chức năng yêu cầu đăng nhập để sử dụng.
