package com.example.tech_store_mobile.Model;

public class CreatePaymentIntentRequest {
    private String userId;
    private String orderId;
    private Double subtotal;
    private Double vatAmount;
    private Double shippingFee;
    private Double totalAmount;
    private String currency;
    private String paymentMethod;

    public CreatePaymentIntentRequest() {
    }

    public CreatePaymentIntentRequest(String userId, String orderId, Double subtotal, Double vatAmount,
                                      Double shippingFee, Double totalAmount, String currency, String paymentMethod) {
        this.userId = userId;
        this.orderId = orderId;
        this.subtotal = subtotal;
        this.vatAmount = vatAmount;
        this.shippingFee = shippingFee;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.paymentMethod = paymentMethod;
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

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(Double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}

