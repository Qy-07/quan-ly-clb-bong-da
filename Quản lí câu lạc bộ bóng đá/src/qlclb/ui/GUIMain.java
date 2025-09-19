package qlclb.ui;

import qlclb.model.*;
import qlclb.service.QuanLyCauThu;
import qlclb.service.QuanLyDoiBong;
import qlclb.auth.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.swing.UIManager;

/**
 * Lớp giao diện chính quản lý câu lạc bộ bóng đá
 * Chứa các thành phần GUI và xử lý sự kiện
 * Lưu ý: Bảng GUI (JTable) giữ nguyên không thay đổi gì
 */
public class GUIMain {
    private static final String DATA_FILE = "data/cau_thu.csv";
    private static final String TROLY_FILE = "data/troly.csv";
    private QuanLyDoiBong doiBong;
    private QuanLyCauThu ql;
    private AuthService auth;
    private JFrame mainFrame;
    private JTable mainTable;
    private DefaultTableModel tableModel;
    private List<TroLy> troLyList = new ArrayList<>();
    private enum View { PLAYERS, ASSISTANTS }
    private View currentView = View.PLAYERS;
    private JTextField searchField;
    private JComboBox<String> sortCombo = new JComboBox<>();
    private JButton[] btns;

    /**
     * Lớp nội trợ lý với thông tin cơ bản
     */
    private static class TroLy {
        private String hoTen, chucVu;
        private LocalDate ngaySinh;
        private double luong;

        public TroLy(String hoTen, LocalDate ngaySinh, String chucVu, double luong) {
            this.hoTen = hoTen;
            this.ngaySinh = ngaySinh;
            this.chucVu = chucVu;
            this.luong = luong;
        }

        public String getHoTen() { return hoTen; }
        public LocalDate getNgaySinh() { return ngaySinh; }
        public String getChucVu() { return chucVu; }
        public double getLuong() { return luong; }
    }

    // Tải danh sách trợ lý từ file CSV
    private void loadTroLyList() {
        Path p = Paths.get(TROLY_FILE);
        if (!Files.exists(p)) {
            // Nếu file không tồn tại, thêm dữ liệu mặc định
            troLyList.add(new TroLy("Nguyễn Văn HLV", LocalDate.of(1970,5,10), "Huấn luyện viên", 100000000));
            troLyList.add(new TroLy("Trần Thị TL", LocalDate.of(1985,3,15), "Trợ lý HLV", 60000000));
            return;
        }
        try (BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8)) {
            String line;
            boolean first = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                if (first) {
                    first = false;
                    continue; // Bỏ qua header
                }
                String[] parts = line.split(";");
                if (parts.length < 4) continue;
                String hoTen = parts[0].trim();
                LocalDate ngaySinh = null;
                if (!parts[1].trim().isEmpty()) {
                    try {
                        ngaySinh = LocalDate.parse(parts[1].trim(), DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception ignored) {}
                }
                String chucVu = parts[2].trim();
                double luong = 0;
                try {
                    luong = Double.parseDouble(parts[3].trim());
                } catch (Exception ignored) {}
                troLyList.add(new TroLy(hoTen, ngaySinh, chucVu, luong));
            }
        } catch (IOException e) {
            System.err.println("Lỗi đọc file trợ lý: " + e.getMessage());
        }
    }

    // Lưu danh sách trợ lý ra file CSV
    private void saveTroLyList() {
        Path p = Paths.get(TROLY_FILE);
        try {
            if (p.getParent() != null) Files.createDirectories(p.getParent());
        } catch (IOException e) {
            System.err.println("Không tạo được thư mục lưu file trợ lý: " + e.getMessage());
        }
        try (BufferedWriter bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8)) {
            bw.write("Họ tên;Ngày sinh;Chức vụ;Lương");
            bw.newLine();
            for (TroLy tl : troLyList) {
                String ngaySinhStr = tl.getNgaySinh() != null ? tl.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                bw.write(tl.getHoTen() + ";" + ngaySinhStr + ";" + tl.getChucVu() + ";" + tl.getLuong());
                bw.newLine();
            }
        } catch (IOException e) {
            System.err.println("Lỗi ghi file trợ lý: " + e.getMessage());
        }
    }

    /**
     * Phương thức main khởi chạy ứng dụng GUI
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIMain::new);
    }

    /**
     * Constructor khởi tạo GUI chính
     * Thiết lập LookAndFeel, tải dữ liệu, hiển thị dialog đăng nhập
     */
    public GUIMain() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {}
        doiBong = new QuanLyDoiBong("CLB 1", "HLV 1");
        ql = doiBong.getQuanLyCauThu();
        auth = new AuthService();
        int n = ql.docFileCSV(DATA_FILE);
        if (n > 0) System.out.println("Đã tải " + n + " cầu thủ");
        loadTroLyList();
        if (showLoginDialog()) createMainGUI();
        else System.exit(0);
    }

    // Tạo dialog với cấu hình cơ bản
    private JDialog createDialog(Frame parent, String title, boolean modal, int width, int height) {
        JDialog d = new JDialog(parent, title, modal);
        d.setSize(width, height);
        d.setLocationRelativeTo(parent);
        d.getContentPane().setBackground(new Color(245, 245, 250));
        return d;
    }

    // Thiết lập GridBagConstraints
    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y) {
        gbc.gridx = x;
        gbc.gridy = y;
        return gbc;
    }

    // Hiển thị dialog đăng nhập
    private boolean showLoginDialog() {
        JDialog d = createDialog(null, "Đăng nhập/Đăng ký", true, 400, 200);
        d.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        JTextField u = new JTextField(20);
        JPasswordField p = new JPasswordField(20);
        JButton regBtn = new JButton("Đăng ký");
        JButton loginBtn = new JButton("Đăng nhập");
        JLabel l = new JLabel("");
        boolean[] loggedIn = {false};
        d.add(new JLabel("Tên đăng nhập:"), setGbc(gbc, 0, 0));
        d.add(u, setGbc(gbc, 1, 0));
        d.add(new JLabel("Mật khẩu:"), setGbc(gbc, 0, 1));
        d.add(p, setGbc(gbc, 1, 1));
        d.add(regBtn, setGbc(gbc, 0, 2));
        d.add(loginBtn, setGbc(gbc, 1, 2));
        d.add(l, setGbc(gbc, 0, 3, 2, 1));
        regBtn.addActionListener(e -> showRegisterDialog());
        loginBtn.addActionListener(e -> {
            String user = u.getText().trim(), pass = new String(p.getPassword());
            if (user.isEmpty()) {
                l.setText("Vui lòng nhập tên đăng nhập.");
                return;
            }
            if (pass.isEmpty()) {
                l.setText("Vui lòng nhập mật khẩu.");
                return;
            }
            if (pass.length() < 4) {
                l.setText("Mật khẩu phải có ít nhất 4 ký tự.");
                return;
            }
            if (auth.login(user, pass)) {
                loggedIn[0] = true;
                d.dispose();
            } else {
                l.setText("Đăng nhập thất bại. Kiểm tra tên đăng nhập và mật khẩu.");
            }
        });
        d.setVisible(true);
        return loggedIn[0];
    }

    // Hiển thị dialog đăng ký
    private void showRegisterDialog() {
        JDialog d = createDialog(null, "Đăng ký", true, 400, 200);
        d.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        JTextField u = new JTextField(20);
        JPasswordField p = new JPasswordField(20);
        JButton regBtn = new JButton("Đăng ký");
        JButton cancelBtn = new JButton("Hủy");
        JLabel l = new JLabel("");
        d.add(new JLabel("Tên đăng nhập:"), setGbc(gbc, 0, 0));
        d.add(u, setGbc(gbc, 1, 0));
        d.add(new JLabel("Mật khẩu:"), setGbc(gbc, 0, 1));
        d.add(p, setGbc(gbc, 1, 1));
        d.add(regBtn, setGbc(gbc, 0, 2));
        d.add(cancelBtn, setGbc(gbc, 1, 2));
        d.add(l, setGbc(gbc, 0, 3, 2, 1));
        regBtn.addActionListener(e -> {
            String user = u.getText().trim(), pass = new String(p.getPassword());
            if (user.isEmpty()) {
                l.setText("Vui lòng nhập tên đăng nhập.");
                return;
            }
            if (pass.isEmpty()) {
                l.setText("Vui lòng nhập mật khẩu.");
                return;
            }
            if (pass.length() < 4) {
                l.setText("Mật khẩu phải có ít nhất 4 ký tự.");
                return;
            }
            if (auth.register(user, pass)) {
                auth.login(user, pass);
                l.setText("Đăng ký và đăng nhập thành công!");
                d.dispose();
            } else {
                l.setText("Đăng ký thất bại (tài khoản đã tồn tại hoặc dữ liệu không hợp lệ).");
            }
        });
        cancelBtn.addActionListener(e -> d.dispose());
        d.pack();
        d.setMinimumSize(new Dimension(400, 200));
        d.setLocationRelativeTo(null);
        d.setVisible(true);
    }

    private GridBagConstraints setGbc(GridBagConstraints gbc, int x, int y, int w, int h) {
        gbc.gridx = x;
        gbc.gridy = y;
        gbc.gridwidth = w;
        gbc.gridheight = h;
        return gbc;
    }

    private void createMainGUI() {
        mainFrame = new JFrame("Quản lý câu lạc bộ bóng đá");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setSize(1600, 700);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.getContentPane().setBackground(new Color(240, 240, 240)); // Nền màu xám nhạt

        // Header panel
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(new Color(44, 62, 80)); // Màu xanh đậm
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(44, 62, 80));
        JLabel titleLabel = new JLabel("Quản lý CLB Bóng đá");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        headerPanel.add(titlePanel);

        // Bảng tìm kiếm và sắp xếp
        JPanel controlPanel = new JPanel();
        controlPanel.setBackground(new Color(44, 62, 80));
        controlPanel.setLayout(new FlowLayout(FlowLayout.LEFT));

        // Tìm kiếm
        JLabel searchLabel = new JLabel("Tìm kiếm:");
        searchLabel.setForeground(Color.WHITE);
        searchLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        this.searchField = new JTextField(15);
        JButton searchButton = new JButton("Tìm");
        searchButton.setBackground(Color.WHITE);
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ActionListener searchListener = e -> {
            String query = this.searchField.getText().trim();
            if (!query.isEmpty()) {
                if (currentView == View.PLAYERS) {
                    List<CauThu> results = ql.timTheoTen(query);
                    if (results.isEmpty()) JOptionPane.showMessageDialog(mainFrame, "Không tìm thấy cầu thủ nào.");
                    else {
                        StringBuilder sb = new StringBuilder("Cầu thủ tìm thấy:\n");
                        results.forEach(ct -> sb.append(ct.getTen()).append(" (").append(ct.getSoAo()).append(")\n"));
                        JOptionPane.showMessageDialog(mainFrame, sb.toString());
                    }
                } else {
                    List<TroLy> results = troLyList.stream().filter(tl -> tl.getHoTen().toLowerCase().contains(query.toLowerCase())).collect(Collectors.toList());
                    if (results.isEmpty()) JOptionPane.showMessageDialog(mainFrame, "Không tìm thấy trợ lý nào.");
                    else {
                        StringBuilder sb = new StringBuilder("Trợ lý tìm thấy:\n");
                        results.forEach(tl -> sb.append(tl.getHoTen()).append(" (").append(tl.getChucVu()).append(")\n"));
                        JOptionPane.showMessageDialog(mainFrame, sb.toString());
                    }
                }
            }
        };
        this.searchField.addActionListener(searchListener);
        searchButton.addActionListener(searchListener);

        // Sắp xếp
        JLabel sortLabel = new JLabel("Sắp xếp:");
        sortLabel.setForeground(Color.WHITE);
        sortLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        this.sortCombo.setBackground(Color.WHITE);
        this.sortCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        ActionListener sortListener = e -> {
            String choice = (String) this.sortCombo.getSelectedItem();
            if (choice != null) {
                if (currentView == View.PLAYERS) {
                    switch (choice) {
                        case "Tên": ql.sapXepTheoTen(); break;
                        case "Số áo": ql.sapXepTheoSoAo(); break;
                        case "Lương": ql.getDanhSach().sort((a, b) -> Double.compare(b.getLuongThang(), a.getLuongThang())); break;
                        case "Vị trí": ql.getDanhSach().sort((a, b) -> a.getViTri().compareTo(b.getViTri())); break;
                    }
                } else {
                    switch (choice) {
                        case "Họ tên": troLyList.sort((a, b) -> a.getHoTen().compareTo(b.getHoTen())); break;
                        case "Chức vụ": troLyList.sort((a, b) -> a.getChucVu().compareTo(b.getChucVu())); break;
                        case "Lương": troLyList.sort((a, b) -> Double.compare(b.getLuong(), a.getLuong())); break;
                    }
                }
                refreshTable();
            }
        };
        this.sortCombo.addActionListener(sortListener);

        controlPanel.add(searchLabel);
        controlPanel.add(searchField);
        controlPanel.add(searchButton);
        controlPanel.add(Box.createHorizontalStrut(20));
        controlPanel.add(sortLabel);
        controlPanel.add(sortCombo);

        // Ban cầu thủ button
        JButton banCauThuButton = new JButton("Ban cầu thủ");
        controlPanel.add(banCauThuButton);

        // Ban trợ lý button
        JButton banTroLyButton = new JButton("Ban trợ lý");
        controlPanel.add(banTroLyButton);

        banCauThuButton.addActionListener(e -> {
            currentView = View.PLAYERS;
            refreshTable();
        });
        banTroLyButton.addActionListener(e -> {
            currentView = View.ASSISTANTS;
            refreshTable();
        });

        headerPanel.add(controlPanel);
        mainFrame.add(headerPanel, BorderLayout.NORTH);

        // Thiết lập bảng
        String[] cols = {"STT", "Tên", "Ngày sinh", "Số áo", "Vị trí", "Tình trạng", "Đá chính", "Dự bị", "Không thi đấu", "Lương (VND)/tháng"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                if (currentView == View.ASSISTANTS) return false;
                if (c == 6) {
                    // Editable if "Dự bị" and "Không thi đấu" are false (so "Đá chính" can be checked only if others are not checked)
                    return !(Boolean) getValueAt(r, 7) && !(Boolean) getValueAt(r, 8);
                } else if (c == 7) {
                    // Editable if "Đá chính" and "Không thi đấu" are false (so "Dự bị" can be checked only if others are not checked)
                    return !(Boolean) getValueAt(r, 6) && !(Boolean) getValueAt(r, 8);
                } else if (c == 8) {
                    // Editable if "Đá chính" and "Dự bị" are false (so "Không thi đấu" can be checked only if others are not checked)
                    return !(Boolean) getValueAt(r, 6) && !(Boolean) getValueAt(r, 7);
                }
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                if (currentView == View.ASSISTANTS) return super.getColumnClass(column);
                if (column == 6 || column == 7 || column == 8) {
                    return Boolean.class;
                }
                return super.getColumnClass(column);
            }
        };
        mainTable = new JTable(tableModel);
        mainTable.setRowHeight(30);
        mainTable.setBackground(Color.WHITE);
        mainTable.setGridColor(new Color(223, 230, 233)); // Light gray grid
        mainTable.setShowGrid(true);
        mainTable.setShowVerticalLines(true);
        mainTable.setShowHorizontalLines(true);
        mainTable.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        mainTable.getTableHeader().setBackground(new Color(223, 230, 233)); // Gray header
        mainTable.getTableHeader().setForeground(Color.BLACK);
        mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        mainTable.getTableHeader().setReorderingAllowed(false);
        mainTable.getTableHeader().setResizingAllowed(false);
        mainTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249)); // Các hàng sọc
                }
                return c;
            }
        });
        // Custom renderer for boolean columns to show checkbox centered, always visible but disabled if not editable
        DefaultTableCellRenderer checkboxRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Boolean checked = (Boolean) value;
                boolean editable = table.isCellEditable(row, column);
                JPanel panel = new JPanel(new GridBagLayout());
                panel.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(249, 249, 249)));
                JCheckBox cb = new JCheckBox();
                cb.setSelected(checked);
                cb.setEnabled(editable);
                cb.setHorizontalAlignment(SwingConstants.CENTER);
                panel.add(cb);
                return panel;
            }
        };
        mainTable.getColumnModel().getColumn(6).setCellRenderer(checkboxRenderer);
        mainTable.getColumnModel().getColumn(7).setCellRenderer(checkboxRenderer);
        mainTable.getColumnModel().getColumn(8).setCellRenderer(checkboxRenderer);
        JScrollPane scrollPane = new JScrollPane(mainTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainFrame.add(scrollPane, BorderLayout.CENTER);

        // Thêm mouse listener để hiển thị chi tiết cầu thủ hoặc trợ lý khi click vào tên
        mainTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = mainTable.rowAtPoint(e.getPoint());
                int col = mainTable.columnAtPoint(e.getPoint());
                if (col == 1 && row >= 0) { // Cột tên
                    if (currentView == View.PLAYERS) {
                        CauThu ct = ql.getDanhSach().get(row);
                        showPlayerDetailsDialog(ct);
                    } else if (currentView == View.ASSISTANTS) {
                        TroLy tl = troLyList.get(row);
                        showAssistantDetailsDialog(tl);
                    }
                }
            }
        });

        // Thêm trình nghe để cập nhật mô hình khi thay đổi hộp kiểm
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (currentView == View.PLAYERS && e.getType() == TableModelEvent.UPDATE) {
                    int row = e.getFirstRow();
                    int col = e.getColumn();
                    if (col == 6) { // Đá chính đã thay đổi
                        Boolean daChinh = (Boolean) tableModel.getValueAt(row, 6);
                        if (daChinh != null) {
                            // When "Đá chính" is checked, "Dự bị" and "Không thi đấu" must be unchecked
                            if (daChinh) {
                                tableModel.setValueAt(false, row, 7);
                                tableModel.setValueAt(false, row, 8);
                            }
                            CauThu ct = ql.getDanhSach().get(row);
                            ct.setDaChinh(daChinh);
                        }
                    } else if (col == 7) { // Dự bị đã thay đổi
                        Boolean duBi = (Boolean) tableModel.getValueAt(row, 7);
                        if (duBi != null) {
                            // When "Dự bị" is checked, "Đá chính" and "Không thi đấu" must be unchecked
                            if (duBi) {
                                tableModel.setValueAt(false, row, 6);
                                tableModel.setValueAt(false, row, 8);
                            }
                            CauThu ct = ql.getDanhSach().get(row);
                            ct.setDaChinh(!duBi);
                        }
                    } else if (col == 8) { // Không thi đấu đã thay đổi
                        Boolean khongThiDau = (Boolean) tableModel.getValueAt(row, 8);
                        if (khongThiDau != null) {
                            // When "Không thi đấu" is checked, "Đá chính" and "Dự bị" must be unchecked
                            if (khongThiDau) {
                                tableModel.setValueAt(false, row, 6);
                                tableModel.setValueAt(false, row, 7);
                            }
                            CauThu ct = ql.getDanhSach().get(row);
                            ct.setKhongThiDau(khongThiDau);
                        }
                    }
                }
            }
        });



        // Bảng nút (kiểu thanh bên)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        this.btns = new JButton[]{new JButton("Thêm"), new JButton("Sửa"), new JButton("Xóa"), new JButton("Thống kê"),
                          new JButton("Lưu"), new JButton("Đăng xuất")};
        for (int i = 0; i < this.btns.length; i++) {
            JButton btn = this.btns[i];
            btn.setPreferredSize(new Dimension(160, 45));
            btn.setMaximumSize(new Dimension(160, 45));
            buttonPanel.add(btn);
            buttonPanel.add(Box.createVerticalStrut(10));
        }
        this.btns[0].addActionListener(e -> {
            if (currentView == View.PLAYERS) {
                showPlayerDialog(null);
            } else if (currentView == View.ASSISTANTS) {
                showAssistantDialog(null);
            }
        });
        this.btns[1].addActionListener(e -> {
            int r = mainTable.getSelectedRow();
            if (r >= 0) {
                if (currentView == View.PLAYERS) {
                    showPlayerDialog(ql.getDanhSach().get(r));
                } else if (currentView == View.ASSISTANTS) {
                    showAssistantDialog(troLyList.get(r));
                }
            }
        });
        this.btns[2].addActionListener(e -> {
            int r = mainTable.getSelectedRow();
            if (r >= 0) {
                if (currentView == View.PLAYERS) {
                    if (JOptionPane.showConfirmDialog(mainFrame, "Xóa cầu thủ?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        ql.xoaTheoSoAo((int) tableModel.getValueAt(r, 3));
                        refreshTable();
                    }
                } else if (currentView == View.ASSISTANTS) {
                    if (JOptionPane.showConfirmDialog(mainFrame, "Xóa trợ lý?", "Xác nhận", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        troLyList.remove(r);
                        refreshTable();
                    }
                }
            }
        });
        this.btns[3].addActionListener(e -> showStatsDialog());
        this.btns[4].addActionListener(e -> { ql.ghiFileCSV(DATA_FILE); saveTroLyList(); JOptionPane.showMessageDialog(mainFrame, "Đã lưu vào " + DATA_FILE); });
        this.btns[5].addActionListener(e -> { auth.logout(); mainFrame.dispose(); new GUIMain(); });
        mainFrame.add(buttonPanel, BorderLayout.EAST);

        refreshTable();
        mainFrame.setVisible(true);
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        if (currentView == View.PLAYERS) {
            // Set columns for players
            String[] playerCols = {"STT", "Tên", "Ngày sinh", "Số áo", "Vị trí", "Tình trạng", "Đá chính", "Dự bị", "Không thi đấu", "Lương (VND)/tháng"};
            tableModel.setColumnIdentifiers(playerCols);
            // Adjust column widths for players
            int[] playerWidths = {40, 300, 120, 80, 120, 120, 100, 100, 150, 250};
            for (int i = 0; i < playerWidths.length; i++) {
                mainTable.getColumnModel().getColumn(i).setPreferredWidth(playerWidths[i]);
            }
            mainTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            // Add player rows
            List<CauThu> ps = ql.getDanhSach();
            for (int i = 0; i < ps.size(); i++) {
                CauThu ct = ps.get(i);
                String ngaySinhStr = ct.getNgaySinh() != null ? ct.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                tableModel.addRow(new Object[]{i + 1, ct.getTen(), ngaySinhStr, ct.getSoAo(), ct.getViTri(), ct.getTinhTrang(),
                                               ct.isDaChinh(), !ct.isDaChinh() && !ct.isKhongThiDau(), ct.isKhongThiDau(), String.format("%,.0f VND", ct.getLuongThang())});
            }
        } else if (currentView == View.ASSISTANTS) {
            // Set columns for assistants
            String[] assistantCols = {"STT", "Họ tên", "Ngày sinh", "Chức vụ", "Lương (VND)/tháng"};
            tableModel.setColumnIdentifiers(assistantCols);
            // Adjust column widths for assistants
            int[] assistantWidths = {40, 300, 120, 200, 250};
            for (int i = 0; i < assistantWidths.length; i++) {
                mainTable.getColumnModel().getColumn(i).setPreferredWidth(assistantWidths[i]);
            }
            // Add assistant rows
            for (int i = 0; i < troLyList.size(); i++) {
                TroLy tl = troLyList.get(i);
                String ngaySinhStr = tl.getNgaySinh() != null ? tl.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
                tableModel.addRow(new Object[]{i + 1, tl.getHoTen(), ngaySinhStr, tl.getChucVu(), String.format("%,.0f VND", tl.getLuong())});
            }
        }
        // Set sortCombo items based on view
        if (currentView == View.PLAYERS) {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Tên");
            model.addElement("Số áo");
            model.addElement("Lương");
            model.addElement("Vị trí");
            sortCombo.setModel(model);
        } else {
            DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
            model.addElement("Họ tên");
            model.addElement("Chức vụ");
            model.addElement("Lương");
            sortCombo.setModel(model);
        }
        // Set enabled state for controls
        searchField.setEnabled(true);
        sortCombo.setEnabled(true);
        btns[0].setEnabled(true);
        btns[1].setEnabled(true);
        btns[2].setEnabled(true);
        btns[3].setEnabled(true);
        btns[4].setEnabled(true);
        btns[5].setEnabled(true);
    }

    private void showPlayerDialog(CauThu editCt) {
        JDialog d = new JDialog(mainFrame, editCt == null ? "Thêm cầu thủ" : "Sửa cầu thủ", true);
        d.setLayout(new BorderLayout());
        d.setSize(450, 550);
        d.setLocationRelativeTo(mainFrame);
        d.getContentPane().setBackground(new Color(245, 245, 250));

        // Bảng tiêu đề
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(editCt == null ? "Thêm cầu thủ mới" : "Sửa thông tin cầu thủ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        d.add(titlePanel, BorderLayout.NORTH);

        // Bảng biểu mẫu
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 220), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField nameF = new JTextField(editCt != null ? editCt.getTen() : "", 15);
        JTextField jerseyF = new JTextField(editCt != null ? String.valueOf(editCt.getSoAo()) : "", 15);
        JComboBox<String> posC = new JComboBox<>(new String[]{"Thủ môn", "Hậu vệ", "Tiền vệ", "Tiền đạo"});
        if (editCt != null) posC.setSelectedItem(editCt.getViTri());
        JComboBox<TinhTrang> statusC = new JComboBox<>(TinhTrang.values());
        if (editCt != null) statusC.setSelectedItem(editCt.getTinhTrang());
        JCheckBox starterC = new JCheckBox("", editCt != null ? editCt.isDaChinh() : false);
        JCheckBox reserveC = new JCheckBox("", editCt != null ? (!editCt.isDaChinh() && !editCt.isKhongThiDau()) : false);
        JTextField salaryF = new JTextField(editCt != null ? String.format("%,.0f", editCt.getLuongThang()) : "", 15);
        JTextField heightF = new JTextField(editCt != null && editCt.getChieuCao() != null ? String.valueOf(editCt.getChieuCao()) : "", 15);
        JTextField weightF = new JTextField(editCt != null && editCt.getCanNang() != null ? String.valueOf(editCt.getCanNang()) : "", 15);

        // Thành phần kiểu dáng
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color fieldBg = new Color(249, 249, 249);

        String[] labels = {"Tên:", "Ngày sinh:", "Số áo:", "Vị trí:", "Tình trạng:", "Đá chính:", "Dự bị:", "Không thi đấu:", "Lương:", "Chiều cao:", "Cân nặng:"};
        JTextField ngaySinhF = new JTextField(editCt != null && editCt.getNgaySinh() != null ? editCt.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", 15);
        JCheckBox khongThiDauC = new JCheckBox("", editCt != null ? editCt.isKhongThiDau() : false);
        JComponent[] fields = {nameF, ngaySinhF, jerseyF, posC, statusC, starterC, reserveC, khongThiDauC, salaryF, heightF, weightF};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            label.setForeground(new Color(52, 73, 94));
            gbc.gridx = 0; gbc.gridy = i;
            formPanel.add(label, gbc);

            fields[i].setFont(fieldFont);
            if (fields[i] instanceof JTextField) {
                ((JTextField) fields[i]).setBackground(fieldBg);
                ((JTextField) fields[i]).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            } else if (fields[i] instanceof JComboBox) {
                ((JComboBox<?>) fields[i]).setBackground(fieldBg);
            }
            gbc.gridx = 1; gbc.gridy = i;
            formPanel.add(fields[i], gbc);
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        d.add(scrollPane, BorderLayout.CENTER);

        // Bảng nút
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton saveB = new JButton("Lưu");
        JButton cancelB = new JButton("Hủy");

        buttonPanel.add(saveB);
        buttonPanel.add(cancelB);
        d.add(buttonPanel, BorderLayout.SOUTH);

        saveB.addActionListener(e -> {
            if ((starterC.isSelected() && reserveC.isSelected()) ||
                (starterC.isSelected() && khongThiDauC.isSelected()) ||
                (reserveC.isSelected() && khongThiDauC.isSelected())) {
                JOptionPane.showMessageDialog(d, "Lỗi! Chỉ được chọn 1 trong 3: Đá chính, Dự bị, hoặc Không thi đấu!");
                return;
            }
            try {
                String name = nameF.getText().trim();
                LocalDate ngaySinh = null;
                String ngaySinhStr = ngaySinhF.getText().trim();
                if (!ngaySinhStr.isEmpty()) {
                    try {
                        ngaySinh = LocalDate.parse(ngaySinhStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception dateEx) {
                        JOptionPane.showMessageDialog(d, "Ngày sinh không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy (ví dụ: 10/10/1989).");
                        return;
                    }
                }
                int jersey = Integer.parseInt(jerseyF.getText().trim());
                String pos = (String) posC.getSelectedItem();
                TinhTrang status = (TinhTrang) statusC.getSelectedItem();
                boolean starter = starterC.isSelected();
                boolean khongThiDau = khongThiDauC.isSelected();
                double salary = Double.parseDouble(salaryF.getText().trim().replace(",", ""));
                Double height = heightF.getText().trim().isEmpty() ? null : Double.parseDouble(heightF.getText().trim());
                Double weight = weightF.getText().trim().isEmpty() ? null : Double.parseDouble(weightF.getText().trim());
                CauThu ct = createPlayer(pos, name, jersey, status, starter, khongThiDau, salary);
                if (ngaySinh != null) ct.setNgaySinh(ngaySinh);
                if (height != null) ct.setChieuCao(height);
                if (weight != null) ct.setCanNang(weight);
                if (editCt != null) {
                    ql.xoaTheoSoAo(editCt.getSoAo());
                }
                ql.themCauThu(ct);
                refreshTable();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });

        cancelB.addActionListener(e -> d.dispose());

        d.setVisible(true);
    }

    private void showPlayerDetailsDialog(CauThu ct) {
        JDialog d = new JDialog(mainFrame, "Chi tiết cầu thủ", true);
        d.setLayout(new BorderLayout());
        d.setSize(500, 600);
        d.setLocationRelativeTo(mainFrame);
        d.getContentPane().setBackground(new Color(245, 245, 250));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Thông tin chi tiết cầu thủ");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        d.add(titlePanel, BorderLayout.NORTH);

        // Details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 220), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 16);
        Color labelColor = new Color(52, 73, 94);
        Color valueColor = new Color(44, 62, 80);

        String[] labels = {"Tên:", "Ngày sinh:", "Tuổi:", "Số áo:", "Vị trí:", "Tình trạng:", "Trạng thái thi đấu:", "Lương/tháng:", "Chiều cao (cm):", "Cân nặng (kg):"};
        String ngaySinhStr = ct.getNgaySinh() != null ? ct.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa cập nhật";
        String tuoiStr = ct.getNgaySinh() != null ? String.valueOf(LocalDate.now().getYear() - ct.getNgaySinh().getYear()) : "Chưa cập nhật";
        String tinhTrangThiDau = ct.isDaChinh() ? "Đá chính" : (ct.isKhongThiDau() ? "Không thi đấu" : "Dự bị");
        String luongStr = String.format("%,.0f VND", ct.getLuongThang());
        String chieuCaoStr = ct.getChieuCao() != null ? String.valueOf(ct.getChieuCao()) : "Chưa cập nhật";
        String canNangStr = ct.getCanNang() != null ? String.valueOf(ct.getCanNang()) : "Chưa cập nhật";
        String[] values = {ct.getTen(), ngaySinhStr, tuoiStr, String.valueOf(ct.getSoAo()), ct.getViTri(), ct.getTinhTrang().toString(), tinhTrangThiDau, luongStr, chieuCaoStr, canNangStr};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            label.setForeground(labelColor);
            gbc.gridx = 0; gbc.gridy = i;
            detailsPanel.add(label, gbc);

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(valueFont);
            valueLabel.setForeground(valueColor);
            gbc.gridx = 1; gbc.gridy = i;
            detailsPanel.add(valueLabel, gbc);
        }

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        d.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> d.dispose());
        buttonPanel.add(closeBtn);
        d.add(buttonPanel, BorderLayout.SOUTH);

        d.setVisible(true);
    }

    private CauThu createPlayer(String pos, String name, int jersey, TinhTrang status, boolean starter, boolean khongThiDau, double salary) {
        switch (pos) {
            case "Thủ môn": return new ThuMon(name, jersey, status, starter, khongThiDau, salary);
            case "Hậu vệ": return new HauVe(name, jersey, status, starter, khongThiDau, salary);
            case "Tiền vệ": return new TienVe(name, jersey, status, starter, khongThiDau, salary);
            case "Tiền đạo": return new TienDao(name, jersey, status, starter, khongThiDau, salary);
            default: return new CauThu(name, jersey, pos, status, starter, khongThiDau, salary);
        }
    }

    private void showStatsDialog() {
        final JDialog[] d = new JDialog[1];
        DefaultTableModel statsModel = new DefaultTableModel(new String[]{"Thống kê", "Giá trị"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        if (currentView == View.PLAYERS) {
            List<CauThu> ps = ql.getDanhSach();
            Map<String, Long> posCount = ql.thongKeTheoViTri();
            Map<TinhTrang, Long> statusCount = ql.thongKeTheoTinhTrang();
            double avgSalary = ql.luongTrungBinh();

            d[0] = new JDialog(mainFrame, "Thống kê cầu thủ", true);

            // Thêm hàng cho cầu thủ
            statsModel.addRow(new Object[]{"Tổng số cầu thủ", ps.size()});
            for (Map.Entry<String, Long> e : posCount.entrySet()) {
                if (e.getValue() > 0) statsModel.addRow(new Object[]{"Vị trí " + e.getKey(), e.getValue()});
            }
            for (Map.Entry<TinhTrang, Long> e : statusCount.entrySet()) {
                if (e.getValue() > 0) statsModel.addRow(new Object[]{"Tình trạng " + e.getKey(), e.getValue()});
            }
            Map<Boolean, Long> daChinhStats = ql.thongKeDaChinh();
            long starterCount = daChinhStats.getOrDefault(true, 0L);
            long reserveCount = daChinhStats.getOrDefault(false, 0L);
            long khongThiDauCount = ql.thongKeKhongThiDau();
            statsModel.addRow(new Object[]{"Số cầu thủ đá chính", starterCount});
            statsModel.addRow(new Object[]{"Số cầu thủ dự bị", reserveCount});
            statsModel.addRow(new Object[]{"Số cầu thủ không thi đấu", khongThiDauCount});
            statsModel.addRow(new Object[]{"Lương trung bình", String.format("%,.0f VND", avgSalary)});
        } else if (currentView == View.ASSISTANTS) {
            d[0] = new JDialog(mainFrame, "Thống kê trợ lý", true);

            // Thêm hàng cho trợ lý
            statsModel.addRow(new Object[]{"Tổng số trợ lý", troLyList.size()});
            Map<String, Long> chucVuCount = troLyList.stream().collect(Collectors.groupingBy(TroLy::getChucVu, Collectors.counting()));
            for (Map.Entry<String, Long> e : chucVuCount.entrySet()) {
                if (e.getValue() > 0) statsModel.addRow(new Object[]{"Chức vụ " + e.getKey(), e.getValue()});
            }
            double avgSalary = troLyList.stream().mapToDouble(TroLy::getLuong).average().orElse(0);
            statsModel.addRow(new Object[]{"Lương trung bình", String.format("%,.0f VND", avgSalary)});
        }

        if (d[0] != null) {
            d[0].setLayout(new BorderLayout());
            d[0].setSize(500, 400);
            d[0].setLocationRelativeTo(mainFrame);

            // Tạo bảng
            JTable statsTable = new JTable(statsModel);
            statsTable.setRowHeight(25);
            statsTable.setShowGrid(true);
            statsTable.setShowVerticalLines(true);
            statsTable.setShowHorizontalLines(true);
            statsTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            statsTable.getTableHeader().setReorderingAllowed(false);
            statsTable.getTableHeader().setResizingAllowed(false);
            statsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    if (column == 1) {
                        ((JLabel) c).setHorizontalAlignment(JLabel.CENTER);
                    }
                    return c;
                }
            });

            JScrollPane scrollPane = new JScrollPane(statsTable);
            d[0].add(scrollPane, BorderLayout.CENTER);

            // Nút đóng
            JPanel buttonPanel = new JPanel();
            JButton closeBtn = new JButton("Đóng");
            closeBtn.addActionListener(e -> d[0].dispose());
            buttonPanel.add(closeBtn);
            d[0].add(buttonPanel, BorderLayout.SOUTH);

            d[0].setVisible(true);
        }
    }

    private void showAssistantDialog(TroLy editTl) {
        JDialog d = new JDialog(mainFrame, editTl == null ? "Thêm trợ lý" : "Sửa trợ lý", true);
        d.setLayout(new BorderLayout());
        d.setSize(450, 400);
        d.setLocationRelativeTo(mainFrame);
        d.getContentPane().setBackground(new Color(245, 245, 250));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel(editTl == null ? "Thêm trợ lý mới" : "Sửa thông tin trợ lý");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        d.add(titlePanel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 220), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        JTextField hoTenF = new JTextField(editTl != null ? editTl.getHoTen() : "", 15);
        JTextField ngaySinhF = new JTextField(editTl != null && editTl.getNgaySinh() != null ? editTl.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "", 15);
        JTextField chucVuF = new JTextField(editTl != null ? editTl.getChucVu() : "", 15);
        JTextField luongF = new JTextField(editTl != null ? String.format("%,.0f", editTl.getLuong()) : "", 15);

        // Styling components
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 14);
        Color fieldBg = new Color(249, 249, 249);

        String[] labels = {"Họ tên:", "Ngày sinh:", "Chức vụ:", "Lương:"};
        JComponent[] fields = {hoTenF, ngaySinhF, chucVuF, luongF};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            label.setForeground(new Color(52, 73, 94));
            gbc.gridx = 0; gbc.gridy = i;
            formPanel.add(label, gbc);

            fields[i].setFont(fieldFont);
            if (fields[i] instanceof JTextField) {
                ((JTextField) fields[i]).setBackground(fieldBg);
                ((JTextField) fields[i]).setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            }
            gbc.gridx = 1; gbc.gridy = i;
            formPanel.add(fields[i], gbc);
        }

        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        d.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton saveB = new JButton("Lưu");
        JButton cancelB = new JButton("Hủy");
        buttonPanel.add(saveB);
        buttonPanel.add(cancelB);
        d.add(buttonPanel, BorderLayout.SOUTH);

        saveB.addActionListener(e -> {
            try {
                String hoTen = hoTenF.getText().trim();
                LocalDate ngaySinh = null;
                String ngaySinhStr = ngaySinhF.getText().trim();
                if (!ngaySinhStr.isEmpty()) {
                    try {
                        ngaySinh = LocalDate.parse(ngaySinhStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception dateEx) {
                        JOptionPane.showMessageDialog(d, "Ngày sinh không hợp lệ. Vui lòng nhập theo định dạng dd/MM/yyyy (ví dụ: 10/10/1989).");
                        return;
                    }
                }
                String chucVu = chucVuF.getText().trim();
                double luong = Double.parseDouble(luongF.getText().trim().replace(",", ""));

                TroLy tl = new TroLy(hoTen, ngaySinh, chucVu, luong);
                if (editTl != null) {
                    troLyList.remove(editTl);
                }
                troLyList.add(tl);
                refreshTable();
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });

        cancelB.addActionListener(e -> d.dispose());

        d.setVisible(true);
    }

    private void showAssistantDetailsDialog(TroLy tl) {
        JDialog d = new JDialog(mainFrame, "Chi tiết trợ lý", true);
        d.setLayout(new BorderLayout());
        d.setSize(500, 400);
        d.setLocationRelativeTo(mainFrame);
        d.getContentPane().setBackground(new Color(245, 245, 250));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(52, 152, 219));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("Thông tin chi tiết trợ lý");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titlePanel.add(titleLabel);
        d.add(titlePanel, BorderLayout.NORTH);

        // Details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 180, 220), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 16);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 16);
        Color labelColor = new Color(52, 73, 94);
        Color valueColor = new Color(44, 62, 80);

        String[] labels = {"Họ tên:", "Ngày sinh:", "Tuổi:", "Chức vụ:", "Lương/tháng:"};
        String ngaySinhStr = tl.getNgaySinh() != null ? tl.getNgaySinh().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "Chưa cập nhật";
        String tuoiStr = tl.getNgaySinh() != null ? String.valueOf(LocalDate.now().getYear() - tl.getNgaySinh().getYear()) : "Chưa cập nhật";
        String luongStr = String.format("%,.0f VND", tl.getLuong());
        String[] values = {tl.getHoTen(), ngaySinhStr, tuoiStr, tl.getChucVu(), luongStr};

        for (int i = 0; i < labels.length; i++) {
            JLabel label = new JLabel(labels[i]);
            label.setFont(labelFont);
            label.setForeground(labelColor);
            gbc.gridx = 0; gbc.gridy = i;
            detailsPanel.add(label, gbc);

            JLabel valueLabel = new JLabel(values[i]);
            valueLabel.setFont(valueFont);
            valueLabel.setForeground(valueColor);
            gbc.gridx = 1; gbc.gridy = i;
            detailsPanel.add(valueLabel, gbc);
        }

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        d.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton closeBtn = new JButton("Đóng");
        closeBtn.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeBtn.addActionListener(e -> d.dispose());
        buttonPanel.add(closeBtn);
        d.add(buttonPanel, BorderLayout.SOUTH);

        d.setVisible(true);
    }

}
