package com.example.tech_store_mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tech_store_mobile.R;

import java.util.List;

public class RecentSearchAdapter extends BaseAdapter {
    private Context context;
    private List<String> historyList;
    private OnHistoryClickListener listener;

    public interface OnHistoryClickListener {
        void onKeywordClick(String keyword);
        void onDeleteClick(int position);
    }

    public RecentSearchAdapter(Context context, List<String> historyList, OnHistoryClickListener listener) {
        this.context = context;
        this.historyList = historyList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return historyList != null ? historyList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return historyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recent_search, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String keyword = historyList.get(position);
        holder.tvKeyword.setText(keyword);

        holder.tvKeyword.setOnClickListener(v -> {
            if (listener != null) listener.onKeywordClick(keyword);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(position);
        });

        return convertView;
    }

    static class ViewHolder {
        TextView tvKeyword;
        ImageView btnDelete;

        public ViewHolder(View view) {
            tvKeyword = view.findViewById(R.id.tvRecentKeyword);
            btnDelete = view.findViewById(R.id.btnDeleteHistory);
        }
    }
}