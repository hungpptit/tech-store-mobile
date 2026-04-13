package com.example.tech_store_mobile.Model;

import com.google.firebase.Timestamp;

public class TrackingHistoryItem {
    private String statusName;
    private String location;
    private Timestamp timestamp;

    public TrackingHistoryItem() {
    }

    public TrackingHistoryItem(String statusName, String location, Timestamp timestamp) {
        this.statusName = statusName;
        this.location = location;
        this.timestamp = timestamp;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}

