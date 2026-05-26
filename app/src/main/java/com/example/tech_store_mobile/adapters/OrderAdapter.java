package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.Order;
import com.example.tech_store_mobile.Model.OrderItem;
import com.example.tech_store_mobile.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    public static class DisplayItem {
        public Order order;
        public OrderItem item;

        public DisplayItem(Order order, OrderItem item) {
            this.order = order;
            this.item = item;
        }
    }

    private final List<DisplayItem> displayItems;
    private final boolean isOngoing;
    private final OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onTrackOrder(Order order);
        void onLeaveReview(Order order, OrderItem item);
    }

    public OrderAdapter(List<DisplayItem> displayItems, boolean isOngoing, OnOrderActionListener listener) {
        this.displayItems = displayItems;
        this.isOngoing = isOngoing;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        DisplayItem di = displayItems.get(position);
        Order order = di.order;
        OrderItem item = di.item;

        holder.tvProductName.setText(item.getProductName());
        holder.tvProductColor.setText(item.getColor());
        holder.tvPrice.setText(String.format(Locale.US, "$ %.2f", item.getPrice()));
        
        // HIỂN THỊ STATUS CỦA ORDER LÊN BADGE (Không phải statusName của tracking)
        holder.tvStatus.setText(com.example.tech_store_mobile.utils.OrderStatusUtil.getViStatus(order.getStatus()));

        Glide.with(holder.itemView.getContext())
                .load(item.getImageUrl())
                .placeholder(R.drawable.vector)
                .into(holder.imgProduct);

        if (isOngoing) {
            holder.btnAction.setText("Track Order");
            holder.btnAction.setEnabled(true);
            holder.btnAction.setAlpha(1.0f);
            holder.btnAction.setOnClickListener(v -> listener.onTrackOrder(order));
        } else {
            if (item.getIsReviewed()) {
                holder.btnAction.setText("Reviewed");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                holder.btnAction.setOnClickListener(v -> listener.onLeaveReview(order, item));
            } else {
                holder.btnAction.setText("Leave Review");
                holder.btnAction.setEnabled(true);
                holder.btnAction.setAlpha(1.0f);
                holder.btnAction.setOnClickListener(v -> listener.onLeaveReview(order, item));
            }
        }
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName, tvProductColor, tvPrice, tvStatus;
        MaterialButton btnAction;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.img_order_product);
            tvProductName = itemView.findViewById(R.id.tv_order_product_name);
            tvProductColor = itemView.findViewById(R.id.tv_order_product_color);
            tvPrice = itemView.findViewById(R.id.tv_order_price);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            btnAction = itemView.findViewById(R.id.btn_order_action);
        }
    }
}
