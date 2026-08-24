package com.example.tech_store_mobile;

import com.example.tech_store_mobile.utils.OrderStatusUtil;
import org.junit.Test;
import static org.junit.Assert.*;

public class OrderStatusUtilTest {

    @Test
    public void testGetViStatus_validStatuses() {
        assertEquals("Đang đóng gói", OrderStatusUtil.getViStatus("packing"));
        assertEquals("Đang đóng gói", OrderStatusUtil.getViStatus("Packing"));
        assertEquals("Đã giao cho ĐVVC", OrderStatusUtil.getViStatus("picked"));
        assertEquals("Đang vận chuyển", OrderStatusUtil.getViStatus("in transit"));
        assertEquals("Đã giao hàng", OrderStatusUtil.getViStatus("delivered"));
        assertEquals("Đã hoàn thành", OrderStatusUtil.getViStatus("completed"));
    }

    @Test
    public void testGetViStatus_unknownAndNull() {
        assertEquals("", OrderStatusUtil.getViStatus(null));
        assertEquals("UnknownStatus", OrderStatusUtil.getViStatus("UnknownStatus"));
    }

    @Test
    public void testTranslateNotificationContent() {
        String content = "Đơn hàng của bạn đã chuyển sang trạng thái: In Transit";
        String translated = OrderStatusUtil.translateNotificationContent(content);
        assertEquals("Đơn hàng của bạn đã chuyển sang trạng thái: Đang vận chuyển", translated);

        String completedContent = "Đơn hàng của bạn đã chuyển sang trạng thái: Completed";
        assertEquals("Đơn hàng của bạn đã chuyển sang trạng thái: Đã hoàn thành", 
                OrderStatusUtil.translateNotificationContent(completedContent));

        assertEquals("", OrderStatusUtil.translateNotificationContent(null));
    }
}
