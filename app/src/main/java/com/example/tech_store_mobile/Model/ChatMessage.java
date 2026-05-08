package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

@SuppressWarnings("unused")
public class ChatMessage {
    private String senderId;
    private String receiverId;
    private String content;
    private String type;
    private String fileUrl;
    private Timestamp createdAt;

    public ChatMessage() {
    }

    public ChatMessage(String senderId, String receiverId, String content, String type,
                       String fileUrl, Timestamp createdAt) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.type = type;
        this.fileUrl = fileUrl;
        this.createdAt = createdAt;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}


