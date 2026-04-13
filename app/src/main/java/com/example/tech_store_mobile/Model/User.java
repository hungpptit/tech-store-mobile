package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

public class User {
    private String userId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String dateOfBirth;
    private String gender;
    private String avatarUrl;
    private String defaultAddressId;
    private String defaultPaymentId;
    private Timestamp createdAt;

    public User() {
    }

    public User(String userId, String fullName, String email, String phoneNumber, String dateOfBirth,
                String gender, String avatarUrl, String defaultAddressId, String defaultPaymentId,
                Timestamp createdAt) {
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.avatarUrl = avatarUrl;
        this.defaultAddressId = defaultAddressId;
        this.defaultPaymentId = defaultPaymentId;
        this.createdAt = createdAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getDefaultAddressId() {
        return defaultAddressId;
    }

    public void setDefaultAddressId(String defaultAddressId) {
        this.defaultAddressId = defaultAddressId;
    }

    public String getDefaultPaymentId() {
        return defaultPaymentId;
    }

    public void setDefaultPaymentId(String defaultPaymentId) {
        this.defaultPaymentId = defaultPaymentId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

