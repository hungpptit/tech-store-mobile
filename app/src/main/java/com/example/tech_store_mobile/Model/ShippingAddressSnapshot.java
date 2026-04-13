package com.example.tech_store_mobile.Model;

public class ShippingAddressSnapshot {
    private String receiverName;
    private String phoneNumber;
    private String fullAddress;
    private String note;

    public ShippingAddressSnapshot() {
    }

    public ShippingAddressSnapshot(String receiverName, String phoneNumber, String fullAddress, String note) {
        this.receiverName = receiverName;
        this.phoneNumber = phoneNumber;
        this.fullAddress = fullAddress;
        this.note = note;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

