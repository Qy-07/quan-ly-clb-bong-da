package qlclb.model;

// Lớp con cho cầu thủ tiền đạo
public class TienDao extends CauThu {
    // Constructor cho tiền đạo
    public TienDao(String ten, int soAo, TinhTrang tinhTrang, boolean daChinh, boolean khongThiDau, double luongThang) {
        super(ten, soAo, "Tiền đạo", tinhTrang, daChinh, khongThiDau, luongThang);
    }
    @Override public String getLoai() { return "TienDao"; }
}
