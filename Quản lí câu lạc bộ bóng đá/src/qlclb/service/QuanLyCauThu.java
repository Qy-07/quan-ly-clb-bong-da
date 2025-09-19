package qlclb.service;

import qlclb.model.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

// Lớp quản lý danh sách cầu thủ với tối ưu và comment tiếng Việt
public class QuanLyCauThu {
    private final List<CauThu> danhSach = new ArrayList<>();

    // Lấy danh sách cầu thủ
    public List<CauThu> getDanhSach() { return danhSach; }

    // Thêm cầu thủ, kiểm tra số áo trùng và dữ liệu hợp lệ
    public void themCauThu(CauThu ct) throws DuplicateSoAoException {
        Objects.requireNonNull(ct, "Cầu thủ không được null");
        if (ct.getSoAo() <= 0) throw new IllegalArgumentException("Số áo phải > 0");
        if (ct.getLuongThang() < 0) throw new IllegalArgumentException("Lương tháng không được âm");
        if (danhSach.stream().anyMatch(c -> c.getSoAo() == ct.getSoAo()))
            throw new DuplicateSoAoException(ct.getSoAo());
        danhSach.add(ct);
    }

    // Xóa cầu thủ theo số áo
    public boolean xoaTheoSoAo(int soAo) {
        return danhSach.removeIf(c -> c.getSoAo() == soAo);
    }

    // Tìm cầu thủ theo số áo
    public Optional<CauThu> timTheoSoAo(int soAo) {
        return danhSach.stream().filter(c -> c.getSoAo() == soAo).findFirst();
    }

    // Tìm cầu thủ theo tên (không phân biệt hoa thường)
    public List<CauThu> timTheoTen(String keyword) {
        String k = keyword.toLowerCase(Locale.ROOT).trim();
        return danhSach.stream()
                .filter(c -> c.getTen().toLowerCase(Locale.ROOT).contains(k))
                .collect(Collectors.toList());
    }

    // Lọc cầu thủ theo vị trí (không phân biệt hoa thường)
    public List<CauThu> locTheoViTri(String viTri) {
        String v = viTri.toLowerCase(Locale.ROOT).trim();
        return danhSach.stream()
                .filter(c -> c.getViTri().toLowerCase(Locale.ROOT).contains(v))
                .collect(Collectors.toList());
    }

    // Lọc cầu thủ theo tình trạng
    public List<CauThu> locTheoTinhTrang(TinhTrang t) {
        return danhSach.stream()
                .filter(c -> c.getTinhTrang() == t)
                .collect(Collectors.toList());
    }

    // Lọc cầu thủ theo đá chính hay dự bị
    public List<CauThu> locTheoDaChinh(boolean daChinh) {
        return danhSach.stream()
                .filter(c -> c.isDaChinh() == daChinh)
                .collect(Collectors.toList());
    }

    // Lọc cầu thủ theo số áo trong khoảng min-max
    public List<CauThu> locTheoSoAoTrongKhoang(int min, int max) {
        return danhSach.stream()
                .filter(c -> c.getSoAo() >= min && c.getSoAo() <= max)
                .collect(Collectors.toList());
    }

    // Hiển thị danh sách cầu thủ, nếu list null thì hiển thị toàn bộ
    public void hienThiDanhSach(List<CauThu> list) {
        if (list == null) list = danhSach;
        if (list.isEmpty()) {
            System.out.println("Danh sách trống.");
            return;
        }
        list.forEach(System.out::println);
    }

    // Sắp xếp danh sách theo tên (theo chuẩn tiếng Việt)
    public void sapXepTheoTen() {
        Collator collator;
        try {
            collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        } catch (Exception e) {
            collator = Collator.getInstance(Locale.getDefault());
        }
        final Collator finalCollator = collator;
        danhSach.sort((a, b) -> {
            int cmp = finalCollator.compare(a.getTen(), b.getTen());
            if (cmp != 0) return cmp;
            return Integer.compare(a.getSoAo(), b.getSoAo());
        });
    }

    // Sắp xếp theo số áo
    public void sapXepTheoSoAo() {
        danhSach.sort(Comparator.comparingInt(CauThu::getSoAo));
    }

    // Sắp xếp theo lương tháng
    public void sapXepTheoLuong() {
        danhSach.sort(Comparator.comparingDouble(CauThu::getLuongThang)
                .thenComparingInt(CauThu::getSoAo));
    }

    // Sắp xếp theo vị trí với thứ tự chuẩn
    public void sapXepTheoViTri() {
        Map<String, Integer> order = Map.of(
                "Thủ môn", 1,
                "Hậu vệ", 2,
                "Tiền vệ", 3,
                "Tiền đạo", 4
        );
        Collator collator;
        try {
            collator = Collator.getInstance(Locale.forLanguageTag("vi-VN"));
        } catch (Exception e) {
            collator = Collator.getInstance();
        }
        final Collator finalCollator = collator;
        danhSach.sort((a, b) -> {
            int oa = order.getOrDefault(a.getViTri(), 99);
            int ob = order.getOrDefault(b.getViTri(), 99);
            if (oa != ob) return Integer.compare(oa, ob);
            return finalCollator.compare(a.getTen(), b.getTen());
        });
    }

    // Thống kê số cầu thủ đá chính và dự bị
    public Map<Boolean, Long> thongKeDaChinh() {
        Map<Boolean, Long> m = new HashMap<>();
        m.put(Boolean.TRUE, danhSach.stream().filter(CauThu::isDaChinh).count());
        m.put(Boolean.FALSE, danhSach.stream().filter(c -> !c.isDaChinh() && !c.isKhongThiDau()).count());
        return m;
    }

    // Thống kê số cầu thủ theo vị trí, đảm bảo thứ tự chuẩn
    public Map<String, Long> thongKeTheoViTri() {
        Map<String, Long> m = new LinkedHashMap<>();
        m.put("Thủ môn", 0L);
        m.put("Hậu vệ", 0L);
        m.put("Tiền vệ", 0L);
        m.put("Tiền đạo", 0L);
        for (CauThu c : danhSach) {
            m.put(c.getViTri(), m.getOrDefault(c.getViTri(), 0L) + 1);
        }
        return m;
    }

    // Thống kê số cầu thủ theo tình trạng
    public Map<TinhTrang, Long> thongKeTheoTinhTrang() {
        Map<TinhTrang, Long> m = new LinkedHashMap<>();
        m.put(TinhTrang.KHOE, 0L);
        m.put(TinhTrang.CHAN_THUONG, 0L);
        m.put(TinhTrang.NGHI_PHEP, 0L);
        for (CauThu c : danhSach) {
            m.put(c.getTinhTrang(), m.getOrDefault(c.getTinhTrang(), 0L) + 1);
        }
        return m;
    }

    // Thống kê số cầu thủ không thi đấu
    public long thongKeKhongThiDau() {
        return danhSach.stream().filter(CauThu::isKhongThiDau).count();
    }

    // Tổng quỹ lương
    public double tongQuyLuong() {
        return danhSach.stream().mapToDouble(CauThu::getLuongThang).sum();
    }

    // Lương trung bình
    public double luongTrungBinh() {
        return danhSach.isEmpty() ? 0 : tongQuyLuong() / danhSach.size();
    }

    // Cầu thủ có lương cao nhất
    public Optional<CauThu> luongCaoNhat() {
        return danhSach.stream().max(Comparator.comparingDouble(CauThu::getLuongThang));
    }

    // Cầu thủ có lương thấp nhất
    public Optional<CauThu> luongThapNhat() {
        return danhSach.stream().min(Comparator.comparingDouble(CauThu::getLuongThang));
    }

    // Đọc danh sách cầu thủ từ file CSV
    public int docFileCSV(String filePath) {
        Path p = Paths.get(filePath);
        if (!Files.exists(p)) return 0;
        int count = 0;
        char sep = ';';
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {
                    first = false;
                    if (line.toLowerCase(Locale.ROOT).startsWith("loai")) continue; // header
                }
                List<String> parts = CauThu.parseCSVLine(line, sep);
                if (parts.size() < 8) continue;
                String loai = parts.get(0);
                String ten = parts.get(1);
                Integer soAo;
                try {
                    soAo = Integer.parseInt(parts.get(2));
                } catch (Exception e) {
                    continue;
                }
                String viTri = parts.get(3);
                TinhTrang tt = TinhTrang.fromString(parts.get(4));
                boolean daChinh = Boolean.parseBoolean(parts.get(5));
                boolean khongThiDau = Boolean.parseBoolean(parts.get(6));
                Double luong;
                try {
                    luong = Double.parseDouble(parts.get(7));
                } catch (Exception e) {
                    luong = 0.0;
                }
                Double chieuCao = null, canNang = null;
                LocalDate ngaySinh = null;
                if (parts.size() > 8) {
                    try {
                        chieuCao = parts.get(8).isEmpty() ? null : Double.parseDouble(parts.get(8));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.size() > 9) {
                    try {
                        canNang = parts.get(9).isEmpty() ? null : Double.parseDouble(parts.get(9));
                    } catch (Exception ignored) {
                    }
                }
                if (parts.size() > 10) {
                    try {
                        ngaySinh = parts.get(10).isEmpty() ? null : LocalDate.parse(parts.get(10), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ignored) {
                    }
                }
                if (soAo <= 0 || luong < 0) {
                    System.err.println("Bỏ qua dòng do dữ liệu không hợp lệ (số áo/lương âm hoặc 0): " + line);
                    continue;
                }
                CauThu ct;
                String loaiNorm = loai.trim().toLowerCase(Locale.ROOT);
                switch (loaiNorm) {
                    case "thumon":
                        ct = new ThuMon(ten, soAo, tt, daChinh, khongThiDau, luong);
                        break;
                    case "hauve":
                        ct = new HauVe(ten, soAo, tt, daChinh, khongThiDau, luong);
                        break;
                    case "tienve":
                        ct = new TienVe(ten, soAo, tt, daChinh, khongThiDau, luong);
                        break;
                    case "tiendao":
                        ct = new TienDao(ten, soAo, tt, daChinh, khongThiDau, luong);
                        break;
                    default:
                        ct = new CauThu(ten, soAo, viTri, tt, daChinh, khongThiDau, luong);
                }
                try {
                    if (chieuCao != null) ct.setChieuCao(chieuCao);
                    if (canNang != null) ct.setCanNang(canNang);
                    if (ngaySinh != null) ct.setNgaySinh(ngaySinh);
                    themCauThu(ct);
                    count++;
                } catch (DuplicateSoAoException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file: " + e.getMessage());
        }
        return count;
    }

    // Ghi danh sách cầu thủ ra file CSV
    public void ghiFileCSV(String filePath) {
        Path p = Paths.get(filePath);
        try {
            if (p.getParent() != null) Files.createDirectories(p.getParent());
        } catch (IOException e) {
            System.err.println("Không tạo được thư mục lưu file: " + e.getMessage());
        }
        char sep = ';';
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            bw.write(CauThu.csvHeader(sep));
            bw.newLine();
            for (CauThu c : danhSach) {
                bw.write(c.toCSVRow(sep));
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + e.getMessage());
        }
    }

    // Exception cho số áo trùng
    public static class DuplicateSoAoException extends Exception {
        public DuplicateSoAoException(int soAo) {
            super("Số áo " + soAo + " đã tồn tại!");
        }
    }

    // Sắp xếp theo tiêu chí và thứ tự tăng/giảm
    public void sapXep(String criteria, boolean ascending) {
        Comparator<CauThu> comparator;
        switch (criteria) {
            case "Tên":
                comparator = Comparator.comparing(CauThu::getTen, Collator.getInstance(Locale.forLanguageTag("vi-VN")));
                break;
            case "Số áo":
                comparator = Comparator.comparingInt(CauThu::getSoAo);
                break;
            case "Lương":
                comparator = Comparator.comparingDouble(CauThu::getLuongThang);
                break;
            case "Vị trí":
                Map<String, Integer> order = Map.of(
                        "Thủ môn", 1,
                        "Hậu vệ", 2,
                        "Tiền vệ", 3,
                        "Tiền đạo", 4
                );
                comparator = Comparator.comparingInt(c -> order.getOrDefault(c.getViTri(), 99));
                break;
            default:
                comparator = Comparator.comparing(CauThu::getTen);
        }
        if (!ascending) comparator = comparator.reversed();
        danhSach.sort(comparator);
    }
}
