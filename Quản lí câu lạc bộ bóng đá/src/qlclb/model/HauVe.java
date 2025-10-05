package qlclb.model;

// Lớp con cho cầu thủ hậu vệ
public class HauVe extends CauThu {
    // Constructor cho hậu vệ
    public HauVe(String ten, int soAo, TinhTrang tinhTrang, boolean daChinh, boolean khongThiDau, double luongThang) {
        super(ten, soAo, "Hậu vệ", tinhTrang, daChinh, khongThiDau, luongThang);
    }
    @Override public String getLoai() { return "HauVe"; }
}
