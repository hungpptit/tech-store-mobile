package com.example.tech_store_mobile.Model;

public class OrderSummary {
    private Double subTotal;
    private Double shippingFee;
    private Double vatAmount;
    private Double totalAmount;

    public OrderSummary() {
    }

    public OrderSummary(Double subTotal, Double shippingFee, Double vatAmount, Double totalAmount) {
        this.subTotal = subTotal;
        this.shippingFee = shippingFee;
        this.vatAmount = vatAmount;
        this.totalAmount = totalAmount;
    }

    public Double getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(Double subTotal) {
        this.subTotal = subTotal;
    }

    public Double getShippingFee() {
        return shippingFee;
    }

    public void setShippingFee(Double shippingFee) {
        this.shippingFee = shippingFee;
    }

    public Double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(Double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
}

