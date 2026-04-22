package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.R;

import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EMPTY = 0;
    private static final int TYPE_ITEM = 1;

    private final List<CartEntry> cartItems;
    private final OnCartActionListener actionListener;

    public CartAdapter(List<CartEntry> cartItems, OnCartActionListener actionListener) {
        this.cartItems = cartItems;
        this.actionListener = actionListener;
    }

    public static class CartEntry {
        private final String productId;
        private final String productName;
        private final String selectedColor;
        private final int imageResId;
        private final double priceAtAdded;
        private int quantity;

        public CartEntry(String productId, String productName, String selectedColor, int imageResId, double priceAtAdded, int quantity) {
            this.productId = productId;
            this.productName = productName;
            this.selectedColor = selectedColor;
            this.imageResId = imageResId;
            this.priceAtAdded = priceAtAdded;
            this.quantity = quantity;
        }

        public String getProductId() { return productId; }
        public String getProductName() { return productName; }
        public String getSelectedColor() { return selectedColor; }
        public int getImageResId() { return imageResId; }
        public double getPriceAtAdded() { return priceAtAdded; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public double getLineTotal() { return priceAtAdded * quantity; }
    }

    public interface OnCartActionListener {
        void onIncreaseQuantity(int position);
        void onDecreaseQuantity(int position);
        void onDeleteItem(int position);
    }

    @Override
    public int getItemViewType(int position) {
        return cartItems.isEmpty() ? TYPE_EMPTY : TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        return cartItems.isEmpty() ? 1 : cartItems.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_EMPTY) {
            return new EmptyViewHolder(inflater.inflate(R.layout.item_cart_empty, parent, false));
        }
        return new ItemViewHolder(inflater.inflate(R.layout.item_cart, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ITEM) {
            bindItem((ItemViewHolder) holder, position);
        }
    }

    private void bindItem(ItemViewHolder holder, int position) {
        CartEntry item = cartItems.get(position);
        holder.imgProduct.setImageResource(item.getImageResId());
        holder.tvProductName.setText(item.getProductName());
        holder.tvColor.setText(item.getSelectedColor());
        holder.tvPrice.setText(formatMoney(item.getPriceAtAdded()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnDelete.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDeleteItem(position);
        });
        holder.btnDecrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onDecreaseQuantity(position);
        });
        holder.btnIncrease.setOnClickListener(v -> {
            if (actionListener != null) actionListener.onIncreaseQuantity(position);
        });
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "$ %.2f", amount);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        ImageView btnDelete;
        TextView btnDecrease;
        TextView btnIncrease;
        TextView tvProductName;
        TextView tvColor;
        TextView tvPrice;
        TextView tvQuantity;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnDecrease = itemView.findViewById(R.id.btnDecrease);
            btnIncrease = itemView.findViewById(R.id.btnIncrease);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvColor = itemView.findViewById(R.id.tvColor);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}



