package qlclb.model;

import java.util.Locale;

// Enum cho tình trạng cầu thủ
public enum TinhTrang {
    KHOE,
    CHAN_THUONG,
    NGHI_PHEP;

    // Chuyển chuỗi thành enum TinhTrang, hỗ trợ nhiều cách viết
    public static TinhTrang fromString(String s) {
        if (s == null) return KHOE;
        s = s.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "khoe":
            case "khỏe":
            case "khoẻ":
            case "k":
                return KHOE;
            case "chan_thuong":
            case "chấn thương":
            case "chan thuong":
            case "ct":
                return CHAN_THUONG;
            case "nghi phep":
            case "nghỉ phép":
            case "nghi_phep":
            case "np":
                return NGHI_PHEP;
            default:
                try {
                    return TinhTrang.valueOf(s.toUpperCase(Locale.ROOT));
                } catch (Exception ignored) { }
                return KHOE;
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case KHOE:
                return "Khỏe";
            case CHAN_THUONG:
                return "Chấn thương";
            case NGHI_PHEP:
                return "Nghỉ phép";
            default:
                return name();
        }
    }
}
