package com.example.tech_store_mobile;

import com.example.tech_store_mobile.Model.OrderItem;
import com.example.tech_store_mobile.Model.StockReservation;
import com.google.firebase.Timestamp;
import org.junit.Test;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.*;

public class StockReservationTest {

    @Test
    public void testStockReservationCreationAndExpirationTTL() {
        Date now = new Date();
        Date fiveMinutesLater = new Date(now.getTime() + (5 * 60 * 1000));

        Timestamp createdAt = new Timestamp(now);
        Timestamp expiresAt = new Timestamp(fiveMinutesLater);

        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("prod_01", "MacBook Pro", 1L, 1200.0, "img_url", "Space Gray"));

        StockReservation reservation = new StockReservation(
                "res_999",
                "usr_100",
                items,
                "pending",
                createdAt,
                expiresAt
        );

        assertEquals("res_999", reservation.getReservationId());
        assertEquals("usr_100", reservation.getUserId());
        assertEquals("pending", reservation.getStatus());
        assertEquals(1, reservation.getItems().size());
        assertEquals(expiresAt, reservation.getExpiresAt());

        // Test status transition
        reservation.setStatus("completed");
        assertEquals("completed", reservation.getStatus());

        reservation.setStatus("released");
        assertEquals("released", reservation.getStatus());
    }
}
