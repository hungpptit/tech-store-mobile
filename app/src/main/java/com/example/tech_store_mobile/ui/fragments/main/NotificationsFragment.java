package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
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

    // Các trường phục vụ phân trang (Pagination)
    private final List<UserNotification> personalList = new ArrayList<>();
    private final List<UserNotification> globalList = new ArrayList<>();
    private DocumentSnapshot lastVisiblePersonal = null;
    private boolean isLastPagePersonal = false;
    private boolean isLoading = false;
    private final int PAGE_SIZE = 20;
    private String currentUserId = null;

    private static final String PREFS_NAME = "read_announcements_prefs";

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
            // Đánh dấu đã đọc khi click
            markAsRead(notification);
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        rvNotifications.setLayoutManager(layoutManager);
        rvNotifications.setAdapter(adapter);

        // Lắng nghe sự kiện cuộn của RecyclerView để thực hiện phân trang (Infinite Scroll)
        rvNotifications.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Nếu đang tải hoặc đã hết trang cá nhân thì không load tiếp
                if (isLoading || isLastPagePersonal) return;

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                // Kiểm tra xem đã cuộn tới cuối danh sách chưa
                if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0) {
                    loadMoreNotifications();
                }
            }
        });
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_notifications);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());
        }
    }

    private void loadNotifications() {
        currentUserId = AuthManager.getCurrentUid();
        if (currentUserId == null) {
            notificationList.clear();
            personalList.clear();
            globalList.clear();
            showEmptyState();
            return;
        }

        Log.d(TAG, "Loading notifications for user: " + currentUserId);

        personalList.clear();
        globalList.clear();
        notificationList.clear();
        lastVisiblePersonal = null;
        isLastPagePersonal = false;
        isLoading = true;

        // Bắt đầu bằng việc tải thông báo chung (global), sau đó tải trang đầu tiên của thông báo cá nhân
        loadGlobalAnnouncements(() -> {
            loadPersonalNotificationsPage();
        });
    }

    private void loadPersonalNotificationsPage() {
        if (currentUserId == null) {
            isLoading = false;
            return;
        }

        isLoading = true;
        Log.d(TAG, "Fetching page of personal notifications. LastVisible is null? " + (lastVisiblePersonal == null));

        Query query = db.collection("notifications")
                .whereEqualTo("userId", currentUserId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(PAGE_SIZE);

        if (lastVisiblePersonal != null) {
            query = query.startAfter(lastVisiblePersonal);
        }

        query.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    List<DocumentSnapshot> docs = queryDocumentSnapshots.getDocuments();
                    if (!docs.isEmpty()) {
                        lastVisiblePersonal = docs.get(docs.size() - 1);

                        docs.forEach(doc -> {
                            UserNotification notif = doc.toObject(UserNotification.class);
                            if (notif != null && (notif.getNotificationId() == null || notif.getNotificationId().isEmpty())) {
                                notif.setNotificationId(doc.getId());
                            }
                            if (notif != null) {
                                personalList.add(notif);
                            }
                        });
                    }

                    if (docs.size() < PAGE_SIZE) {
                        isLastPagePersonal = true;
                        Log.d(TAG, "Reached last page of personal notifications");
                    }

                    Log.d(TAG, "Loaded " + docs.size() + " personal notifications. Total in memory: " + personalList.size());
                    mergeAndDisplayNotifications();
                    isLoading = false;
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load personal notifications page", e);
                    isLoading = false;
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Lỗi khi tải thông báo", Toast.LENGTH_SHORT).show();
                        if (e.getMessage() != null && e.getMessage().contains("FAILED_PRECONDITION")) {
                            Log.e(TAG, "Lỗi thiếu chỉ mục (Composite Index). Vui lòng click vào link trong Logcat phía dưới để tạo chỉ mục trên Firebase.");
                        }
                    }
                });
    }

    private void loadGlobalAnnouncements(Runnable onComplete) {
        db.collection("global_announcements")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50) // Giới hạn tối đa 50 thông báo chung gần nhất
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;

                    queryDocumentSnapshots.getDocuments().forEach(doc -> {
                        String title = doc.getString("title");
                        String content = doc.getString("content");
                        Object createdAtObj = doc.get("createdAt");
                        String announcementId = doc.getId();

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

                        globalList.add(notif);
                    });

                    Log.d(TAG, "Loaded " + globalList.size() + " global announcements");
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load announcements (not critical)", e);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                });
    }

    private void loadMoreNotifications() {
        if (isLoading || isLastPagePersonal) return;
        loadPersonalNotificationsPage();
    }

    private void mergeAndDisplayNotifications() {
        notificationList.clear();
        notificationList.addAll(personalList);
        notificationList.addAll(globalList);

        // Sắp xếp gộp chung theo createdAt giảm dần (mới nhất lên đầu)
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

        // Cập nhật isRead trong Firestore cho thông báo cá nhân
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
        if (getContext() == null) return false;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        return prefs.getBoolean(id, false);
    }

    private void saveReadAnnouncement(String id) {
        if (getContext() == null) return;
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
    }
}
