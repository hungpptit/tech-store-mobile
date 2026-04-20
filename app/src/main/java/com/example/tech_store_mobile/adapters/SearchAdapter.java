package com.example.tech_store_mobile.adapters;

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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {
    private List<Product> productList;
    private OnItemClickListener listener;

    public interface OnItemClickListener { void onItemClick(Product product); }

    public SearchAdapter(List<Product> productList, OnItemClickListener listener) {
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Product product = productList.get(position);
        holder.tvName.setText(product.getProductName());
        holder.tvPrice.setText("$ " + (product.getFinalPrice() != null ? product.getFinalPrice() : 0.0));

        Glide.with(holder.itemView.getContext())
                .load(product.getImageUrl())
                .placeholder(R.drawable.watch)
                .error(R.drawable.watch)
                .into(holder.imgProduct);

        // Xử lý Click: Nhấn vào bất kỳ đâu trên item đều mở chi tiết
        View.OnClickListener clickHandler = v -> {
            if (listener != null) listener.onItemClick(product);
        };

        holder.itemView.setOnClickListener(clickHandler);
        if (holder.imgArrow != null) {
            holder.imgArrow.setOnClickListener(clickHandler);
        }
    }

    @Override
    public int getItemCount() { return productList != null ? productList.size() : 0; }

    public static class SearchViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct, imgArrow;
        TextView tvName, tvPrice;
        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProductSearch);
            tvName = itemView.findViewById(R.id.tvProductNameSearch);
            tvPrice = itemView.findViewById(R.id.tvProductPriceSearch);
            imgArrow = itemView.findViewById(R.id.imgArrow); // Khớp ID với XML của bạn
        }
    }
}