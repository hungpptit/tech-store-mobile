package com.example.tech_store_mobile.ui.fragments.main;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.example.tech_store_mobile.Model.ShippingAddressSnapshot;
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

    private TextView tvOrderDate, tvReceiverName, tvPhone, tvAddress;

    // 5 trạng thái cố định theo yêu cầu
    private static final String[] STATUS_SEQUENCE = {"Packing", "Picked", "In Transit", "Delivered", "Completed"};

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

        // Ánh xạ các view thông tin đơn hàng và địa chỉ
        tvOrderDate = view.findViewById(R.id.tv_track_order_date);
        tvReceiverName = view.findViewById(R.id.tv_track_receiver_name);
        tvPhone = view.findViewById(R.id.tv_track_phone);
        tvAddress = view.findViewById(R.id.tv_track_address);

        RecyclerView rvTracking = view.findViewById(R.id.rv_tracking_history);
        rvTracking.setLayoutManager(new LinearLayoutManager(getContext()));

        // Khởi tạo 5 bước mặc định
        displaySteps.clear();
        for (String status : STATUS_SEQUENCE) {
            displaySteps.add(new TrackingStep(status));
        }

        adapter = new TrackingAdapter(displaySteps);
        rvTracking.setAdapter(adapter);

        loadOrderData();
    }

    private void loadOrderData() {
        if (orderId == null) return;

        db.collection("orders").document(orderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Order order = documentSnapshot.toObject(Order.class);
                    if (order != null) {
                        bindOrderHeaderInfo(order);
                        updateTimeline(order.getStatus());
                    }
                })
                .addOnFailureListener(e -> Log.e("TrackOrderFragment", "Error loading order details", e));
    }

    private void bindOrderHeaderInfo(Order order) {
        // Hiển thị ngày đặt hàng
        if (order.getOrderDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
            tvOrderDate.setText(sdf.format(order.getOrderDate().toDate()));
        }

        // Hiển thị thông tin nhận hàng
        ShippingAddressSnapshot shipping = order.getShippingAddress();
        if (shipping != null) {
            tvReceiverName.setText(shipping.getReceiverName());
            tvPhone.setText(shipping.getPhoneNumber());
            tvAddress.setText(shipping.getFullAddress());
        }
    }

    private void updateTimeline(String currentStatus) {
        int currentIndex = -1;
        
        // Tìm vị trí của trạng thái hiện tại trong chuỗi cố định
        for (int i = 0; i < STATUS_SEQUENCE.length; i++) {
            if (STATUS_SEQUENCE[i].equalsIgnoreCase(currentStatus)) {
                currentIndex = i;
                break;
            }
        }

        // Cập nhật trạng thái hiển thị cho từng bước (không dùng trackingHistory)
        for (int i = 0; i < displaySteps.size(); i++) {
            TrackingStep step = displaySteps.get(i);
            step.isReached = (i <= currentIndex);
            step.isCurrent = (i == currentIndex);
        }
        
        adapter.notifyDataSetChanged();
    }

    private static class TrackingStep {
        String status;
        boolean isReached = false;
        boolean isCurrent = false;

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

            // Không sử dụng trackingHistory nên ẩn các view chi tiết (location, time) trong mỗi bước
            holder.tvLocation.setVisibility(View.GONE);
            holder.tvTime.setVisibility(View.GONE);

            // Xử lý in đậm và màu sắc cho trạng thái
            if (step.isCurrent) {
                holder.tvStatus.setTypeface(null, Typeface.BOLD);
                holder.tvStatus.setTextColor(Color.BLACK);
                holder.dot.setImageResource(R.drawable.ic_dot_filled);
            } else if (step.isReached) {
                holder.tvStatus.setTypeface(null, Typeface.NORMAL);
                holder.tvStatus.setTextColor(Color.BLACK);
                holder.dot.setImageResource(R.drawable.ic_dot_filled);
            } else {
                holder.tvStatus.setTypeface(null, Typeface.NORMAL);
                holder.tvStatus.setTextColor(Color.parseColor("#BDBDBD"));
                holder.dot.setImageResource(R.drawable.ic_dot_outline);
            }

            // Xử lý đường nối giữa các bước
            boolean isLast = position == steps.size() - 1;
            holder.line.setVisibility(isLast ? View.GONE : View.VISIBLE);
            
            // Đường nối màu đen nếu bước TIẾP THEO đã hoàn thành hoặc hiện tại
            if (!isLast && steps.get(position + 1).isReached) {
                holder.line.setBackgroundColor(Color.BLACK);
            } else {
                holder.line.setBackgroundColor(Color.parseColor("#F2F3F2"));
            }

            // Độ mờ cho các bước chưa tới
            holder.itemView.setAlpha(step.isReached ? 1.0f : 0.4f);
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
