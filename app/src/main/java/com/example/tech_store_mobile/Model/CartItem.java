package com.example.tech_store_mobile.Model;

public class CartItem {
    private String productId;
    private Long quantity;
    private String selectedColor;
    private Double priceAtAdded;

    public CartItem() {
    }

    public CartItem(String productId, Long quantity, String selectedColor, Double priceAtAdded) {
        this.productId = productId;
        this.quantity = quantity;
        this.selectedColor = selectedColor;
        this.priceAtAdded = priceAtAdded;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public String getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(String selectedColor) {
        this.selectedColor = selectedColor;
    }

    public Double getPriceAtAdded() {
        return priceAtAdded;
    }

    public void setPriceAtAdded(Double priceAtAdded) {
        this.priceAtAdded = priceAtAdded;
    }
}

