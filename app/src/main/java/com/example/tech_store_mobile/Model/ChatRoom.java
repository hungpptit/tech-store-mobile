package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

@SuppressWarnings("unused")
public class ChatRoom {
    private String userId;
    private String userName;
    private String lastMessage;
    private Timestamp updatedAt;
    private Long userUnreadCount;
    private Long adminUnreadCount;

    public ChatRoom() {
    }

    public ChatRoom(String userId, String userName, String lastMessage, Timestamp updatedAt,
                    Long userUnreadCount, Long adminUnreadCount) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
        this.userUnreadCount = userUnreadCount;
        this.adminUnreadCount = adminUnreadCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getUserUnreadCount() {
        return userUnreadCount;
    }

    public void setUserUnreadCount(Long userUnreadCount) {
        this.userUnreadCount = userUnreadCount;
    }

    public Long getAdminUnreadCount() {
        return adminUnreadCount;
    }

    public void setAdminUnreadCount(Long adminUnreadCount) {
        this.adminUnreadCount = adminUnreadCount;
    }
}


