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

public class RecentSearchAdapter extends RecyclerView.Adapter<RecentSearchAdapter.ViewHolder> {
    private List<String> historyList;
    private OnHistoryClickListener listener;

    // Interface để Fragment lắng nghe sự kiện Click và Delete
    public interface OnHistoryClickListener {
        void onKeywordClick(String keyword);
        void onDeleteClick(int position);
    }

    public RecentSearchAdapter(List<String> historyList, OnHistoryClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Nạp layout item_recent_search.xml mà bạn đã làm
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recent_search, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String keyword = historyList.get(position);
        holder.tvKeyword.setText(keyword);

        // 1. Click vào text: Thực hiện tìm kiếm lại từ khóa đó
        holder.tvKeyword.setOnClickListener(v -> {
            if (listener != null) listener.onKeywordClick(keyword);
        });

        // 2. Click vào nút xóa (thùng rác/x): Xóa mục này khỏi danh sách
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;
        ImageView btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Ánh xạ ID đúng như trong file XML item_recent_search.xml của bạn
            tvKeyword = itemView.findViewById(R.id.tvRecentKeyword);
            btnDelete = itemView.findViewById(R.id.btnDeleteHistory);
        }
    }
}