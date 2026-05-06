package com.example.tech_store_mobile.utils;

import androidx.annotation.Nullable;
import android.util.Log;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Singleton manager that listens to Firestore "notifications" for the current user
 * and notifies listeners about unread count changes.
 */
public class NotificationBadgeManager {
    private static NotificationBadgeManager instance;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration registration;
    private final Set<BadgeListener> listeners = new CopyOnWriteArraySet<>();
    private int currentCount = 0;

    public interface BadgeListener {
        void onBadgeCountChanged(int unreadCount);
    }

    private NotificationBadgeManager() {
    }

    public static synchronized NotificationBadgeManager getInstance() {
        if (instance == null) {
            instance = new NotificationBadgeManager();
        }
        return instance;
    }

    public void addListener(BadgeListener l) {
        if (l == null) return;
        listeners.add(l);
        // notify immediately with current value
        l.onBadgeCountChanged(currentCount);
    }

    public void removeListener(BadgeListener l) {
        if (l == null) return;
        listeners.remove(l);
    }

    public int getCurrentCount() {
        return currentCount;
    }

    public void start() {
        String uid = AuthManager.getCurrentUid();
        Log.d("NotificationBadgeMgr", "start() called, currentUid=" + uid);
        if (uid == null) {
            updateCount(0);
            return;
        }

        if (registration != null) return; // already listening

        registration = db.collection("notifications")
                .whereEqualTo("userId", uid)
                .whereEqualTo("isRead", false)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Log.w("NotificationBadgeMgr", "snapshot listener error", error);
                        }
                        if (error != null) {
                            // keep previous count on error
                            return;
                        }
                        int count = (value == null) ? 0 : value.size();
                        Log.d("NotificationBadgeMgr", "snapshot changed, unreadCount=" + count);
                        updateCount(count);
                    }
                });
    }

    public void stop() {
        if (registration != null) {
            registration.remove();
            registration = null;
        }
    }

    private void updateCount(int count) {
        if (this.currentCount == count) return;
        Log.d("NotificationBadgeMgr", "updateCount from " + this.currentCount + " to " + count);
        this.currentCount = count;
        for (BadgeListener l : listeners) {
            try {
                l.onBadgeCountChanged(count);
            } catch (Exception ignored) {
            }
        }
    }
}


