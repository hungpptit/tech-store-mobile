package com.example.tech_store_mobile.utils;

import java.util.HashMap;
import java.util.Map;

public class OrderStatusUtil {

    private static final Map<String, String> STATUS_MAP = new HashMap<>();

    static {
        STATUS_MAP.put("packing", "Đang đóng gói");
        STATUS_MAP.put("picked", "Đã giao cho ĐVVC");
        STATUS_MAP.put("in transit", "Đang vận chuyển");
        STATUS_MAP.put("delivered", "Đã giao hàng");
        STATUS_MAP.put("completed", "Đã hoàn thành");
    }

    /**
     * Chuyển đổi trạng thái đơn hàng từ tiếng Anh sang tiếng Việt.
     * Ví dụ: "Picked" -> "Đã giao cho ĐVVC"
     */
    public static String getViStatus(String englishStatus) {
        if (englishStatus == null) return "";
        String cleanStatus = englishStatus.trim().toLowerCase();
        String viStatus = STATUS_MAP.get(cleanStatus);
        return viStatus != null ? viStatus : englishStatus;
    }

    /**
     * Dịch các trạng thái tiếng Anh xuất hiện trong nội dung thông báo sang tiếng Việt.
     * Ví dụ: "Đơn hàng của bạn đã chuyển sang trạng thái: Picked" -> "... trạng thái: Đã bàn giao vận chuyển"
     */
    public static String translateNotificationContent(String content) {
        if (content == null) return "";
        return content
                .replace("trạng thái: Packing", "trạng thái: Đang đóng gói")
                .replace("trạng thái: Picked", "trạng thái: Đã bàn giao vận chuyển")
                .replace("trạng thái: In Transit", "trạng thái: Đang vận chuyển")
                .replace("trạng thái: Delivered", "trạng thái: Đã giao hàng thành công")
                .replace("trạng thái: Completed", "trạng thái: Đã hoàn thành")
                .replace("trạng thái: packing", "trạng thái: Đang đóng gói")
                .replace("trạng thái: picked", "trạng thái: Đã bàn giao vận chuyển")
                .replace("trạng thái: in transit", "trạng thái: Đang vận chuyển")
                .replace("trạng thái: delivered", "trạng thái: Đã giao hàng thành công")
                .replace("trạng thái: completed", "trạng thái: Đã hoàn thành");
    }
}
