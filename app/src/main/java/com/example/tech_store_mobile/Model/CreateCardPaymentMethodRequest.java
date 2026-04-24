package com.example.tech_store_mobile.Model;

public class CreateCardPaymentMethodRequest {
    private String userId;
    private String cardNumber;
    private Integer expMonth;
    private Integer expYear;
    private String cvc;
    private String cardHolderName;

    public CreateCardPaymentMethodRequest() {
    }

    public CreateCardPaymentMethodRequest(String userId, String cardNumber, Integer expMonth, Integer expYear, String cvc, String cardHolderName) {
        this.userId = userId;
        this.cardNumber = sanitizeValue(cardNumber);
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.cvc = sanitizeValue(cvc);
        this.cardHolderName = cardHolderName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = sanitizeValue(cardNumber);
    }

    public Integer getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(Integer expMonth) {
        this.expMonth = expMonth;
    }

    public Integer getExpYear() {
        return expYear;
    }

    public void setExpYear(Integer expYear) {
        this.expYear = expYear;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = sanitizeValue(cvc);
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


