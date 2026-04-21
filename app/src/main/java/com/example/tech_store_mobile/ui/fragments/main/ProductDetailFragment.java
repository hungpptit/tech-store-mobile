package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import com.bumptech.glide.Glide;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.Model.Review;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.RatingFormatUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;

/**
 * ProductDetailFragment - Hiển thị chi tiết sản phẩm
 *
 * Arguments:
 * - productId: ID của sản phẩm
 */
public class ProductDetailFragment extends Fragment {
    private static final String TAG = "ProductDetailFragment";
    private static final String ARG_PRODUCT_ID = "productId";

    // Views
    private ImageView btnBack, btnFavorite, ivProductImage;
    private TextView tvProductName, tvBrand, tvPrice, tvRating, tvReviewCount;
    private TextView tvDescription, tvChooseColor;
    private LinearLayout llColorContainer;
    private View btnAddToCart;

    // Firebase
    private FirebaseFirestore db;

    // Data
    private String productId;
    private Product product;
    private String selectedColor = "";

    public ProductDetailFragment() {
    }

    public static ProductDetailFragment newInstance(String productId) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_product_detail, container, false);

        Log.d(TAG, "=== ProductDetailFragment onCreateView called ===");
        Log.d(TAG, "productId: " + productId);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Map Views
        initializeViews(view);

        // Setup listeners
        setupListeners();

        // Load product data
        loadProductData();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Hide bottom navigation menu
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
            Log.d(TAG, "✅ Bottom navigation hidden");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        btnFavorite = view.findViewById(R.id.btnFavorite);
        ivProductImage = view.findViewById(R.id.ivProductImage);
        tvProductName = view.findViewById(R.id.tvProductName);
        tvBrand = view.findViewById(R.id.tvBrand);
        tvPrice = view.findViewById(R.id.tvPrice);
        tvRating = view.findViewById(R.id.tvRating);
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        tvDescription = view.findViewById(R.id.tvDescription);
        tvChooseColor = view.findViewById(R.id.tvChooseColor);
        llColorContainer = view.findViewById(R.id.llColorContainer);
        btnAddToCart = view.findViewById(R.id.btnAddToCart);

        // Make button clickable
        if (btnAddToCart != null) {
            btnAddToCart.setClickable(true);
            btnAddToCart.setFocusable(true);
        }
    }

    private void setupListeners() {
        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "🔙 Back button clicked from ProductDetail");
            if (isAdded()) {
                // Show bottom navigation immediately
                View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
                if (bottomNav != null) {
                    bottomNav.setVisibility(View.VISIBLE);
                }

                requireActivity().getSupportFragmentManager().popBackStack();

                v.postDelayed(() -> {
                    if (isAdded()) {
                        View viewPager = requireActivity().findViewById(R.id.view_pager);
                        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

                        if (viewPager != null) viewPager.setVisibility(View.VISIBLE);
                        if (fragmentContainer != null) fragmentContainer.setVisibility(View.GONE);
                    }
                }, 100);
            }
        });

        // Favorite button
        btnFavorite.setOnClickListener(v -> {
            Log.d(TAG, "❤️ Favorite button clicked");
            Toast.makeText(requireContext(), "Added to favorites!", Toast.LENGTH_SHORT).show();
            // TODO: Implement add to favorites logic
        });

        // Add to cart button
        btnAddToCart.setOnClickListener(v -> {
            Log.d(TAG, "🛒 Add to cart clicked for product: " + productId);
            if (selectedColor.isEmpty()) {
                Toast.makeText(requireContext(), "Please choose a color", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(requireContext(), "Added to cart! Color: " + selectedColor, Toast.LENGTH_SHORT).show();
            // TODO: Implement add to cart logic
        });

        // Review count click
        tvReviewCount.setOnClickListener(v -> {
            Log.d(TAG, "📝 Review count clicked - navigating to ReviewFragment");
            if (product != null) {
                navigateToReviewFragment();
            }
        });
    }

    private void navigateToReviewFragment() {
        Log.d(TAG, "🔀 Navigating to ReviewFragment for product: " + productId);

        ReviewFragment reviewFragment = ReviewFragment.newInstance(
                productId,
                product.getProductName(),
                product.getRating(),
                product.getReviewCount()
        );

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, reviewFragment)
                .addToBackStack(null)
                .commit();
    }

    private void loadProductData() {
        Log.d(TAG, "🔍 Loading product data for productId: " + productId);

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        product = documentSnapshot.toObject(Product.class);
                        String productName = product != null ? product.getProductName() : null;
                        Log.d(TAG, "✅ Product loaded: " + (productName != null ? productName : "<unknown>"));
                        displayProductData();
                    } else {
                        Log.e(TAG, "❌ Product not found: " + productId);
                        Toast.makeText(requireContext(), "Product not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading product", e);
                    Toast.makeText(requireContext(), "Error loading product: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void displayProductData() {
        if (product == null || !isAdded()) return;

        Log.d(TAG, "📱 Displaying product data");

        // Product Name & Brand
        tvProductName.setText(product.getProductName());
        tvBrand.setText(product.getBrand());

        // Price
        String priceText = String.format(Locale.getDefault(), "$ %.2f", product.getFinalPrice());
        tvPrice.setText(priceText);

        // Description
        tvDescription.setText(product.getDescription());

        // Image
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(ivProductImage);
        }

        // Load actual ratings from reviews collection
        loadActualRatings();

        // Colors
        setupColorPicker();
    }

    private void loadActualRatings() {
        db.collection("reviews")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    List<Review> reviews = querySnapshot.toObjects(Review.class);
                    long reviewCount = reviews.size();

                    // Calculate average rating
                    double totalRating = 0;
                    long validRatingCount = 0;
                    for (Review review : reviews) {
                        if (review.getRating() != null) {
                            totalRating += review.getRating();
                            validRatingCount++;
                        }
                    }
                    double avgRating = validRatingCount > 0 ? totalRating / validRatingCount : 0;
                    avgRating = RatingFormatUtil.roundToTenth(avgRating);

                    Log.d(TAG, "✅ Actual ratings loaded: " + reviewCount + " reviews, avg: " + avgRating);

                    // Update UI with actual values
                    tvRating.setText(RatingFormatUtil.formatRatingWithSuffix(avgRating, "/5 "));
                    tvReviewCount.setText(String.format(Locale.getDefault(), "(%d reviews)", reviewCount));

                    // Update product object for ReviewFragment
                    product.setRating(avgRating);
                    product.setReviewCount(reviewCount);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading reviews", e);
                    // Fallback to product's stored values
                    tvRating.setText(RatingFormatUtil.formatRatingWithSuffix(product.getRating(), "/5 "));
                    tvReviewCount.setText(String.format(Locale.getDefault(), "(%d reviews)", product.getReviewCount()));
                });
    }

    private void setupColorPicker() {
        if (product.getColors() == null || product.getColors().isEmpty()) {
            tvChooseColor.setText("No colors available");
            return;
        }

        llColorContainer.removeAllViews();
        selectedColor = ""; // Reset selection

        for (String color : product.getColors()) {
            View colorOption = createColorOption(color);
            llColorContainer.addView(colorOption);
        }
    }

    private View createColorOption(String colorName) {
        View colorView = new View(requireContext());

        // 1. Tăng kích thước tổng thể lên một chút (từ 32 lên 40) để chứa viền
        int size = (int) (40 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(0, 0, (int) (10 * getResources().getDisplayMetrics().density), 0);
        colorView.setLayoutParams(params);

        // 2. QUAN TRỌNG: Thêm padding để viền không bị cắt
        int p = (int) (4 * getResources().getDisplayMetrics().density);
        colorView.setPadding(p, p, p, p);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        int colorCode = getColorCode(colorName);
        shape.setColor(colorCode);

        colorView.setBackground(shape);
        colorView.setTag(colorCode);

        colorView.setOnClickListener(v -> {
            selectedColor = colorName;
            for (int i = 0; i < llColorContainer.getChildCount(); i++) {
                updateColorViewBorder(llColorContainer.getChildAt(i), false);
            }
            updateColorViewBorder(v, true);
        });

        return colorView;
    }

    private void updateColorViewBorder(View colorView, boolean isSelected) {
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor((int) colorView.getTag());

        if (isSelected) {
            // Vẽ viền đen dày 3dp (~6-8px)
            shape.setStroke((int) (3 * getResources().getDisplayMetrics().density), Color.BLACK);
        } else {
            // Nếu là màu trắng thì giữ viền xám mờ, màu khác thì không viền
            int bgColor = (int) colorView.getTag();
            if (bgColor == Color.WHITE) {
                shape.setStroke(1, Color.LTGRAY);
            } else {
                shape.setStroke(0, Color.TRANSPARENT);
            }
        }
        colorView.setBackground(shape);
    }
    /**
     * Chuyển đổi tên màu thành mã hex color từ resources
     */
    private int getColorCode(String colorName) {
        if (colorName == null) {
            return requireContext().getColor(R.color.product_default_gray);
        }

        switch (colorName.toLowerCase().trim()) {
            case "black":
                return requireContext().getColor(R.color.product_black);
            case "white":
                return requireContext().getColor(R.color.product_white);
            case "gold":
                return requireContext().getColor(R.color.product_gold);
            case "silver":
                return requireContext().getColor(R.color.product_silver);
            case "space gray":
            case "space grey":
                return requireContext().getColor(R.color.product_space_gray);
            case "blue":
                return requireContext().getColor(R.color.product_blue);
            case "gray":
            case "grey":
                return requireContext().getColor(R.color.product_gray);
            case "green":
                return requireContext().getColor(R.color.product_green);
            case "pink":
                return requireContext().getColor(R.color.product_pink);
            case "midnight":
                return requireContext().getColor(R.color.product_midnight);
            case "natural titanium":
                return requireContext().getColor(R.color.product_natural_titanium);
            case "blue titanium":
                return requireContext().getColor(R.color.product_blue_titanium);
            case "white titanium":
                return requireContext().getColor(R.color.product_white_titanium);
            case "black titanium":
                return requireContext().getColor(R.color.product_black_titanium);
            case "platinum":
                return requireContext().getColor(R.color.product_platinum);
            case "yellow":
                return requireContext().getColor(R.color.product_yellow);
            case "deep purple":
                return requireContext().getColor(R.color.product_deep_purple);
            default:
                return requireContext().getColor(R.color.product_default_gray);
        }
    }
}











