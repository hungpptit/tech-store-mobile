package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.UserNotification;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.NotificationAdapter;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment hiển thị danh sách thông báo của user hiện tại
 */
public class NotificationsFragment extends Fragment {
    private static final String TAG = "NotificationsFragment";

    private RecyclerView rvNotifications;
    private View emptyStateView;
    private NotificationAdapter adapter;
    private List<UserNotification> notificationList;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private int loadCallbacksExpected = 0;
    private int loadCallbacksCompleted = 0;

    private static final String PREFS_NAME = "read_announcements_prefs";
    private static final String KEY_READ_ANNOUNCEMENTS = "read_announcements_list";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupListeners(view);
        loadNotifications();

        return view;
    }

    private void initializeViews(View view) {
        rvNotifications = view.findViewById(R.id.rv_notifications);
        emptyStateView = view.findViewById(R.id.tv_empty_state);
    }

    private void setupRecyclerView() {
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(notificationList);
        adapter.setOnNotificationClickListener((notification, position) -> {
            // Mark as read when clicked
            markAsRead(notification);
        });
        rvNotifications.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvNotifications.setAdapter(adapter);
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_notifications);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void loadNotifications() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            notificationList.clear();
            showEmptyState();
            return;
        }

        Log.d(TAG, "Loading notifications for user: " + userId);

        notificationList.clear();
        loadCallbacksExpected = 2; // Expect 2 callbacks (personal + announcements)
        loadCallbacksCompleted = 0;

        // Load personal notifications
        loadPersonalNotifications(userId);
        // Load global announcements (shown as notifications too)
        loadGlobalAnnouncements();
    }

    private void loadPersonalNotifications(String userId) {
        // Query without orderBy to avoid composite index requirement
        // We'll sort in code instead
        db.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    // Map documents to UserNotification objects with document ID as notificationId
                    queryDocumentSnapshots.getDocuments().forEach(doc -> {
                        UserNotification notif = doc.toObject(UserNotification.class);
                        // Use document ID if notificationId field is null
                        if (notif != null && (notif.getNotificationId() == null || notif.getNotificationId().isEmpty())) {
                            notif.setNotificationId(doc.getId());
                        }
                        if (notif != null) {
                            notificationList.add(notif);
                        }
                    });

                    Log.d(TAG, "Loaded " + queryDocumentSnapshots.size() + " personal notifications");
                    onLoadCallbackCompleted();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load notifications", e);
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
                    }
                    onLoadCallbackCompleted();
                });
    }

    private void loadGlobalAnnouncements() {
        // Query without orderBy to avoid composite index requirement
        db.collection("global_announcements")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    // Convert global announcements to UserNotification format for display
                    queryDocumentSnapshots.getDocuments().forEach(doc -> {
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        Object createdAtObj = doc.get("createdAt");
                        String announcementId = doc.getId();

                        // Create UserNotification from global announcement
                        UserNotification notif = new UserNotification();
                        notif.setNotificationId("announcement_" + announcementId);
                        notif.setTitle((title != null ? title : "Thông báo"));
                        notif.setContent((content != null ? content : ""));
                        notif.setType("System");
                        
                        // Kiểm tra trạng thái đã đọc trong SharedPreferences
                        notif.setIsRead(isAnnouncementRead(notif.getNotificationId())); 
                        
                        if (createdAtObj instanceof com.google.firebase.Timestamp) {
                            notif.setCreatedAt((com.google.firebase.Timestamp) createdAtObj);
                        }

                        notificationList.add(notif);
                    });

                    Log.d(TAG, "Loaded " + queryDocumentSnapshots.size() + " global announcements");
                    onLoadCallbackCompleted();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load announcements (not critical)", e);
                    onLoadCallbackCompleted();
                });
    }

    private void onLoadCallbackCompleted() {
        loadCallbacksCompleted++;
        if (loadCallbacksCompleted >= loadCallbacksExpected) {
            updateUI();
        }
    }

    private void updateUI() {
        // Sort by createdAt descending
        notificationList.sort((n1, n2) -> {
            if (n1.getCreatedAt() == null || n2.getCreatedAt() == null) return 0;
            return n2.getCreatedAt().compareTo(n1.getCreatedAt());
        });

        if (notificationList.isEmpty()) {
            showEmptyState();
        } else {
            showNotifications();
        }
        adapter.notifyDataSetChanged();
    }

    private void markAsRead(UserNotification notification) {
        if (notification.getNotificationId() == null) return;

        // Nếu là thông báo toàn cục, lưu vào SharedPreferences
        if (notification.getNotificationId().startsWith("announcement_")) {
            saveReadAnnouncement(notification.getNotificationId());
            notification.setIsRead(true);
            adapter.notifyDataSetChanged();
            return;
        }

        // Update isRead in Firestore cho thông báo cá nhân
        db.collection("notifications")
                .document(notification.getNotificationId())
                .update("isRead", true)
                .addOnSuccessListener(unused -> {
                    Log.d(TAG, "Marked notification as read: " + notification.getNotificationId());
                    notification.setIsRead(true);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to mark as read", e);
                    Toast.makeText(requireContext(), "Lỗi khi cập nhật", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isAnnouncementRead(String id) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(id, false);
    }

    private void saveReadAnnouncement(String id) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean(id, true).apply();
    }


    private void showEmptyState() {
        rvNotifications.setVisibility(View.GONE);
        emptyStateView.setVisibility(View.VISIBLE);
    }

    private void showNotifications() {
        rvNotifications.setVisibility(View.VISIBLE);
        emptyStateView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bỏ loadNotifications() ở đây để tránh duplicate khi Fragment hiển thị lại
    }
}


