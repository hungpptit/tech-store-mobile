package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;
// imageview references removed; badge is handled in MainActivity


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
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.AuthUiHelper;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
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
    private TextView tvViewAllNewProducts, tvViewAllBestSellers;

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
    private NotificationBadgeManager.BadgeListener badgeListener;
    private TextView notificationBadgeView;

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
        // Notification badge is managed centrally by MainActivity
        rvCategories = view.findViewById(R.id.rv_categories_home);
        rvProducts = view.findViewById(R.id.rv_products_home);
        rvBestSellers = view.findViewById(R.id.rv_best_sellers);
        tvViewAllNewProducts = view.findViewById(R.id.tvViewAllNewProducts);
        tvViewAllBestSellers = view.findViewById(R.id.tvViewAllBestSellers);

        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }

        // Setup RecyclerViews
        setupCategoryRecyclerView();
        setupProductRecyclerView();
        setupBestSellerRecyclerView();
        setupViewAllActions();

        // Load Data from Firebase
        loadCategoriesFromFirebase();
        loadNewProductsFromFirebase();
        loadBestSellersFromFirebase();

        return view;
    }

    // Badge updates handled by MainActivity

    @Override
    public void onResume() {
        super.onResume();
        startNotificationBadgeListener();
        Log.d(TAG, "🔄 HomeFragment onResume - isFirstLoad: " + isFirstLoad);

        // Nếu không phải lần đầu load, reload data (quay lại từ ProductListFragment)
        if (!isFirstLoad) {
            Log.d(TAG, "   Reloading data because we came back from another fragment");
            reloadData();
        }
        isFirstLoad = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNotificationBadgeListener();
    }

    private void startNotificationBadgeListener() {
        badgeListener = unreadCount -> {
            if (notificationBadgeView == null) return;
            if (unreadCount <= 0) {
                notificationBadgeView.setVisibility(View.GONE);
            } else {
                notificationBadgeView.setVisibility(View.VISIBLE);
                notificationBadgeView.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            }
        };
        NotificationBadgeManager.getInstance().addListener(badgeListener);
        NotificationBadgeManager.getInstance().start();
    }

    private void stopNotificationBadgeListener() {
        if (badgeListener != null) {
            NotificationBadgeManager.getInstance().removeListener(badgeListener);
        }
        NotificationBadgeManager.getInstance().stop();
    }

    private void navigateToNotifications() {
        if (!isAdded() || getActivity() == null) return;
        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        NotificationsFragment fragment = new NotificationsFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
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
            navigateToProductListFragment(fragment);
        });
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupViewAllActions() {
        if (tvViewAllNewProducts != null) {
            tvViewAllNewProducts.setOnClickListener(v -> {
                Log.d(TAG, "📌 View all New Products clicked");
                navigateToProductListFragment(ProductListFragment.newInstanceForNewProducts());
            });
        }

        if (tvViewAllBestSellers != null) {
            tvViewAllBestSellers.setOnClickListener(v -> {
                Log.d(TAG, "📌 View all Best Sellers clicked");
                navigateToProductListFragment(ProductListFragment.newInstanceForBestSellers());
            });
        }
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

        productAdapter.setOnHeartClickListener(this::toggleSavedItem);

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

        bestSellerAdapter.setOnHeartClickListener(this::toggleSavedItem);

        rvBestSellers.setAdapter(bestSellerAdapter);
    }

    private void toggleSavedItem(Product product, int position) {
        if (!AuthUiHelper.requireLogin(this, R.string.login_required_title, R.string.login_required_favorite_message)) {
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null || product == null || product.getProductId() == null) {
            Toast.makeText(getContext(), "Unable to save item right now.", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = userId + "_" + product.getProductId();
        db.collection("saved_items").document(docId).get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        db.collection("saved_items").document(docId).delete()
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Removed from saved items", Toast.LENGTH_SHORT).show());
                    } else {
                        java.util.Map<String, Object> savedData = new java.util.HashMap<>();
                        savedData.put("userId", userId);
                        savedData.put("productId", product.getProductId());

                        db.collection("saved_items").document(docId).set(savedData)
                                .addOnSuccessListener(unused -> Toast.makeText(getContext(), "Saved successfully", Toast.LENGTH_SHORT).show());
                    }
                });
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

    private void navigateToProductListFragment(ProductListFragment fragment) {
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();

        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);
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

