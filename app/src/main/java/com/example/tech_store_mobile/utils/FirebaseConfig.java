package com.example.tech_store_mobile.utils;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

/**
 * FirebaseConfig - Cấu hình Firebase Firestore
 * Bật offline persistence để có thể load data nếu mất kết nối
 */
public class FirebaseConfig {
    private static final String TAG = "FirebaseConfig";

    public static void configureFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Bật offline persistence (deprecated nhưng vẫn hoạt động)
        try {
            db.clearPersistence();
        } catch (Exception e) {
            Log.d(TAG, "Clear persistence error (normal): " + e.getMessage());
        }

        // Cấu hình settings
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .build();

        db.setFirestoreSettings(settings);

        Log.d(TAG, "✅ Firestore configured");
    }
}


