package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * HomeFragment - Màn hình trang chủ
 * Hiển thị:
 * - Categories (từ Firebase)
 * - New Products (từ Firebase)
 * - Best Sellers (từ Firebase)
 */
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    // Views
    private RecyclerView rvCategories, rvProducts, rvBestSellers;

    // Firebase
    private FirebaseFirestore db;

    // Adapters
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter, bestSellerAdapter;

    // Data
    private List<Category> categoryList;
    private List<Product> productList, bestSellersList;

    // Flags
    private boolean isFirstLoad = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Init data lists
        categoryList = new ArrayList<>();
        productList = new ArrayList<>();
        bestSellersList = new ArrayList<>();

        // Map Views
        rvCategories = view.findViewById(R.id.rv_categories_home);
        rvProducts = view.findViewById(R.id.rv_products_home);
        rvBestSellers = view.findViewById(R.id.rv_best_sellers);

        // Setup RecyclerViews
        setupCategoryRecyclerView();
        setupProductRecyclerView();
        setupBestSellerRecyclerView();

        // Load Data from Firebase
        loadCategoriesFromFirebase();
        loadNewProductsFromFirebase();
        loadBestSellersFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 HomeFragment onResume - isFirstLoad: " + isFirstLoad);

        // Nếu không phải lần đầu load, reload data (quay lại từ ProductListFragment)
        if (!isFirstLoad) {
            Log.d(TAG, "   Reloading data because we came back from another fragment");
            reloadData();
        }
        isFirstLoad = false;
    }

    /**
     * Reload data từ Firebase
     * Public method để gọi từ MainActivity khi back
     */
    public void reloadData() {
        Log.d(TAG, "📡 reloadData called - loading all data from Firebase");
        loadCategoriesFromFirebase();
        loadNewProductsFromFirebase();
        loadBestSellersFromFirebase();
    }

    // ==================== SETUP RECYCLERVIEW ====================

    private void setupCategoryRecyclerView() {
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categoryList);
        categoryAdapter.setOnCategoryClickListener(category -> {
            // Navigate to ProductListFragment
            Log.d(TAG, "📌 Category clicked - ID: " + category.getCategoryId() + ", Name: " + category.getCategoryName());
            ProductListFragment fragment = ProductListFragment.newInstance(
                    category.getCategoryId(),
                    category.getCategoryName()
            );
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();

            // Hide ViewPager and show fragment container
            requireActivity().findViewById(R.id.view_pager).setVisibility(View.GONE);
            requireActivity().findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupProductRecyclerView() {
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setNestedScrollingEnabled(false);
        productAdapter = new ProductAdapter(productList);

        // Add click listener for new products
        productAdapter.setOnProductClickListener(product -> {
            Log.d(TAG, "🛍️ New Product clicked: " + product.getProductName());
            navigateToProductDetail(product.getProductId());
        });

        rvProducts.setAdapter(productAdapter);
    }

    private void setupBestSellerRecyclerView() {
        rvBestSellers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvBestSellers.setNestedScrollingEnabled(false);
        bestSellerAdapter = new ProductAdapter(bestSellersList);

        // Add click listener for best sellers
        bestSellerAdapter.setOnProductClickListener(product -> {
            Log.d(TAG, "🛍️ Best Seller clicked: " + product.getProductName());
            navigateToProductDetail(product.getProductId());
        });

        rvBestSellers.setAdapter(bestSellerAdapter);
    }

    private void navigateToProductDetail(String productId) {
        Log.d(TAG, "🔀 Navigating to ProductDetailFragment for product: " + productId);

        // Hide ViewPager and show fragment container
        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);

        // Create and navigate to ProductDetailFragment
        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(productId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    // ==================== LOAD FROM FIREBASE ====================

    /**
     * Lấy danh sách categories từ Firebase
     * Sắp xếp theo displayOrder
     */
    private void loadCategoriesFromFirebase() {
        db.collection("categories")
                .orderBy("displayOrder")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        categoryList.clear();
                        categoryList.addAll(task.getResult().toObjects(Category.class));
                        categoryAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Categories loaded: " + categoryList.size());
                        
                        // Debug: print all categories
                        for (Category cat : categoryList) {
                            Log.d(TAG, "   - Category ID: " + cat.getCategoryId() + ", Name: " + cat.getCategoryName());
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading categories", task.getException());
                        Toast.makeText(getContext(), "Error loading categories", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Lấy danh sách sản phẩm mới (isNew = true)
     * Giới hạn 4 sản phẩm
     */
    private void loadNewProductsFromFirebase() {
        db.collection("products")
                .whereEqualTo("isNew", true)
                .limit(4)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        productList.addAll(task.getResult().toObjects(Product.class));
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ New products loaded: " + productList.size());
                    } else {
                        Log.e(TAG, "❌ Error loading new products", task.getException());
                    }
                });
    }

    /**
     * Lấy danh sách sản phẩm bán chạy (isBestSeller = true)
     * Giới hạn 4 sản phẩm
     */
    private void loadBestSellersFromFirebase() {
        db.collection("products")
                .whereEqualTo("isBestSeller", true)
                .limit(4)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        bestSellersList.clear();
                        bestSellersList.addAll(task.getResult().toObjects(Product.class));
                        bestSellerAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Best sellers loaded: " + bestSellersList.size());
                    } else {
                        Log.e(TAG, "❌ Error loading best sellers", task.getException());
                    }
                });
    }
}

