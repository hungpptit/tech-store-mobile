package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.ProductAdapter;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductListFragment - Hiển thị danh sách sản phẩm của một category
 *
 * Arguments:
 * - categoryId: ID của category
 * - categoryName: Tên category (hiển thị trên header)
 */
public class ProductListFragment extends Fragment {
    private static final String TAG = "ProductListFragment";
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY_NAME = "categoryName";

    // Views
    private ImageView btnBack;
    private TextView tvCategoryName;
    private RecyclerView rvProducts;

    // Firebase
    private FirebaseFirestore db;

    // Adapters & Data
    private ProductAdapter productAdapter;
    private List<Product> productList;

    // Arguments
    private String categoryId;
    private String categoryName;

    public ProductListFragment() {
    }

    public static ProductListFragment newInstance(String categoryId, String categoryName) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Init data list
        productList = new ArrayList<>();

        // Map Views
        btnBack = view.findViewById(R.id.btnBack);
        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        rvProducts = view.findViewById(R.id.rvProducts);

        // Set category name
        tvCategoryName.setText(categoryName);

        // Setup RecyclerView
        setupProductRecyclerView();

        // Load products by category
        loadProductsByCategory();

        // Back button
        btnBack.setOnClickListener(v -> requireActivity().onBackPressed());

        return view;
    }

    private void setupProductRecyclerView() {
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setNestedScrollingEnabled(false);
        productAdapter = new ProductAdapter(productList);
        rvProducts.setAdapter(productAdapter);
    }

    /**
     * Lấy danh sách sản phẩm theo categoryId
     */
    private void loadProductsByCategory() {
        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        productList.addAll(task.getResult().toObjects(Product.class));
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Products loaded for category " + categoryName + ": " + productList.size());
                    } else {
                        Log.e(TAG, "❌ Error loading products", task.getException());
                        Toast.makeText(getContext(), "Error loading products", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

