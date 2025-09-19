// Lớp quản lý đội bóng, chứa thông tin đội và quản lý cầu thủ
package qlclb.service;

public class QuanLyDoiBong {
    private String tenDoi;
    private String hlv;
    private QuanLyCauThu quanLyCauThu = new QuanLyCauThu();

    // Constructor khởi tạo đội bóng
    public QuanLyDoiBong(String tenDoi, String hlv) { this.tenDoi = tenDoi; this.hlv = hlv; }

    // Getter và setter cho tên đội
    public String getTenDoi() { return tenDoi; }
    public void setTenDoi(String tenDoi) { this.tenDoi = tenDoi; }

    // Getter và setter cho HLV
    public String getHlv() { return hlv; }
    public void setHlv(String hlv) { this.hlv = hlv; }

    // Lấy đối tượng quản lý cầu thủ
    public QuanLyCauThu getQuanLyCauThu() { return quanLyCauThu; }
}
