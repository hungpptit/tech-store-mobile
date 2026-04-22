package com.example.tech_store_mobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Paint;
import android.graphics.PorterDuff;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.RatingFormatUtil;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
     private static final String TAG = "ProductAdapter";
     private final List<Product> productList;
     private final boolean tintHeartRed;
     private OnProductClickListener listener;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> productList) {
        this(productList, false);
    }

    public ProductAdapter(List<Product> productList, boolean tintHeartRed) {
        this.productList = productList;
        this.tintHeartRed = tintHeartRed;
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

          Double basePrice = product.getBasePrice();
          Double discountPercentage = product.getDiscountPercentage();
          Double finalPrice = product.getFinalPrice();

          if (basePrice != null) {
              holder.tvBasePrice.setVisibility(View.VISIBLE);
              holder.tvBasePrice.setText(holder.itemView.getContext().getString(
                      R.string.product_price_format,
                      String.format(Locale.getDefault(), "%.2f", basePrice)
              ));
              holder.tvBasePrice.setPaintFlags(holder.tvBasePrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
          } else {
              holder.tvBasePrice.setVisibility(View.GONE);
          }

          if (discountPercentage != null && discountPercentage > 0) {
              holder.tvDiscountPercentage.setVisibility(View.VISIBLE);
              holder.tvDiscountPercentage.setText(String.format(Locale.getDefault(), "-%.0f%%", discountPercentage));
          } else {
              holder.tvDiscountPercentage.setVisibility(View.GONE);
          }

          holder.tvPrice.setText(holder.itemView.getContext().getString(
                  R.string.product_price_format,
                  String.format(Locale.getDefault(), "%.2f", finalPrice != null ? finalPrice : 0.0)
          ));
         holder.tvRating.setText(RatingFormatUtil.formatRating(product.getRating()));

        // Load ảnh từ Firebase URL bằng Glide
        // Nếu imageUrl trống hoặc null → luôn dùng placeholder
        String imageUrl = product.getImageUrl();
        Log.d(TAG, "   imageUrl: '" + imageUrl + "'");

        if (imageUrl != null && !imageUrl.isEmpty()) {
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
        if (tintHeartRed) {
            holder.btnHeart.setColorFilter(ContextCompat.getColor(holder.itemView.getContext(), R.color.red_heart), PorterDuff.Mode.SRC_IN);
        } else {
            holder.btnHeart.clearColorFilter();
        }
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
        TextView tvName, tvBasePrice, tvDiscountPercentage, tvPrice, tvRating;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            btnHeart = itemView.findViewById(R.id.btnHeart);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvBasePrice = itemView.findViewById(R.id.tvBasePrice);
            tvDiscountPercentage = itemView.findViewById(R.id.tvDiscountPercentage);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }
}