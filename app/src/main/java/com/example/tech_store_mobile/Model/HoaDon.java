package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

import java.util.List;

public class HoaDon {
    private String hoaDonId;
    private String userId;
    private String orderId;
    private String invoiceNumber;
    private Timestamp issueDate;
    private Timestamp dueDate;
    private String paymentStatus;
    private String paymentMethod;
    private String paymentGateway;
    private String transactionId;
    private List<OrderItem> items;
    private OrderSummary summary;
    private ShippingAddressSnapshot shippingAddress;
    private String note;
    private Timestamp createdAt;

    public HoaDon() {
    }

    public HoaDon(String hoaDonId, String userId, String orderId, String invoiceNumber, Timestamp issueDate,
                  Timestamp dueDate, String paymentStatus, String paymentMethod, String paymentGateway,
                  String transactionId, List<OrderItem> items, OrderSummary summary,
                  ShippingAddressSnapshot shippingAddress, String note, Timestamp createdAt) {
        this.hoaDonId = hoaDonId;
        this.userId = userId;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentGateway = paymentGateway;
        this.transactionId = transactionId;
        this.items = items;
        this.summary = summary;
        this.shippingAddress = shippingAddress;
        this.note = note;
        this.createdAt = createdAt;
    }

    public String getHoaDonId() {
        return hoaDonId;
    }

    public void setHoaDonId(String hoaDonId) {
        this.hoaDonId = hoaDonId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Timestamp getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(Timestamp issueDate) {
        this.issueDate = issueDate;
    }

    public Timestamp getDueDate() {
        return dueDate;
    }

    public void setDueDate(Timestamp dueDate) {
        this.dueDate = dueDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
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

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
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

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

