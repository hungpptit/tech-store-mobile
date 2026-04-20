package com.example.tech_store_mobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private static final String TAG = "ProductAdapter";
    private List<Product> productList;
    private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    public void setOnProductClickListener(OnProductClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp cái khuôn item_product vào Adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        Log.d(TAG, "onBindViewHolder called - position: " + position + ", product: " + product.getProductName());

        // Đổ dữ liệu từ Model vào View
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText("$ " + product.getFinalPrice());
        holder.tvRating.setText(String.valueOf(product.getRating()));

        // Load ảnh từ Firebase URL bằng Glide
        // Nếu imageUrl trống hoặc null → luôn dùng placeholder
        String imageUrl = product.getImageUrl();
        Log.d(TAG, "   imageUrl: '" + imageUrl + "'");

        if (imageUrl != null && !imageUrl.isEmpty() && !imageUrl.equals("")) {
            // Có URL hợp lệ → load từ Firebase
            Log.d(TAG, "   Loading from Firebase URL");
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.watch)  // Ảnh tạm thời khi loading
                    .error(R.drawable.watch)         // Ảnh nếu lỗi
                    .into(holder.imgProduct);
        } else {
            // imageUrl trống → set placeholder ngay, không gọi Glide
            Log.d(TAG, "   Using placeholder (imageUrl is empty)");
            holder.imgProduct.setImageResource(R.drawable.watch);
        }

        // Xử lý nút trái tim (tym)
        holder.btnHeart.setOnClickListener(v -> {
            // Logic xử lý yêu thích sẽ viết ở đây
        });

        // Click listener cho item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = productList != null ? productList.size() : 0;
        Log.d(TAG, "getItemCount called: " + count);
        return count;
    }

    // ViewHolder để giữ các thành phần giao diện
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, btnHeart;
        TextView tvName, tvPrice, tvRating;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}