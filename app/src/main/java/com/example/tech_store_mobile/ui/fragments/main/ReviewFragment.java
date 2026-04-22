package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.FrameLayout;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Review;
import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.ReviewAdapter;
import com.example.tech_store_mobile.utils.RatingFormatUtil;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * ReviewFragment - Hiển thị danh sách đánh giá của sản phẩm
 *
 * Arguments:
 * - productId: ID của sản phẩm
 * - productName: Tên sản phẩm
 * - rating: Điểm rating trung bình
 * - reviewCount: Tổng số đánh giá
 */
public class ReviewFragment extends Fragment {
    private static final String TAG = "ReviewFragment";
    private static final String ARG_PRODUCT_ID = "productId";
    private static final String ARG_PRODUCT_NAME = "productName";
    private static final String ARG_RATING = "rating";
    private static final String ARG_REVIEW_COUNT = "reviewCount";

    // Views
    private ImageView btnBack;
    private TextView tvTitle, tvRating, tvReviewCount, tvReviewStats;
    private AutoCompleteTextView spnReviewFilter;
    private RecyclerView rvReviews;
    private LinearLayout llRatingBreakdown;

    // Firebase
    private FirebaseFirestore db;

    // Data
    private String productId, productName;
    private Double rating;
    private Long reviewCount;
    private final List<Review> allReviewList = new ArrayList<>();
    private final List<Review> reviewList = new ArrayList<>();
    private ReviewAdapter reviewAdapter;
    private int selectedFilterPosition = 0;

    public ReviewFragment() {
    }

    public static ReviewFragment newInstance(String productId, String productName, Double rating, Long reviewCount) {
        ReviewFragment fragment = new ReviewFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, productName);
        args.putDouble(ARG_RATING, RatingFormatUtil.roundToTenth(rating));
        args.putLong(ARG_REVIEW_COUNT, reviewCount != null ? reviewCount : 0L);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
            productName = getArguments().getString(ARG_PRODUCT_NAME);
            rating = RatingFormatUtil.roundToTenth(getArguments().getDouble(ARG_RATING));
            reviewCount = getArguments().getLong(ARG_REVIEW_COUNT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        Log.d(TAG, "=== ReviewFragment onCreateView called ===");
        Log.d(TAG, "productId: " + productId);
        Log.d(TAG, "productName: " + productName);
        Log.d(TAG, "rating: " + rating);
        Log.d(TAG, "reviewCount: " + reviewCount);

        // Init Firebase
        db = FirebaseFirestore.getInstance();

        // Map Views
        initializeViews(view);

        // Setup RecyclerView
        setupRecyclerView();

        // Load reviews
        loadReviews();

        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "🔙 Back button clicked");
            if (isAdded()) {
                requireActivity().getSupportFragmentManager().popBackStack();
                v.postDelayed(() -> {
                    if (isAdded() && requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).syncBottomNavigationVisibility();
                    }
                }, 100);
            }
        });

        return view;
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        tvTitle = view.findViewById(R.id.tvTitle);
        tvRating = view.findViewById(R.id.tvRating);
        tvReviewCount = view.findViewById(R.id.tvReviewCount);
        tvReviewStats = view.findViewById(R.id.tvReviewStats);
        spnReviewFilter = view.findViewById(R.id.spnReviewFilter);
        rvReviews = view.findViewById(R.id.rvReviews);
        llRatingBreakdown = view.findViewById(R.id.llRatingBreakdown);

        // Set title
        tvTitle.setText(R.string.reviews_title);

        setupReviewFilter();
    }

    private void setupReviewFilter() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.review_filter_options,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(R.layout.item_review_filter_dropdown);
        spnReviewFilter.setAdapter(adapter);
        spnReviewFilter.setText(adapter.getItem(0), false);
        spnReviewFilter.setOnItemClickListener((parent, view, position, id) -> {
            selectedFilterPosition = position;
            applyReviewFilter();
        });
    }

    private void setupRecyclerView() {
        Log.d(TAG, "🔧 setupRecyclerView called");

        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setNestedScrollingEnabled(false);
        rvReviews.setHasFixedSize(false);
        reviewAdapter = new ReviewAdapter(reviewList);
        rvReviews.setAdapter(reviewAdapter);

        Log.d(TAG, "   RecyclerView setup complete");
    }

    private void loadReviews() {
        Log.d(TAG, "🔍 Loading reviews for productId: " + productId);

        db.collection("reviews")
                .whereEqualTo("productId", productId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Review> reviews = querySnapshot.toObjects(Review.class);
                    allReviewList.clear();
                    allReviewList.addAll(reviews);
                    long commentCount = countComments(reviews);

                    Log.d(TAG, "✅ Reviews loaded: " + reviews.size());

                    // Display rating info
                    tvRating.setText(RatingFormatUtil.formatRating(rating));
                    tvReviewCount.setText(getResources().getQuantityString(
                            R.plurals.review_count,
                            reviews.size(),
                            reviews.size()));
                    tvReviewStats.setText(getString(
                            R.string.review_comment_summary_format,
                            reviews.size(),
                            commentCount));

                    // Build rating breakdown
                    buildRatingBreakdown(reviews);

                    // Update adapter with current filter
                    applyReviewFilter();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Error loading reviews", e);
                    Toast.makeText(getContext(), "Error loading reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    long safeReviewCount = reviewCount != null ? reviewCount : 0L;
                    tvReviewCount.setText(getResources().getQuantityString(
                            R.plurals.review_count,
                            (int) safeReviewCount,
                            (int) safeReviewCount));
                    tvReviewStats.setText(getString(
                            R.string.review_comment_summary_format,
                            safeReviewCount,
                            0L));
                    allReviewList.clear();
                    applyReviewFilter();
                });
    }

    private void applyReviewFilter() {
        reviewList.clear();

        for (Review review : allReviewList) {
            if (matchesCurrentFilter(review)) {
                reviewList.add(review);
            }
        }

        if (reviewAdapter != null) {
            reviewAdapter.notifyDataSetChanged();
        }
    }

    private boolean matchesCurrentFilter(Review review) {
        if (review == null) {
            return false;
        }

        switch (selectedFilterPosition) {
            case 1:
                return review.getComment() != null && !review.getComment().trim().isEmpty();
            case 2:
                return hasExactStar(review, 5);
            case 3:
                return hasExactStar(review, 4);
            case 4:
                return hasExactStar(review, 3);
            case 5:
                return hasExactStar(review, 2);
            case 6:
                return hasExactStar(review, 1);
            case 0:
            default:
                return true;
        }
    }

    private boolean hasExactStar(Review review, int stars) {
        return review.getRating() != null && Math.round(review.getRating().floatValue()) == stars;
    }

    private long countComments(List<Review> reviews) {
        long count = 0L;
        for (Review review : reviews) {
            if (review != null && review.getComment() != null && !review.getComment().trim().isEmpty()) {
                count++;
            }
        }
        return count;
    }

    private void buildRatingBreakdown(List<Review> reviews) {
        // Count reviews by rating
        int[] ratingCounts = new int[6]; // 0-5 stars
        for (Review review : reviews) {
            if (review.getRating() == null) {
                continue;
            }

            int star = Math.round(review.getRating().floatValue());
            if (star >= 1 && star <= 5) {
                ratingCounts[star]++;
            }
        }

        llRatingBreakdown.removeAllViews();

        // Display rating bars for 5 down to 1
        for (int star = 5; star >= 1; star--) {
            View ratingBar = createRatingBar(star, ratingCounts[star], reviews.size());
            llRatingBreakdown.addView(ratingBar);
        }
    }

    private View createRatingBar(int stars, int count, int total) {
        View ratingBarView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_rating_bar, llRatingBreakdown, false);

        // Set stars (★ for filled, ☆ for empty)
        TextView tvStars = ratingBarView.findViewById(R.id.tvStarRating);
        StringBuilder starStr = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            starStr.append(i < stars ? "★" : "☆");
        }
        tvStars.setText(starStr.toString());
        tvStars.setTextColor(0xFFFFC107); // Gold color

        // Set progress bar width based on percentage
        View progressBar = ratingBarView.findViewById(R.id.progressBar);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) progressBar.getLayoutParams();

        // Calculate percentage (0-100%)
        float percentage = total > 0 ? (count * 100f) / total : 0f;

        // Set width as percentage of parent
        params.width = (int) (percentage * 2.56); // Approximate conversion for percentage width
        progressBar.setLayoutParams(params);

        // Set count
        TextView tvCount = ratingBarView.findViewById(R.id.tvCount);
        tvCount.setText(String.valueOf(count));

        return ratingBarView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        hideBottomNavigation();
    }

    @Override
    public void onResume() {
        super.onResume();
        hideBottomNavigation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private void hideBottomNavigation() {
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.GONE);
            Log.d(TAG, "✅ Bottom navigation hidden");
        }
    }
}




