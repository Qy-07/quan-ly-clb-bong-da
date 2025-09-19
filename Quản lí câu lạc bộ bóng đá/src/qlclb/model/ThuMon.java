package qlclb.model;

// Lớp con cho cầu thủ thủ môn
public class ThuMon extends CauThu {
    // Constructor cho thủ môn
    public ThuMon(String ten, int soAo, TinhTrang tinhTrang, boolean daChinh, boolean khongThiDau, double luongThang) {
        super(ten, soAo, "Thủ môn", tinhTrang, daChinh, khongThiDau, luongThang);
    }
    @Override public String getLoai() { return "ThuMon"; }
}
