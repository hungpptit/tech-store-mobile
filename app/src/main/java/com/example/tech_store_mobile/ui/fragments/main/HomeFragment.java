package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Category;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.CategoryAdapter;
import com.example.tech_store_mobile.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    // 1. Khai báo thêm rvBestSellers
    private RecyclerView rvCategories, rvProducts, rvBestSellers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Đảm bảo inflate đúng file XML có Layout tràn viền
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // 1. Ánh xạ View [cite: 13, 14]
        rvCategories = view.findViewById(R.id.rv_categories_home);
        rvProducts = view.findViewById(R.id.rv_products_home);
        rvBestSellers = view.findViewById(R.id.rv_best_sellers); // Ánh xạ thêm phần mới

        // 2. Thiết lập Dữ liệu danh mục [cite: 2, 13]
        setupCategories();

        // 3. Thiết lập Dữ liệu Sản phẩm mới [cite: 3, 4, 14]
        setupNewProducts();

        // 4. Thiết lập Dữ liệu Bán chạy [cite: 4, 14]
        setupBestSellers();

        return view;
    }

    private void setupCategories() {
        List<Category> categoryList = new ArrayList<>();
        // categoryId, categoryName, imageUrl, displayOrder [cite: 2]
        categoryList.add(new Category("1", "Smart Phone", "", 1L));
        categoryList.add(new Category("2", "Laptop", "", 2L));
        categoryList.add(new Category("3", "Watch", "", 3L));
        categoryList.add(new Category("4", "Screen", "", 4L));

        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvCategories.setAdapter(new CategoryAdapter(categoryList));
    }

    private void setupNewProducts() {
        List<Product> productList = new ArrayList<>();
        // Thêm các sản phẩm có isNew = true
        productList.add(new Product("p1", "c1", "Macbook Pro M4", "Apple", "Chip M4 mạnh mẽ",
                2089.0, 0.0, 2089.0, null, "", 5.0, 100L, 10L, true, true, null));

        productList.add(new Product("p2", "c2", "Airpods 4", "Apple", "Chống ồn chủ động",
                20.90, 0.0, 20.90, null, "", 4.5, 50L, 20L, true, false, null));

        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setNestedScrollingEnabled(false);
        rvProducts.setAdapter(new ProductAdapter(productList));
    }

    private void setupBestSellers() {
        List<Product> bestSellersList = new ArrayList<>();
        // Thêm các sản phẩm có isBestSeller = true
        bestSellersList.add(new Product("p3", "c1", "Apple Magic Mouse", "Apple", "Thiết kế tối giản",
                10.7, 0.0, 10.7, null, "", 4.5, 30L, 15L, false, true, null));

        bestSellersList.add(new Product("p4", "c1", "Macbook Air M2", "Apple", "Mỏng nhẹ",
                700.0, 0.0, 700.0, null, "", 4.7, 150L, 5L, false, true, null));

        rvBestSellers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBestSellers.setNestedScrollingEnabled(false);
        rvBestSellers.setAdapter(new ProductAdapter(bestSellersList));
    }
}