package com.example.tech_store_mobile.Model;

import java.util.List;

public class CreatePaymentIntentResponse {
    private String id;
    private String object;
    private Long amount;
    private String currency;
    private String clientSecret;
    private String paymentIntentId;
    private String status;
    private String message;
    private List<String> paymentMethodTypes;

    public CreatePaymentIntentResponse() {
    }

    public CreatePaymentIntentResponse(String id, String object, Long amount, String currency, String clientSecret,
                                       String paymentIntentId, String status, String message, List<String> paymentMethodTypes) {
        this.id = id;
        this.object = object;
        this.amount = amount;
        this.currency = currency;
        this.clientSecret = clientSecret;
        this.paymentIntentId = paymentIntentId;
        this.status = status;
        this.message = message;
        this.paymentMethodTypes = paymentMethodTypes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getPaymentIntentId() {
        return paymentIntentId;
    }

    public void setPaymentIntentId(String paymentIntentId) {
        this.paymentIntentId = paymentIntentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getPaymentMethodTypes() {
        return paymentMethodTypes;
    }

    public void setPaymentMethodTypes(List<String> paymentMethodTypes) {
        this.paymentMethodTypes = paymentMethodTypes;
    }
}


