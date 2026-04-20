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
import com.example.tech_store_mobile.Model.Category;
import com.example.tech_store_mobile.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private static final String TAG = "CategoryAdapter";
    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);

        // Đổ tên danh mục từ Model Category vào TextView
        holder.tvName.setText(category.getCategoryName());

        // Load ảnh từ Firebase URL bằng Glide (hoặc fallback to local drawable)
        String imageUrl = category.getImageUrl();
        Log.d(TAG, "Category: " + category.getCategoryName() + ", imageUrl: " + imageUrl);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            // Có URL từ Firebase → load ảnh từ Cloudinary/URL
            Log.d(TAG, "   Loading from Firebase URL: " + imageUrl);
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.laptop)  // Placeholder khi loading
                    .error(getFallbackDrawable(category))  // Fallback nếu URL lỗi
                    .into(holder.imgCategory);
        } else {
            // Không có URL → fallback to local drawable
            Log.d(TAG, "   Using fallback drawable");
            holder.imgCategory.setImageResource(getFallbackDrawable(category));
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCategoryClick(category);
            }
        });
    }

    /**
     * Fallback drawable dựa trên category name (khi không có URL từ Firebase)
     */
    private int getFallbackDrawable(Category category) {
        switch (category.getCategoryName()) {
            case "Smartphone":
                return R.drawable.smart_phone;
            case "Laptop":
                return R.drawable.laptop;
            case "Watch":
                return R.drawable.watch;
            case "Screen":
                return R.drawable.screen;
            default:
                return R.drawable.laptop;
        }
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView tvName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            tvName = itemView.findViewById(R.id.tvCategoryName);
        }
    }
}