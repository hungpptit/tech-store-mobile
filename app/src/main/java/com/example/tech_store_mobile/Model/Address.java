package com.example.tech_store_mobile.Model;

public class Address {
    private String addressId;
    private String userId;
    private String nickname;
    private String fullAddress;
    private Boolean isDefault;

    public Address() {
    }

    public Address(String addressId, String userId, String nickname, String fullAddress, Boolean isDefault) {
        this.addressId = addressId;
        this.userId = userId;
        this.nickname = nickname;
        this.fullAddress = fullAddress;
        this.isDefault = isDefault;
    }

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(String addressId) {
        this.addressId = addressId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }
}

