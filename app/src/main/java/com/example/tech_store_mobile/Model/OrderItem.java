package com.example.tech_store_mobile.Model;

public class OrderItem {
    private String productId;
    private String productName;
    private Long quantity;
    private Double price;
    private String imageUrl;
    private String color;
    private Boolean isReviewed = false;

    public OrderItem() {
    }

    public OrderItem(String productId, String productName, Long quantity, Double price,
                     String imageUrl, String color) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.imageUrl = imageUrl;
        this.color = color;
        this.isReviewed = false;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Boolean getIsReviewed() {
        return isReviewed != null ? isReviewed : false;
    }

    public void setIsReviewed(Boolean isReviewed) {
        this.isReviewed = isReviewed;
    }
}

