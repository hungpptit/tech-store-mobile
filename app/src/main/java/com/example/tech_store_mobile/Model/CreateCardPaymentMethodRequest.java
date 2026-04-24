package com.example.tech_store_mobile.Model;

public class CreateCardPaymentMethodRequest {
    private String userId;
    private String cardToken;
    private String cardHolderName;

    public CreateCardPaymentMethodRequest() {
    }

    public CreateCardPaymentMethodRequest(String userId, String cardToken, String cardHolderName) {
        this.userId = userId;
        this.cardToken = sanitizeValue(cardToken);
        this.cardHolderName = cardHolderName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardToken() {
        return cardToken;
    }

    public void setCardToken(String cardToken) {
        this.cardToken = sanitizeValue(cardToken);
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    private String sanitizeValue(String value) {
        return value == null ? null : value.replaceAll("\\s+", "");
    }
}


