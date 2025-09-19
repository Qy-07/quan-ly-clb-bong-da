package qlclb.model;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringJoiner;

// Lớp cơ sở cho cầu thủ, chứa thông tin chung và phương thức CSV
public class CauThu {
    private String ten;
    private int soAo;
    private String viTri; // Thủ môn, Hậu vệ, Tiền vệ, Tiền đạo
    private TinhTrang tinhTrang; // Khỏe/Chấn thương/Nghỉ phép
    private boolean daChinh; // true=đá chính, false=dự bị
    private boolean khongThiDau; // true=không thi đấu
    private double luongThang;

    // Bổ sung thông tin chi tiết
    private Double chieuCao; // cm
    private Double canNang;  // kg
    private LocalDate ngaySinh; // Ngày sinh

    // Constructor với validation
    public CauThu(String ten, int soAo, String viTri, TinhTrang tinhTrang, boolean daChinh, boolean khongThiDau, double luongThang) {
        setTen(ten);
        setSoAo(soAo);
        setViTri(viTri);
        setTinhTrang(tinhTrang);
        setDaChinh(daChinh);
        this.khongThiDau = khongThiDau;
        setLuongThang(luongThang);
        this.ngaySinh = null;
    }

    public String getLoai() { return "CauThu"; }

    public String getTen() { return ten; }
    public void setTen(String ten) {
        if (ten == null || ten.trim().isEmpty()) throw new IllegalArgumentException("Tên không được để trống");
        this.ten = ten.trim();
    }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public int getTuoi() {
        if (ngaySinh == null) return -1;
        return Period.between(ngaySinh, LocalDate.now()).getYears();
    }

    public int getSoAo() { return soAo; }
    public void setSoAo(int soAo) {
        if (soAo <= 0) throw new IllegalArgumentException("Số áo phải > 0");
        this.soAo = soAo;
    }

    public String getViTri() { return viTri; }
    public void setViTri(String viTri) {
        if (viTri == null || viTri.trim().isEmpty()) throw new IllegalArgumentException("Vị trí không được để trống");
        this.viTri = chuanHoaViTri(viTri);
    }

    // Chuẩn hóa tên vị trí từ nhiều cách viết
    private static String chuanHoaViTri(String viTri) {
        String s = viTri.trim().toLowerCase(Locale.ROOT);
        if (s.contains("mon")) return "Thủ môn";
        if (s.contains("hậu") || s.contains("hau")) return "Hậu vệ";
        if (s.contains("tiền v") || s.contains("tien v")) return "Tiền vệ";
        if (s.contains("đạo") || s.contains("dao")) return "Tiền đạo";
        return Character.toUpperCase(viTri.charAt(0)) + viTri.substring(1);
    }

    public TinhTrang getTinhTrang() { return tinhTrang; }
    public void setTinhTrang(TinhTrang tinhTrang) { this.tinhTrang = (tinhTrang == null ? TinhTrang.KHOE : tinhTrang); }

    public boolean isDaChinh() { return daChinh; }
    public void setDaChinh(boolean daChinh) { this.daChinh = daChinh; }

    public boolean isKhongThiDau() { return khongThiDau; }
    public void setKhongThiDau(boolean khongThiDau) { this.khongThiDau = khongThiDau; }

    public double getLuongThang() { return luongThang; }
    public void setLuongThang(double luongThang) {
        if (luongThang < 0) throw new IllegalArgumentException("Lương tháng không được âm");
        this.luongThang = luongThang;
    }

    public Double getChieuCao() { return chieuCao; }
    public void setChieuCao(Double chieuCao) {
        if (chieuCao != null && chieuCao <= 0) throw new IllegalArgumentException("Chiều cao phải > 0");
        this.chieuCao = chieuCao;
    }

    public Double getCanNang() { return canNang; }
    public void setCanNang(Double canNang) {
        if (canNang != null && canNang <= 0) throw new IllegalArgumentException("Cân nặng phải > 0");
        this.canNang = canNang;
    }

    // Xuất dữ liệu cầu thủ ra dòng CSV
    public String toCSVRow(char sep) {
        StringJoiner j = new StringJoiner(String.valueOf(sep));
        j.add(escapeCSV(getLoai(), sep));
        j.add(escapeCSV(getTen(), sep));
        j.add(String.valueOf(getSoAo()));
        j.add(escapeCSV(getViTri(), sep));
        j.add(escapeCSV(getTinhTrang().name(), sep));
        j.add(String.valueOf(isDaChinh()));
        j.add(String.valueOf(isKhongThiDau()));
        j.add(String.valueOf(getLuongThang()));
        // thêm chiều cao, cân nặng có thể null
        j.add(getChieuCao() == null ? "" : String.valueOf(getChieuCao()));
        j.add(getCanNang() == null ? "" : String.valueOf(getCanNang()));
        // thêm ngày sinh
        j.add(getNgaySinh() == null ? "" : getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        return j.toString();
    }

    public static String csvHeader(char sep) {
        return String.join(String.valueOf(sep),
                "loai", "ten", "soAo", "viTri", "tinhTrang", "daChinh", "khongThiDau", "luongThang", "chieuCao", "canNang", "ngaySinh");
    }

    public static String escapeCSV(String s, char sep) {
        if (s == null) return "";
        boolean needQuote = s.indexOf(sep) >= 0 || s.indexOf('"') >= 0 || s.contains("\n") || s.contains("\r");
        String out = s.replace("\"", "\"\"");
        return needQuote ? ('"' + out + '"') : out;
    }

    // Phân tích dòng CSV thành list các trường, hỗ trợ quote
    public static List<String> parseCSVLine(String line, char sep) {
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++; // skip escaped quote
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == sep && !inQuotes) {
                res.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        res.add(sb.toString());
        return res;
    }

    @Override
    public String toString() {
        String cc = (chieuCao == null ? "?" : String.format(Locale.US, "%.1fcm", chieuCao));
        String cn = (canNang == null ? "?" : String.format(Locale.US, "%.1fkg", canNang));
        return new StringBuilder()
            .append("[" + getLoai() + "] ")
            .append("Tên: ").append(ten)
            .append(", Số áo: ").append(soAo)
            .append(", Vị trí: ").append(viTri)
            .append(", Tình trạng: ").append(tinhTrang)
            .append(", ").append(daChinh ? "Đá chính" : "Dự bị")
            .append(", Lương: ").append(String.format(Locale.US, "%.0f", luongThang))
            .append(", Chiều cao: ").append(cc)
            .append(", Cân nặng: ").append(cn)
            .toString();
    }
}
