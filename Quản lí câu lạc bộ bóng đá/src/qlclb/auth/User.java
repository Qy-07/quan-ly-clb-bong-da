package qlclb.auth;

import java.util.StringJoiner;

/**
 * Lớp đại diện người dùng với thông tin username, salt, hash mật khẩu và thời gian tạo
 */
public class User {
    private String username;
    private String saltB64;
    private String pwdHashB64;
    private String createdAt; // ISO-8601 string

    /**
     * Khởi tạo người dùng mới
     * @param username tên đăng nhập
     * @param saltB64 chuỗi salt mã hóa base64
     * @param pwdHashB64 chuỗi hash mật khẩu mã hóa base64
     * @param createdAt thời gian tạo theo chuẩn ISO-8601
     */
    public User(String username, String saltB64, String pwdHashB64, String createdAt) {
        this.username = username;
        this.saltB64 = saltB64;
        this.pwdHashB64 = pwdHashB64;
        this.createdAt = createdAt;
    }

    public String getUsername() { return username; }
    public String getSaltB64() { return saltB64; }
    public String getPwdHashB64() { return pwdHashB64; }
    public String getCreatedAt() { return createdAt; }

    /**
     * Chuyển đối tượng người dùng thành dòng CSV
     * @param sep ký tự phân tách
     * @return chuỗi CSV
     */
    public String toCSVRow(char sep) {
        StringJoiner j = new StringJoiner(String.valueOf(sep));
        j.add(username == null ? "" : username);
        j.add(saltB64 == null ? "" : saltB64);
        j.add(pwdHashB64 == null ? "" : pwdHashB64);
        j.add(createdAt == null ? "" : createdAt);
        return j.toString();
    }

    /**
     * Trả về header CSV cho file người dùng
     * @param sep ký tự phân tách
     * @return chuỗi header CSV
     */
    public static String csvHeader(char sep) {
        return String.join(String.valueOf(sep), "username", "salt", "pwdHash", "createdAt");
    }
}
