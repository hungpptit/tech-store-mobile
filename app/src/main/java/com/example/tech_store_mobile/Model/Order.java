package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

import java.util.List;

public class Order {
    private String orderId;
    private String userId;
    private Timestamp orderDate;
    private String status;
    private List<OrderItem> items;
    private OrderSummary summary;
    private ShippingAddressSnapshot shippingAddress;
    private String paymentMethod;
    private List<TrackingHistoryItem> trackingHistory;

    public Order() {
    }

    public Order(String orderId, String userId, Timestamp orderDate, String status, List<OrderItem> items,
                 OrderSummary summary, ShippingAddressSnapshot shippingAddress, String paymentMethod,
                 List<TrackingHistoryItem> trackingHistory) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderDate = orderDate;
        this.status = status;
        this.items = items;
        this.summary = summary;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.trackingHistory = trackingHistory;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public OrderSummary getSummary() {
        return summary;
    }

    public void setSummary(OrderSummary summary) {
        this.summary = summary;
    }

    public ShippingAddressSnapshot getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddressSnapshot shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<TrackingHistoryItem> getTrackingHistory() {
        return trackingHistory;
    }

    public void setTrackingHistory(List<TrackingHistoryItem> trackingHistory) {
        this.trackingHistory = trackingHistory;
    }
}

