package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

import java.util.List;

public class Cart {
    private String userId;
    private List<CartItem> items;
    private Timestamp updatedAt;

    public Cart() {
    }

    public Cart(String userId, List<CartItem> items, Timestamp updatedAt) {
        this.userId = userId;
        this.items = items;
        this.updatedAt = updatedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}

