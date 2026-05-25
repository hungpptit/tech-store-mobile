package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;
import java.util.List;

public class StockReservation {
    private String reservationId;
    private String userId;
    private List<OrderItem> items;
    private String status; // "pending" | "completed" | "released"
    private Timestamp createdAt;
    private Timestamp expiresAt;
    private Timestamp releasedAt;

    public StockReservation() {
    }

    public StockReservation(String reservationId, String userId, List<OrderItem> items, String status, Timestamp createdAt, Timestamp expiresAt) {
        this.reservationId = reservationId;
        this.userId = userId;
        this.items = items;
        this.status = status;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Timestamp getReleasedAt() {
        return releasedAt;
    }

    public void setReleasedAt(Timestamp releasedAt) {
        this.releasedAt = releasedAt;
    }
}
