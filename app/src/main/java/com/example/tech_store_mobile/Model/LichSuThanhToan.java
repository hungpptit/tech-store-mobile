package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

public class LichSuThanhToan {
    private String paymentHistoryId;
    private String userId;
    private String hoaDonId;
    private String orderId;
    private String paymentMethod;
    private String paymentGateway;
    private String paymentStatus;
    private String transactionId;
    private String stripePaymentIntentId;
    private Double amount;
    private String currency;
    private Timestamp paidAt;
    private Timestamp createdAt;
    private String failureReason;
    private String note;

    public LichSuThanhToan() {
    }

    public LichSuThanhToan(String paymentHistoryId, String userId, String hoaDonId, String orderId,
                           String paymentMethod, String paymentGateway, String paymentStatus,
                           String transactionId, String stripePaymentIntentId, Double amount, String currency,
                           Timestamp paidAt, Timestamp createdAt, String failureReason, String note) {
        this.paymentHistoryId = paymentHistoryId;
        this.userId = userId;
        this.hoaDonId = hoaDonId;
        this.orderId = orderId;
        this.paymentMethod = paymentMethod;
        this.paymentGateway = paymentGateway;
        this.paymentStatus = paymentStatus;
        this.transactionId = transactionId;
        this.stripePaymentIntentId = stripePaymentIntentId;
        this.amount = amount;
        this.currency = currency;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
        this.failureReason = failureReason;
        this.note = note;
    }

    public String getPaymentHistoryId() {
        return paymentHistoryId;
    }

    public void setPaymentHistoryId(String paymentHistoryId) {
        this.paymentHistoryId = paymentHistoryId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(String hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

