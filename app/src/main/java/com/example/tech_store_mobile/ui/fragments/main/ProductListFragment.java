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

        // Set category name
        tvCategoryName.setText(categoryName);

        // Setup RecyclerView
        setupProductRecyclerView();

        // Load products by category
        loadProductsByCategory();

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

                        if (productList.size() == 0) {
                            Toast.makeText(getContext(), "No products found for this category", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "❌ Error loading products", task.getException());
                        Toast.makeText(getContext(), "Error loading products: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

