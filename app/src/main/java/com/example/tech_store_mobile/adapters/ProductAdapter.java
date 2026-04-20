package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {
    private List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
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

        // Đổ dữ liệu từ Model vào View
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText("$ " + product.getFinalPrice());
        holder.tvRating.setText(String.valueOf(product.getRating()));

        // Tạm thời dùng ảnh watch bạn đã có để test giao diện
        // Sau này có Firebase, bạn sẽ dùng thư viện Glide để load product.getImageUrl()
        holder.imgProduct.setImageResource(R.drawable.watch);

        // Xử lý nút trái tim (tym)
        holder.btnHeart.setOnClickListener(v -> {
            // Logic xử lý yêu thích sẽ viết ở đây
        });
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
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