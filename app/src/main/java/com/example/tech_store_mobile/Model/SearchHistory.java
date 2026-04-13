package com.example.tech_store_mobile.Model;

import java.util.List;

public class SearchHistory {
    private String userId;
    private List<String> recentKeywords;

    public SearchHistory() {
    }

    public SearchHistory(String userId, List<String> recentKeywords) {
        this.userId = userId;
        this.recentKeywords = recentKeywords;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getRecentKeywords() {
        return recentKeywords;
    }

    public void setRecentKeywords(List<String> recentKeywords) {
        this.recentKeywords = recentKeywords;
    }
}

