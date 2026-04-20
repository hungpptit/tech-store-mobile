package com.example.tech_store_mobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Category;
import com.example.tech_store_mobile.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
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

        // Xử lý hiển thị ảnh dựa trên tên danh mục (Vì bạn chưa có link ảnh online)
        switch (category.getCategoryName()) {
            case "Smart Phone":
                holder.imgCategory.setImageResource(R.drawable.smart_phone);
                break;
            case "Laptop":
                holder.imgCategory.setImageResource(R.drawable.laptop);
                break;
            case "Watch":
                holder.imgCategory.setImageResource(R.drawable.watch);
                break;
            case "Screen":
                holder.imgCategory.setImageResource(R.drawable.screen);
                break;
            default:
                holder.imgCategory.setImageResource(R.drawable.laptop); // Ảnh mặc định
                break;
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