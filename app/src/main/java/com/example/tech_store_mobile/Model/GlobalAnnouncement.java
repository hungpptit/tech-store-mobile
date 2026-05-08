package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

public class GlobalAnnouncement {
    private String announcementId;
    private String adminId;
    private String title;
    private String content;
    private String imageUrl;
    private String type; // Promotion | System
    private String targetGroup; // All | Member | VIP
    private Timestamp scheduledAt;
    private Timestamp createdAt;

    public GlobalAnnouncement() {
    }

    public GlobalAnnouncement(String announcementId, String adminId, String title, String content,
                              String imageUrl, String type, String targetGroup,
                              Timestamp scheduledAt, Timestamp createdAt) {
        this.announcementId = announcementId;
        this.adminId = adminId;
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.type = type;
        this.targetGroup = targetGroup;
        this.scheduledAt = scheduledAt;
        this.createdAt = createdAt;
    }

    public String getAnnouncementId() {
        return announcementId;
    }

    public void setAnnouncementId(String announcementId) {
        this.announcementId = announcementId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTargetGroup() {
        return targetGroup;
    }

    public void setTargetGroup(String targetGroup) {
        this.targetGroup = targetGroup;
    }

    public Timestamp getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(Timestamp scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}

