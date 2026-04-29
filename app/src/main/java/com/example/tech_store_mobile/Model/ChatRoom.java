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
    private Boolean isStarredByAdmin;
    private Boolean isSpamByAdmin;
    private Boolean isDeletedByAdmin;
    private Boolean isImportByAdmin;

    public ChatRoom() {
    }

    public ChatRoom(String userId, String userName, String lastMessage, Timestamp updatedAt,
                    Long userUnreadCount, Long adminUnreadCount, Boolean isStarredByAdmin,
                    Boolean isSpamByAdmin, Boolean isDeletedByAdmin, Boolean isImportByAdmin) {
        this.userId = userId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
        this.userUnreadCount = userUnreadCount;
        this.adminUnreadCount = adminUnreadCount;
        this.isStarredByAdmin = isStarredByAdmin;
        this.isSpamByAdmin = isSpamByAdmin;
        this.isDeletedByAdmin = isDeletedByAdmin;
        this.isImportByAdmin = isImportByAdmin;
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

    public Boolean getIsStarredByAdmin() {
        return isStarredByAdmin;
    }

    public void setIsStarredByAdmin(Boolean isStarredByAdmin) {
        this.isStarredByAdmin = isStarredByAdmin;
    }

    public Boolean getIsSpamByAdmin() {
        return isSpamByAdmin;
    }

    public void setIsSpamByAdmin(Boolean isSpamByAdmin) {
        this.isSpamByAdmin = isSpamByAdmin;
    }

    public Boolean getIsDeletedByAdmin() {
        return isDeletedByAdmin;
    }

    public void setIsDeletedByAdmin(Boolean isDeletedByAdmin) {
        this.isDeletedByAdmin = isDeletedByAdmin;
    }

    public Boolean getIsImportByAdmin() {
        return isImportByAdmin;
    }

    public void setIsImportByAdmin(Boolean isImportByAdmin) {
        this.isImportByAdmin = isImportByAdmin;
    }
}


