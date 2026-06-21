package com.example.tech_store_mobile.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;

import java.util.List;

public class SearchAdapter extends BaseAdapter {
    private Context context;
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public SearchAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return productList != null ? productList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return productList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_search_result, parent, false);
            holder = new SearchViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SearchViewHolder) convertView.getTag();
        }

        Product product = productList.get(position);
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText("$ " + (product.getFinalPrice() != null ? product.getFinalPrice() : 0.0));

        Glide.with(context)
                .load(product.getImageUrl())
                .placeholder(R.drawable.watch)
                .error(R.drawable.watch)
                .into(holder.imgProduct);

        // Xử lý Click được thực hiện qua ListView.setOnItemClickListener hoặc trực tiếp tại đây
        convertView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(product);
        });

        return convertView;
    }

    static class SearchViewHolder {
        ImageView imgProduct, imgArrow;
        TextView tvName, tvPrice;

        public SearchViewHolder(View view) {
            imgProduct = view.findViewById(R.id.imgProductSearch);
            tvName = view.findViewById(R.id.tvProductNameSearch);
            tvPrice = view.findViewById(R.id.tvProductPriceSearch);
            imgArrow = view.findViewById(R.id.imgArrow);
        }
    }
}