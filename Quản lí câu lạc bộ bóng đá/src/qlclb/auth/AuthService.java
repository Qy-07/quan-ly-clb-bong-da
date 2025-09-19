package qlclb.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

// Lớp dịch vụ xác thực người dùng, quản lý đăng ký, đăng nhập và lưu trữ người dùng
public class AuthService {
    private static final String USERS_FILE = "data/users.csv";
    private static final char SEP = ';';

    private final Map<String, User> users = new HashMap<>();
    private String currentUser;

    // Constructor khởi tạo dịch vụ và tải danh sách người dùng từ file
    public AuthService() {
        loadUsers();
    }

    public Optional<String> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isLoggedIn() { return currentUser != null; }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        User u = users.get(username.toLowerCase(Locale.ROOT));
        if (u == null) return false;
        String hash = hashPassword(password, base64Decode(u.getSaltB64()));
        if (u.getPwdHashB64().equals(Base64.getEncoder().encodeToString(hash.getBytes(StandardCharsets.UTF_8)))) {
            currentUser = u.getUsername();
            return true;
        }
        return false;
    }

    public void logout() { currentUser = null; }

    // Đăng ký người dùng mới với username và password
    public boolean register(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.length() < 4)
            return false;
        String key = username.toLowerCase(Locale.ROOT);
        if (users.containsKey(key)) return false;
        byte[] salt = generateSalt();
        String hash = hashPassword(password, salt);
        User u = new User(username.trim(), Base64.getEncoder().encodeToString(salt), Base64.getEncoder().encodeToString(hash.getBytes(StandardCharsets.UTF_8)), Instant.now().toString());
        users.put(key, u);
        saveUsers();
        return true;
    }

    private void loadUsers() {
        Path p = Paths.get(USERS_FILE);
        users.clear();
        if (!Files.exists(p)) return;
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line; boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) { first = false; if (line.startsWith("username")) continue; }
                String[] parts = split(line, SEP, 4);
                if (parts.length < 4) continue;
                User u = new User(parts[0], parts[1], parts[2], parts[3]);
                users.put(parts[0].toLowerCase(Locale.ROOT), u);
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc users: " + e.getMessage());
        }
    }

    private void saveUsers() {
        Path p = Paths.get(USERS_FILE);
        try { if (p.getParent() != null) Files.createDirectories(p.getParent()); } catch (IOException ignored) {}
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            bw.write(User.csvHeader(SEP)); bw.newLine();
            for (User u : users.values()) { bw.write(u.toCSVRow(SEP)); bw.newLine(); }
        } catch (IOException e) {
            System.err.println("Lỗi ghi users: " + e.getMessage());
        }
    }

    private static String[] split(String s, char sep, int expected) {
        List<String> res = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQ = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') { inQ = !inQ; }
            else if (c == sep && !inQ) { res.add(sb.toString()); sb.setLength(0); }
            else sb.append(c);
        }
        res.add(sb.toString());
        return res.toArray(new String[0]);
    }

    private static byte[] generateSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] digest = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : digest) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] base64Decode(String b64) {
        try {
            return Base64.getDecoder().decode(b64);
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
