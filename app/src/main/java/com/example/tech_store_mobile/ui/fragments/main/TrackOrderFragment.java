package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Order;
import com.example.tech_store_mobile.Model.TrackingHistoryItem;
import com.example.tech_store_mobile.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TrackOrderFragment extends Fragment {

    private static final String ARG_ORDER_ID = "order_id";
    private String orderId;
    private TrackingAdapter adapter;
    private final List<TrackingStep> displaySteps = new ArrayList<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Định nghĩa 4 bước cố định theo thiết kế
    private static final String[] STATUS_SEQUENCE = {"Packing", "Picked", "In Transit", "Delivered"};

    public static TrackOrderFragment newInstance(String orderId) {
        TrackOrderFragment fragment = new TrackOrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ORDER_ID, orderId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            orderId = getArguments().getString(ARG_ORDER_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_order, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back_track).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageView btnClose = view.findViewById(R.id.btn_close_track);
        if (btnClose != null) btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        RecyclerView rvTracking = view.findViewById(R.id.rv_tracking_history);
        rvTracking.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo 4 bước rỗng ban đầu (Clear để tránh lặp khi Fragment bị recreate)
        displaySteps.clear();
        for (String status : STATUS_SEQUENCE) {
            displaySteps.add(new TrackingStep(status));
        }

        adapter = new TrackingAdapter(displaySteps);
        rvTracking.setAdapter(adapter);

        loadOrderTracking();
    }

    private void loadOrderTracking() {
        if (orderId == null) return;

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Order order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        mapOrderDataToSteps(order);
                    }
                })
                .addOnFailureListener(e -> Log.e("TrackOrderFragment", "Error loading tracking", e));
    }

    private void mapOrderDataToSteps(Order order) {
        // Trong TrackOrderFragment.javaprivate void mapOrderDataToSteps(Order order) {
        List<TrackingHistoryItem> history = order.getTrackingHistory();
        // Lấy địa chỉ giao hàng cuối cùng từ snapshot đơn hàng
        String destination = order.getShippingAddress() != null ? order.getShippingAddress().getFullAddress() : "";

        // Sửa lỗi .size() -> .length cho mảng STATUS_SEQUENCE
        for (int i = 0; i < STATUS_SEQUENCE.length; i++) {
            String statusName = STATUS_SEQUENCE[i];
            TrackingStep step = displaySteps.get(i);

            // Tìm xem bước này đã thực hiện chưa trong mảng Firestore
            TrackingHistoryItem match = findInHistory(history, statusName);
            if (match != null) {
                step.completed = true;
                step.location = match.getLocation();
                step.timestamp = match.getTimestamp() != null ?
                        new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.US).format(match.getTimestamp().toDate()) : "";
            } else if (statusName.equals("Delivered")) {
                // Hiển thị trước địa chỉ đích tại bước Delivered cho dù chưa giao xong
                step.location = destination;
            }
        }
        adapter.notifyDataSetChanged();

    }

    private TrackingHistoryItem findInHistory(List<TrackingHistoryItem> history, String statusName) {
        if (history == null) return null;
        for (TrackingHistoryItem item : history) {
            if (statusName.equalsIgnoreCase(item.getStatusName())) return item;
        }
        return null;
    }

    // Class helper để quản lý hiển thị từng bước
    private static class TrackingStep {
        String status;
        String location = "";
        String timestamp = "";
        boolean completed = false;

        TrackingStep(String status) { this.status = status; }
    }

    private static class TrackingAdapter extends RecyclerView.Adapter<TrackingAdapter.ViewHolder> {
        private final List<TrackingStep> steps;

        TrackingAdapter(List<TrackingStep> steps) { this.steps = steps; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tracking_step, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            TrackingStep step = steps.get(position);
            holder.tvStatus.setText(step.status);

            if (!TextUtils.isEmpty(step.location)) {
                holder.tvLocation.setText(step.location);
                holder.tvLocation.setVisibility(View.VISIBLE);
            } else {
                holder.tvLocation.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(step.timestamp)) {
                holder.tvTime.setText(step.timestamp);
                holder.tvTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            // UI Line & Dot
            holder.line.setVisibility(position == steps.size() - 1 ? View.GONE : View.VISIBLE);
            holder.dot.setImageResource(step.completed ? R.drawable.ic_dot_filled : R.drawable.ic_dot_outline);

            // Làm mờ các bước chưa tới
            holder.itemView.setAlpha(step.completed || (step.status.equals("Delivered") && !TextUtils.isEmpty(step.location)) ? 1.0f : 0.4f);
        }

        @Override
        public int getItemCount() { return steps.size(); }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvStatus, tvLocation, tvTime;
            ImageView dot;
            View line;

            ViewHolder(View itemView) {
                super(itemView);
                tvStatus = itemView.findViewById(R.id.tv_tracking_status);
                tvLocation = itemView.findViewById(R.id.tv_tracking_location);
                tvTime = itemView.findViewById(R.id.tv_tracking_time);
                dot = itemView.findViewById(R.id.img_tracking_dot);
                line = itemView.findViewById(R.id.view_tracking_line);
            }
        }
    }
}
