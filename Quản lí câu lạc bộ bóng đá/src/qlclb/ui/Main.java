package qlclb.ui;

import qlclb.model.*;
import qlclb.service.QuanLyCauThu;
import qlclb.service.QuanLyDoiBong;
import qlclb.auth.AuthService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

// Lớp Main chính, khởi chạy GUI
public class Main {
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(GUIMain::new);
    }
}

// Lớp Main console bị vô hiệu hóa, thay thế bằng GUIMain
class MainDisabled {
    private static final String DATA_FILE = "data/cau_thu.csv";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
        QuanLyDoiBong doiBong = new QuanLyDoiBong("CLB 1", "HLV 1");
        QuanLyCauThu ql = doiBong.getQuanLyCauThu();
        AuthService auth = new AuthService();

        int n = ql.docFileCSV(DATA_FILE);
        if (n > 0) System.out.println("Đã nạp " + n + " cầu thủ từ " + DATA_FILE);

        while (true) {
            inMenu(auth);
            int chon = readInt(sc, "Chọn chức năng: ", 0, 15);
            switch (chon) {
                case 1: handleDangKy(sc, auth); break;
                case 2: handleDangNhap(sc, auth); break;
                case 3: handleDangXuat(auth); break;
                case 4: requireLogin(auth, () -> handleThemCauThu(sc, ql)); break;
                case 5: ql.hienThiDanhSach(ql.getDanhSach()); break;
                case 6: handleTimKiem(sc, ql); break;
                case 7: handlePhanLoai(sc, ql); break;
                case 8: handleThongKe(ql); break;
                case 9: handleSapXep(sc, ql); break;
                case 10: requireLogin(auth, () -> handleXoa(sc, ql)); break;
                case 11: requireLogin(auth, () -> handleXemChiTiet(sc, ql)); break;
                case 12: requireLogin(auth, () -> handleChinhSuaChiTiet(sc, ql)); break;
                case 13: requireLogin(auth, () -> { ql.ghiFileCSV(DATA_FILE); System.out.println("Đã lưu dữ liệu vào " + DATA_FILE); }); break;
                case 14: requireLogin(auth, () -> { ql.ghiFileCSV(DATA_FILE); System.out.println("Đã lưu và thoát."); System.exit(0); }); break;
                case 15: requireLogin(auth, () -> { seedDemoData(ql); System.out.println("Đã tạo dữ liệu mẫu."); }); break;
                case 0: System.out.println("Thoát không lưu."); return;
                default: System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }

    private static void inMenu(AuthService auth) {
        System.out.println("\n===== QUẢN LÝ CÂU LẠC BỘ BÓNG ĐÁ =====");
        System.out.println("Người dùng: " + auth.getCurrentUser().orElse("(chưa đăng nhập)"));
        System.out.println("1. Đăng ký");
        System.out.println("2. Đăng nhập");
        System.out.println("3. Đăng xuất");
        System.out.println("4. Thêm cầu thủ (cần đăng nhập)");
        System.out.println("5. Hiển thị danh sách");
        System.out.println("6. Tìm kiếm cầu thủ");
        System.out.println("7. Phân loại (dự bị/đá chính, vị trí, tình trạng, số áo)");
        System.out.println("8. Thống kê (quỹ lương, lương TB, cao nhất/thấp nhất)");
        System.out.println("9. Sắp xếp (tên, số áo, lương, vị trí)");
        System.out.println("10. Xóa cầu thủ theo số áo (cần đăng nhập)");
        System.out.println("11. Xem chi tiết cầu thủ (cần đăng nhập)");
        System.out.println("12. Chỉnh sửa chi tiết cầu thủ (cần đăng nhập)");
        System.out.println("13. Lưu vào file (cần đăng nhập)");
        System.out.println("14. Lưu và thoát (cần đăng nhập)");
        System.out.println("15. Tạo dữ liệu mẫu (cần đăng nhập)");
        System.out.println("0. Thoát không lưu");
    }

    private static void requireLogin(AuthService auth, Runnable action) {
        if (!auth.isLoggedIn()) {
            System.out.println("Vui lòng đăng nhập để thực hiện chức năng này.");
            return;
        }
        action.run();
    }

    private static void handleDangKy(Scanner sc, AuthService auth) {
        System.out.println("-- Đăng ký --");
        String user = readNonEmpty(sc, "Tên đăng nhập: ");
        String pass = readPassword(sc, "Mật khẩu (>=4 ký tự): ");
        boolean ok = auth.register(user, pass);
        System.out.println(ok ? "Đăng ký thành công." : "Đăng ký thất bại (tài khoản đã tồn tại hoặc dữ liệu không hợp lệ).");
    }

    private static void handleDangNhap(Scanner sc, AuthService auth) {
        System.out.println("-- Đăng nhập --");
        String user = readNonEmpty(sc, "Tên đăng nhập: ");
        String pass = readNonEmpty(sc, "Mật khẩu: ");
        boolean ok = auth.login(user, pass);
        System.out.println(ok ? ("Đăng nhập thành công. Xin chào, " + user + ".") : "Sai tài khoản hoặc mật khẩu.");
    }

    private static void handleDangXuat(AuthService auth) {
        if (auth.isLoggedIn()) { auth.logout(); System.out.println("Đã đăng xuất."); }
        else System.out.println("Bạn chưa đăng nhập.");
    }

    private static void handleThemCauThu(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Thêm cầu thủ --");
        String ten = readNonEmpty(sc, "Tên cầu thủ: ");
        int soAo = readInt(sc, "Số áo (>0): ", 1, null);
        int loai = readInt(sc, "Vị trí: 1- Thủ môn, 2- Hậu vệ, 3- Tiền vệ, 4- Tiền đạo: ", 1, 4);
        int tt = readInt(sc, "Tình trạng: 1- Khỏe, 2- Chấn thương, 3- Nghỉ phép: ", 1, 3);
        boolean daChinh = readYesNo(sc, "Đá chính? (y/n): ");
        boolean khongThiDau = readYesNo(sc, "Không thi đấu? (y/n): ");
        double luong = readDouble(sc, "Lương tháng (>=0): ", 0.0, null);
        Double cc = readOptionalDouble(sc, "Chiều cao (cm, bỏ trống nếu không): ");
        Double cn = readOptionalDouble(sc, "Cân nặng (kg, bỏ trống nếu không): ");
        TinhTrang tinhTrang = (tt == 1 ? TinhTrang.KHOE : tt == 2 ? TinhTrang.CHAN_THUONG : TinhTrang.NGHI_PHEP);
        CauThu ct;
        switch (loai) {
            case 1: ct = new ThuMon(ten, soAo, tinhTrang, daChinh, khongThiDau, luong); break;
            case 2: ct = new HauVe(ten, soAo, tinhTrang, daChinh, khongThiDau, luong); break;
            case 3: ct = new TienVe(ten, soAo, tinhTrang, daChinh, khongThiDau, luong); break;
            case 4: ct = new TienDao(ten, soAo, tinhTrang, daChinh, khongThiDau, luong); break;
            default: ct = new CauThu(ten, soAo, "Khác", tinhTrang, daChinh, khongThiDau, luong);
        }
        try {
            if (cc != null) ct.setChieuCao(cc);
            if (cn != null) ct.setCanNang(cn);
            ql.themCauThu(ct);
            System.out.println("Đã thêm: " + ct);
        } catch (QuanLyCauThu.DuplicateSoAoException e) {
            System.out.println("Lỗi: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Lỗi dữ liệu: " + e.getMessage());
        }
    }

    private static void handleTimKiem(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Tìm kiếm --");
        int c = readInt(sc, "1- Theo tên, 2- Theo số áo: ", 1, 2);
        if (c == 1) {
            String k = readNonEmpty(sc, "Nhập từ khóa tên: ");
            List<CauThu> res = ql.timTheoTen(k);
            ql.hienThiDanhSach(res);
        } else {
            int soAo = readInt(sc, "Nhập số áo: ", 1, null);
            ql.timTheoSoAo(soAo).ifPresentOrElse(
                ct -> System.out.println(ct),
                () -> System.out.println("Không tìm thấy."));
        }
    }

    private static void handlePhanLoai(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Phân loại --");
        System.out.println("1- Dự bị/Đá chính");
        System.out.println("2- Theo vị trí");
        System.out.println("3- Theo tình trạng");
        System.out.println("4- Theo khoảng số áo");
        int c = readInt(sc, "Chọn: ", 1, 4);
        switch (c) {
            case 1: {
                boolean dc = readYesNo(sc, "Hiển thị Đá chính? (y=Đá chính, n=Dự bị): ");
                ql.hienThiDanhSach(ql.locTheoDaChinh(dc));
                break;
            }
            case 2: {
                int loai = readInt(sc, "1- Thủ môn, 2- Hậu vệ, 3- Tiền vệ, 4- Tiền đạo: ", 1, 4);
                String v = (loai == 1 ? "Thủ môn" : loai == 2 ? "Hậu vệ" : loai == 3 ? "Tiền vệ" : "Tiền đạo");
                ql.hienThiDanhSach(ql.locTheoViTri(v));
                break;
            }
            case 3: {
                int st = readInt(sc, "1- Khỏe, 2- Chấn thương, 3- Nghỉ phép: ", 1, 3);
                TinhTrang tt = (st == 1 ? TinhTrang.KHOE : st == 2 ? TinhTrang.CHAN_THUONG : TinhTrang.NGHI_PHEP);
                ql.hienThiDanhSach(ql.locTheoTinhTrang(tt));
                break;
            }
            case 4: {
                int min = readInt(sc, "Số áo từ: ", 1, null);
                int max = readInt(sc, "đến: ", min, null);
                ql.hienThiDanhSach(ql.locTheoSoAoTrongKhoang(min, max));
                break;
            }
        }
    }

    private static void handleThongKe(QuanLyCauThu ql) {
        System.out.println("-- Thống kê --");
        Map<Boolean, Long> dc = ql.thongKeDaChinh();
        System.out.println("Đá chính: " + dc.getOrDefault(true, 0L) + ", Dự bị: " + dc.getOrDefault(false, 0L));

        Map<String, Long> tvt = ql.thongKeTheoViTri();
        System.out.println("Theo vị trí: " + tvt);

        Map<TinhTrang, Long> ttt = ql.thongKeTheoTinhTrang();
        System.out.println("Theo tình trạng: " + ttt);

        double tong = ql.tongQuyLuong();
        double tb = ql.luongTrungBinh();
        System.out.println(String.format(Locale.US, "Quỹ lương: %.0f, Lương TB: %.0f", tong, tb));

        ql.luongCaoNhat().ifPresent(ct -> System.out.println("Lương cao nhất: " + ct));
        ql.luongThapNhat().ifPresent(ct -> System.out.println("Lương thấp nhất: " + ct));
    }

    private static void handleSapXep(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Sắp xếp --");
        System.out.println("1- Theo tên");
        System.out.println("2- Theo số áo");
        System.out.println("3- Theo lương");
        System.out.println("4- Theo vị trí (thứ tự: Thủ môn → Hậu vệ → Tiền vệ → Tiền đạo)");
        int c = readInt(sc, "Chọn: ", 1, 4);
        switch (c) {
            case 1: ql.sapXepTheoTen(); break;
            case 2: ql.sapXepTheoSoAo(); break;
            case 3: ql.sapXepTheoLuong(); break;
            case 4: ql.sapXepTheoViTri(); break;
        }
        System.out.println("Đã sắp xếp.");
        ql.hienThiDanhSach(ql.getDanhSach());
    }

    private static void handleXoa(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Xóa cầu thủ --");
        int so = readInt(sc, "Nhập số áo cần xóa: ", 1, null);
        boolean ok = ql.xoaTheoSoAo(so);
        System.out.println(ok ? "Đã xóa." : "Không tìm thấy số áo.");
    }

    private static void handleXemChiTiet(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Xem chi tiết cầu thủ --");
        int soAo = readInt(sc, "Nhập số áo: ", 1, null);
        Optional<CauThu> opt = ql.timTheoSoAo(soAo);
        if (opt.isPresent()) {
            CauThu ct = opt.get();
            System.out.println(ct);
        } else {
            System.out.println("Không tìm thấy cầu thủ.");
        }
    }

    private static void handleChinhSuaChiTiet(Scanner sc, QuanLyCauThu ql) {
        System.out.println("-- Chỉnh sửa chi tiết cầu thủ --");
        int soAo = readInt(sc, "Nhập số áo: ", 1, null);
        Optional<CauThu> opt = ql.timTheoSoAo(soAo);
        if (opt.isEmpty()) { System.out.println("Không tìm thấy cầu thủ."); return; }
        CauThu ct = opt.get();
        System.out.println("Hiện tại: " + ct);

        if (readYesNo(sc, "Sửa tên? (y/n): ")) {
            ct.setTen(readNonEmpty(sc, "Tên mới: "));
        }
        if (readYesNo(sc, "Sửa tình trạng? (y/n): ")) {
            int st = readInt(sc, "1- Khỏe, 2- Chấn thương, 3- Nghỉ phép: ", 1, 3);
            ct.setTinhTrang(st == 1 ? TinhTrang.KHOE : st == 2 ? TinhTrang.CHAN_THUONG : TinhTrang.NGHI_PHEP);
        }
        if (readYesNo(sc, "Sửa đá chính/dự bị? (y/n): ")) {
            ct.setDaChinh(readYesNo(sc, "Đá chính? (y/n): "));
        }
        if (readYesNo(sc, "Sửa lương? (y/n): ")) {
            ct.setLuongThang(readDouble(sc, "Lương tháng (>=0): ", 0.0, null));
        }
        if (readYesNo(sc, "Sửa chiều cao? (y/n): ")) {
            Double cc = readOptionalDouble(sc, "Chiều cao (cm, bỏ trống để xóa): ");
            ct.setChieuCao(cc);
        }
        if (readYesNo(sc, "Sửa cân nặng? (y/n): ")) {
            Double cn = readOptionalDouble(sc, "Cân nặng (kg, bỏ trống để xóa): ");
            ct.setCanNang(cn);
        }
        System.out.println("Đã cập nhật: " + ct);
    }

    // Input helpers
    private static String readNonEmpty(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            if (s != null) s = s.trim();
            if (s != null && !s.isEmpty()) return s;
            System.out.println("Không được để trống, hãy nhập lại.");
        }
    }

    private static String readPassword(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            if (s != null && s.length() >= 4) return s;
            System.out.println("Mật khẩu tối thiểu 4 ký tự.");
        }
    }

    private static int readInt(Scanner sc, String prompt, Integer min, Integer max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                int val = Integer.parseInt(line.trim());
                if (min != null && val < min) { System.out.println("Giá trị phải >= " + min); continue; }
                if (max != null && val > max) { System.out.println("Giá trị phải <= " + max); continue; }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Sai kiểu dữ liệu, hãy nhập số nguyên.");
            }
        }
    }

    private static double readDouble(Scanner sc, String prompt, Double min, Double max) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            try {
                double val = Double.parseDouble(line.trim());
                if (min != null && val < min) { System.out.println("Giá trị phải >= " + min); continue; }
                if (max != null && val > max) { System.out.println("Giá trị phải <= " + max); continue; }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Sai kiểu dữ liệu, hãy nhập số.");
            }
        }
    }

    private static Double readOptionalDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = sc.nextLine();
            if (line == null || line.trim().isEmpty()) return null;
            try {
                double val = Double.parseDouble(line.trim());
                if (val <= 0) { System.out.println("Giá trị phải > 0 hoặc bỏ trống."); continue; }
                return val;
            } catch (NumberFormatException e) {
                System.out.println("Sai kiểu dữ liệu, hãy nhập số hoặc bỏ trống.");
            }
        }
    }

    private static boolean readYesNo(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim().toLowerCase();
            if (s.equals("y") || s.equals("yes") || s.equals("c") || s.equals("co") || s.equals("có")) return true;
            if (s.equals("n") || s.equals("no") || s.equals("khong") || s.equals("không")) return false;
            System.out.println("Chỉ nhập y/n.");
        }
    }

    // Demo seed
    private static void seedDemoData(QuanLyCauThu ql) {
        if (!ql.getDanhSach().isEmpty()) return;
        try {
            CauThu a = new ThuMon("Nguyen Van A", 1, TinhTrang.KHOE, true, false, 30000000.0); a.setChieuCao(185.0); a.setCanNang(78.0); ql.themCauThu(a);
            CauThu b = new HauVe("Tran Van B", 4, TinhTrang.CHAN_THUONG, false, false, 25000000.0); b.setChieuCao(180.0); b.setCanNang(75.0); ql.themCauThu(b);
            CauThu c = new TienVe("Le Van C", 8, TinhTrang.KHOE, true, false, 40000000.0); c.setChieuCao(176.0); c.setCanNang(70.0); ql.themCauThu(c);
            CauThu d = new TienDao("Pham Van D", 9, TinhTrang.NGHI_PHEP, false, false, 50000000.0); d.setChieuCao(182.0); d.setCanNang(77.0); ql.themCauThu(d);
        } catch (Exception ignored) {}
    }
}
