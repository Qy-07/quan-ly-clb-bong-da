package qlclb.model;

// Lớp con cho cầu thủ tiền vệ
public class TienVe extends CauThu {
    // Constructor cho tiền vệ
    public TienVe(String ten, int soAo, TinhTrang tinhTrang, boolean daChinh, boolean khongThiDau, double luongThang) {
        super(ten, soAo, "Tiền vệ", tinhTrang, daChinh, khongThiDau, luongThang);
    }
    @Override public String getLoai() { return "TienVe"; }
}
