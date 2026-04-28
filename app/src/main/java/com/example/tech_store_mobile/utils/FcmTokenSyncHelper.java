package com.example.tech_store_mobile.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Collections;

public final class FcmTokenSyncHelper {
    private static final String TAG = "FcmTokenSyncHelper";

    private FcmTokenSyncHelper() {
    }

    public static void syncCurrentToken() {
        String uid = AuthManager.getCurrentUid();
        if (uid == null) {
            return;
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> saveToken(uid, token))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to get FCM token", e));
    }

    public static void syncCurrentTokenIfLoggedIn() {
        if (AuthManager.isLoggedIn()) {
            syncCurrentToken();
        }
    }

    public static void saveTokenForCurrentUser(@NonNull String token) {
        String uid = AuthManager.getCurrentUid();
        if (uid == null || token.trim().isEmpty()) {
            return;
        }
        saveToken(uid, token.trim());
    }

    public static void deactivateCurrentToken(@NonNull Runnable onComplete) {
        String uid = AuthManager.getCurrentUid();
        if (uid == null) {
            onComplete.run();
            return;
        }

        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(task -> clearTokenField(uid, onComplete))
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to delete local FCM token", e);
                    clearTokenField(uid, onComplete);
                });
    }

    private static void saveToken(String uid, String token) {
        if (token == null || token.trim().isEmpty()) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(Collections.singletonMap("fcmToken", token.trim()), SetOptions.merge())
                .addOnSuccessListener(unused -> Log.d(TAG, "FCM token synced for uid=" + uid))
                .addOnFailureListener(e -> Log.w(TAG, "Failed to store FCM token", e));
    }

    private static void clearTokenField(String uid, Runnable onComplete) {
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .update("fcmToken", FieldValue.delete())
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "FCM token cleared for uid=" + uid);
                    onComplete.run();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to clear FCM token field, falling back to null", e);
                    FirebaseFirestore.getInstance()
                            .collection("users")
                            .document(uid)
                            .set(Collections.singletonMap("fcmToken", null), SetOptions.merge())
                            .addOnCompleteListener(task -> onComplete.run());
                });
    }
}

