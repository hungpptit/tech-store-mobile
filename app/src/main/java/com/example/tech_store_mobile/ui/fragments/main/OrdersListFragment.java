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
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OrdersListFragment extends Fragment implements OrderAdapter.OnOrderActionListener {

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
        String userId = AuthManager.getCurrentUid();
        if (userId == null) return;

        progressBar.setVisibility(View.VISIBLE);
        
        List<String> ongoingStatuses = Arrays.asList("Packing", "Picked", "In Transit", "Shipping");
        List<String> completedStatuses = Arrays.asList("Delivered", "Completed", "Cancelled");

        Query query = db.collection("orders")
                .whereEqualTo("userId", userId)
                .orderBy("orderDate", Query.Direction.DESCENDING);

        if (isOngoing) {
            query = query.whereIn("status", ongoingStatuses);
        } else {
            query = query.whereIn("status", completedStatuses);
        }

        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            progressBar.setVisibility(View.GONE);
            displayList.clear();
            
            List<Order> rawOrders = queryDocumentSnapshots.toObjects(Order.class);
            for (Order order : rawOrders) {
                if (order.getItems() != null) {
                    for (OrderItem item : order.getItems()) {
                        // Thêm từng sản phẩm vào danh sách hiển thị riêng biệt
                        displayList.add(new OrderAdapter.DisplayItem(order, item));
                    }
                }
            }
            
            adapter.notifyDataSetChanged();

            if (displayList.isEmpty()) {
                updateEmptyStateUI();
                emptyState.setVisibility(View.VISIBLE);
                rvOrders.setVisibility(View.GONE);
            } else {
                emptyState.setVisibility(View.GONE);
                rvOrders.setVisibility(View.VISIBLE);
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Log.e("OrdersListFragment", "Error loading orders", e);
        });
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
        // Sử dụng fragment_container chính của Activity để đè lên ViewPager
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
