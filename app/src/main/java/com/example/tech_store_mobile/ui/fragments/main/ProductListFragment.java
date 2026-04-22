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
import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.adapters.ProductAdapter;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.AuthUiHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ProductListFragment - Hiển thị danh sách sản phẩm theo category hoặc theo bộ lọc đặc biệt
 *
 * Arguments:
 * - mode: category | new_products | best_sellers
 * - categoryId: ID của category (chỉ dùng khi mode = category)
 * - categoryName / displayTitle: Tên hiển thị trên header
 */
public class ProductListFragment extends Fragment {
    private static final String TAG = "ProductListFragment";
    private static final String ARG_MODE = "mode";
    private static final String ARG_CATEGORY_ID = "categoryId";
    private static final String ARG_CATEGORY_NAME = "categoryName";
    private static final String ARG_DISPLAY_TITLE = "displayTitle";

    private static final String MODE_CATEGORY = "category";
    private static final String MODE_NEW_PRODUCTS = "new_products";
    private static final String MODE_BEST_SELLERS = "best_sellers";

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
    private String mode;
    private String categoryId;
    private String categoryName;
    private String displayTitle;

    public ProductListFragment() {
    }

    public static ProductListFragment newInstance(String categoryId, String categoryName) {
        return newInstanceInternal(MODE_CATEGORY, categoryId, categoryName, categoryName);
    }

    public static ProductListFragment newInstanceForNewProducts() {
        return newInstanceInternal(MODE_NEW_PRODUCTS, null, null, "New Products");
    }

    public static ProductListFragment newInstanceForBestSellers() {
        return newInstanceInternal(MODE_BEST_SELLERS, null, null, "Best Sellers");
    }

    private static ProductListFragment newInstanceInternal(String mode, String categoryId, String categoryName, String displayTitle) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_MODE, mode);
        args.putString(ARG_CATEGORY_ID, categoryId);
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putString(ARG_DISPLAY_TITLE, displayTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mode = getArguments().getString(ARG_MODE, MODE_CATEGORY);
            categoryId = getArguments().getString(ARG_CATEGORY_ID);
            categoryName = getArguments().getString(ARG_CATEGORY_NAME);
            displayTitle = getArguments().getString(ARG_DISPLAY_TITLE, categoryName);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_list, container, false);

        Log.d(TAG, "=== ProductListFragment onCreateView called ===");
        Log.d(TAG, "categoryId: " + categoryId);
        Log.d(TAG, "categoryName: " + categoryName);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Init data list
        productList = new ArrayList<>();

        // Map Views
        btnBack = view.findViewById(R.id.btnBack);
        tvCategoryName = view.findViewById(R.id.tvCategoryName);
        rvProducts = view.findViewById(R.id.rvProducts);

        // Set title
        tvCategoryName.setText(displayTitle);

        // Setup RecyclerView
        setupProductRecyclerView();

        // Load products by selected mode
        loadProductsForCurrentMode();

        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "🔙 Back button clicked");

            // Pop fragment
            if (isAdded()) {
                requireActivity().getSupportFragmentManager().popBackStack();

                // Show ViewPager and hide fragment container with delay to ensure proper timing
                v.postDelayed(() -> {
                    // Check if fragment is still attached before accessing activity
                    if (isAdded()) {
                        View viewPager = requireActivity().findViewById(R.id.view_pager);
                        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

                        if (viewPager != null) viewPager.setVisibility(View.VISIBLE);
                        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
                        if (requireActivity() instanceof MainActivity) {
                            ((MainActivity) requireActivity()).syncBottomNavigationVisibility();
                        }

                        Log.d(TAG, "   ViewPager visibility restored");
                    } else {
                        Log.d(TAG, "   Fragment detached, skipping visibility update");
                    }
                }, 100);
            }
        });

        return view;
    }

    private void setupProductRecyclerView() {
        Log.d(TAG, "🔧 setupProductRecyclerView called");
        Log.d(TAG, "   rvProducts width: " + rvProducts.getWidth());
        Log.d(TAG, "   rvProducts height: " + rvProducts.getHeight());
        
        rvProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvProducts.setNestedScrollingEnabled(true);  // Enable nested scrolling
        productAdapter = new ProductAdapter(productList);

        // Set click listener
        productAdapter.setOnProductClickListener(product -> {
            Log.d(TAG, "📱 Product clicked: " + product.getProductName());
            navigateToProductDetail(product.getProductId());
        });

        productAdapter.setOnHeartClickListener(this::toggleSavedItem);

        rvProducts.setAdapter(productAdapter);
        
        Log.d(TAG, "   Adapter set. getItemCount: " + productAdapter.getItemCount());
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

    private void loadProductsForCurrentMode() {
        if (MODE_NEW_PRODUCTS.equals(mode)) {
            loadProductsByBooleanField("isNew", "New Products");
            return;
        }

        if (MODE_BEST_SELLERS.equals(mode)) {
            loadProductsByBooleanField("isBestSeller", "Best Sellers");
            return;
        }

        loadProductsByCategory();
    }

    /**
     * Lấy danh sách sản phẩm theo categoryId
     * Thử 2 cách: đầu tiên bằng categoryId, nếu không có kết quả thì tìm bằng categoryName
     */
    private void loadProductsByCategory() {
        Log.d(TAG, "🔍 Loading products for categoryId: " + categoryId + ", categoryName: " + categoryName);
        
        // Cách 1: Tìm bằng categoryId
        db.collection("products")
                .whereEqualTo("categoryId", categoryId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        int count = task.getResult().size();
                        Log.d(TAG, "   Query by categoryId returned: " + count + " products");
                        
                        if (count > 0) {
                            // Nếu có kết quả, dùng kết quả này
                            productList.clear();
                            productList.addAll(task.getResult().toObjects(Product.class));
                            Log.d(TAG, "   Before notifyDataSetChanged: productList size = " + productList.size());
                            productAdapter.notifyDataSetChanged();
                            Log.d(TAG, "   After notifyDataSetChanged");
                            Log.d(TAG, "✅ Products loaded (by ID) for category " + categoryName + ": " + count);
                        } else {
                            // Nếu không có kết quả, thử tìm bằng categoryName
                            Log.d(TAG, "   No products found by categoryId, trying by categoryName...");
                            loadProductsByCategoryName();
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading products by categoryId", task.getException());
                        // Nếu có lỗi, thử cách khác
                        loadProductsByCategoryName();
                    }
                });
    }

    private void loadProductsByBooleanField(String fieldName, String emptyMessageLabel) {
        Log.d(TAG, "🔍 Loading products by field: " + fieldName);

        db.collection("products")
                .whereEqualTo(fieldName, true)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        productList.clear();
                        productList.addAll(task.getResult().toObjects(Product.class));
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Products loaded by " + fieldName + ": " + productList.size());

                        if (productList.isEmpty()) {
                            Toast.makeText(getContext(), "No products found for " + emptyMessageLabel, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading products by " + fieldName, task.getException());
                        Exception exception = task.getException();
                        String message = exception != null && exception.getMessage() != null ? exception.getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Error loading products: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Tìm sản phẩm theo tên category (dùng searchKeywords hoặc tìm kiếm khác)
     */
    private void loadProductsByCategoryName() {
        Log.d(TAG, "🔍 Trying alternative search for category: " + categoryName);
        
        db.collection("products")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "   Total products in Firebase: " + task.getResult().size());

                        // In tất cả các sản phẩm để debug
                        for (Product p : task.getResult().toObjects(Product.class)) {
                            Log.d(TAG, "   - Product: " + p.getProductName() + ", categoryId: " + p.getCategoryId());
                        }

                        productList.clear();
                        
                        // Lọc sản phẩm theo categoryName từ searchKeywords hoặc tìm kiếm khác
                        for (Product product : task.getResult().toObjects(Product.class)) {
                            // Kiểm tra nếu categoryId hoặc searchKeywords chứa categoryName
                            if (product.getCategoryId() != null && product.getCategoryId().equalsIgnoreCase(categoryName)) {
                                productList.add(product);
                            } else if (product.getSearchKeywords() != null && 
                                       product.getSearchKeywords().contains(categoryName)) {
                                productList.add(product);
                            }
                        }
                        
                        productAdapter.notifyDataSetChanged();
                        Log.d(TAG, "✅ Products loaded (by Name) for category " + categoryName + ": " + productList.size());

                        if (productList.isEmpty()) {
                            Toast.makeText(getContext(), "No products found for this category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading products", task.getException());
                        Exception exception = task.getException();
                        String message = exception != null && exception.getMessage() != null ? exception.getMessage() : "Unknown error";
                        Toast.makeText(getContext(), "Error loading products: " + message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
