package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.OrderItem;
import com.example.tech_store_mobile.R;

import java.util.List;
import java.util.Locale;

public class CheckoutProductAdapter extends RecyclerView.Adapter<CheckoutProductAdapter.ViewHolder> {

    private final List<OrderItem> items;

    public CheckoutProductAdapter(List<OrderItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public CheckoutProductAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckoutProductAdapter.ViewHolder holder, int position) {
        OrderItem item = items.get(position);

        holder.tvProductName.setText(item.getProductName());
        holder.tvColor.setText(item.getColor());
        holder.tvPrice.setText(formatMoney(item.getPrice()));
        holder.tvQuantity.setText(String.format(Locale.getDefault(), "x%d", item.getQuantity()));

        if (item.getImageUrl() != null && !item.getImageUrl().trim().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.watch)
                    .error(R.drawable.watch)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.watch);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "$ %.2f", amount);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView tvProductName;
        TextView tvColor;
        TextView tvPrice;
        TextView tvQuantity;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProductCheckout);
            tvProductName = itemView.findViewById(R.id.tvProductNameCheckout);
            tvColor = itemView.findViewById(R.id.tvColorCheckout);
            tvPrice = itemView.findViewById(R.id.tvPriceCheckout);
            tvQuantity = itemView.findViewById(R.id.tvQuantityCheckout);
        }
    }
}
