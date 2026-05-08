package com.example.tech_store_mobile.utils;

import com.example.tech_store_mobile.Model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class UserProfileSyncHelper {

    public interface SyncCallback {
        void onSuccess();
        void onFailure(Exception exception);
    }

    private UserProfileSyncHelper() {
    }

    public static void syncEmailPasswordUserProfile(String uid, String fullName, String email, SyncCallback callback) {
        syncCompleteUserProfile(
                uid,
                new User(uid, "user", safeValue(fullName), safeValue(email), "", "", "", "", "", "", "", Timestamp.now()),
                callback
        );
    }

    public static void syncGoogleUserProfile(String uid, String displayName, String email, String avatarUrl, SyncCallback callback) {
        syncCompleteUserProfile(
                uid,
                new User(uid, "user", normalizeDisplayName(displayName, email), safeValue(email), "", "", "", safeValue(avatarUrl), "", "", "", Timestamp.now()),
                callback
        );
    }

    private static void syncCompleteUserProfile(String uid, User authUser, SyncCallback callback) {
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

        userRef.get()
                .addOnSuccessListener(snapshot -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("userId", safeValue(authUser.getUserId()));
                    userData.put("role", pickString(snapshot.getString("role"), authUser.getRole()));
                    userData.put("fullName", pickString(snapshot.getString("fullName"), authUser.getFullName()));
                    userData.put("email", pickString(snapshot.getString("email"), authUser.getEmail()));
                    userData.put("phoneNumber", pickString(snapshot.getString("phoneNumber"), authUser.getPhoneNumber()));
                    userData.put("dateOfBirth", pickString(snapshot.getString("dateOfBirth"), authUser.getDateOfBirth()));
                    userData.put("gender", pickString(snapshot.getString("gender"), authUser.getGender()));
                    userData.put("avatarUrl", pickString(snapshot.getString("avatarUrl"), authUser.getAvatarUrl()));
                    userData.put("defaultAddressId", pickString(snapshot.getString("defaultAddressId"), authUser.getDefaultAddressId()));
                    userData.put("defaultPaymentId", pickString(snapshot.getString("defaultPaymentId"), authUser.getDefaultPaymentId()));

                    Timestamp createdAt = snapshot.getTimestamp("createdAt");
                    userData.put("createdAt", createdAt != null ? createdAt : authUser.getCreatedAt() != null ? authUser.getCreatedAt() : Timestamp.now());

                    userRef.set(userData, SetOptions.merge())
                            .addOnSuccessListener(unused -> {
                                if (callback != null) callback.onSuccess();
                            })
                            .addOnFailureListener(exception -> {
                                if (callback != null) callback.onFailure(exception);
                            });
                })
                .addOnFailureListener(exception -> {
                    if (callback != null) callback.onFailure(exception);
                });
    }

    private static String normalizeDisplayName(String displayName, String email) {
        if (hasText(displayName)) {
            return displayName.trim();
        }

        if (hasText(email) && email.contains("@")) {
            return email.substring(0, email.indexOf('@')).trim();
        }

        return "";
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String pickString(String existingValue, String fallbackValue) {
        if (hasText(existingValue)) {
            return existingValue.trim();
        }

        if (hasText(fallbackValue)) {
            return fallbackValue.trim();
        }

        return "";
    }

    private static String safeValue(String value) {
        return value == null ? "" : value.trim();
    }
}


