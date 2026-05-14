package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Order;
import com.example.tech_store_mobile.Model.OrderItem;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.OrderAdapter;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrdersListFragment extends Fragment implements OrderAdapter.OnOrderActionListener {

    private static final String TAG = "OrdersListFragment";
    private static final String ARG_IS_ONGOING = "is_ongoing";
    
    private boolean isOngoing;
    private RecyclerView rvOrders;
    private ProgressBar progressBar;
    private LinearLayout emptyState;
    private TextView tvEmptyTitle, tvEmptyMessage;
    private ImageView imgEmpty;
    private OrderAdapter adapter;
    private final List<OrderAdapter.DisplayItem> displayList = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static OrdersListFragment newInstance(boolean isOngoing) {
        OrdersListFragment fragment = new OrdersListFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_ONGOING, isOngoing);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isOngoing = getArguments().getBoolean(ARG_IS_ONGOING);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders_list, container, false);
        rvOrders = view.findViewById(R.id.rv_orders_list);
        progressBar = view.findViewById(R.id.progress_bar_orders);
        emptyState = view.findViewById(R.id.layout_empty_orders);
        tvEmptyTitle = view.findViewById(R.id.tv_empty_orders_title);
        tvEmptyMessage = view.findViewById(R.id.tv_empty_orders_message);
        imgEmpty = view.findViewById(R.id.img_empty_orders);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        loadOrders();
    }

    private void setupRecyclerView() {
        rvOrders.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new OrderAdapter(displayList, isOngoing, this);
        rvOrders.setAdapter(adapter);
    }

    private void loadOrders() {
        String currentUserId = AuthManager.getCurrentUid();
        if (currentUserId == null) {
            showEmptyState();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    displayList.clear();
                    
                    List<Order> filteredOrders = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Order order = document.toObject(Order.class);
                        
                        // Bước 1: Kiểm tra userId trùng khớp
                        if (currentUserId.equals(order.getUserId())) {
                            filteredOrders.add(order);
                        }
                    }

                    // Sắp xếp đơn hàng theo thời gian mới nhất (giảm dần)
                    filteredOrders.sort((o1, o2) -> {
                        if (o1.getOrderDate() == null || o2.getOrderDate() == null) return 0;
                        return o2.getOrderDate().compareTo(o1.getOrderDate());
                    });

                    // Bước 2: Phân loại theo field 'status' của Order
                    for (Order order : filteredOrders) {
                        String statusValue = order.getStatus() != null ? order.getStatus().trim() : "";

                        boolean isCompleted = statusValue.equalsIgnoreCase("Completed");

                        boolean shouldInclude = (isOngoing && !isCompleted) || (!isOngoing && isCompleted);

                        if (shouldInclude && order.getItems() != null) {
                            for (OrderItem item : order.getItems()) {
                                // "Phẳng hóa": Mỗi item hiển thị 1 thẻ, lấy status của order cha để hiện lên badge
                                displayList.add(new OrderAdapter.DisplayItem(order, item));
                            }
                        }
                    }
                    
                    adapter.notifyDataSetChanged();

                    if (displayList.isEmpty()) {
                        showEmptyState();
                    } else {
                        emptyState.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Lỗi tải đơn hàng: " + e.getMessage());
                    showEmptyState();
                });
    }

    private void showEmptyState() {
        updateEmptyStateUI();
        emptyState.setVisibility(View.VISIBLE);
        rvOrders.setVisibility(View.GONE);
    }

    private void updateEmptyStateUI() {
        if (isOngoing) {
            tvEmptyTitle.setText("No Ongoing Orders!");
            tvEmptyMessage.setText("You don’t have any ongoing orders at this time.");
        } else {
            tvEmptyTitle.setText("No Completed Orders!");
            tvEmptyMessage.setText("You don’t have any completed orders at this time.");
        }
        if (imgEmpty != null) {
            imgEmpty.setImageResource(R.drawable.box);
            imgEmpty.setAlpha(0.2f);
        }
    }

    @Override
    public void onTrackOrder(Order order) {
        replaceFragment(TrackOrderFragment.newInstance(order.getOrderId()));
    }

    @Override
    public void onLeaveReview(OrderItem item) {
        showLeaveReviewDialog(item.getProductId(), item.getProductName());
    }

    private void showLeaveReviewDialog(String productId, String productName) {
        LeaveReviewBottomSheet bottomSheet = LeaveReviewBottomSheet.newInstance(productId, productName);
        bottomSheet.show(getChildFragmentManager(), "LeaveReviewBottomSheet");
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
