//package com.example.tech_store_mobile.utils;
//
//import android.util.Log;
//
//import com.example.tech_store_mobile.Model.Review;
//import com.google.firebase.firestore.DocumentSnapshot;
//import com.google.firebase.firestore.FirebaseFirestore;
//import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.firestore.SetOptions;
//import com.google.firebase.firestore.WriteBatch;
//
//import java.util.HashMap;
//import java.util.Locale;
//import java.util.Map;
//
///**
// * Temporary utility used to recalculate product rating and reviewCount from real reviews.
// * Run it once from MainActivity, verify the data, then remove the call.
// */
//public final class ProductReviewStatsMigrationUtil {
//    private static final String TAG = "ProductReviewStatsMig";
//    private static final int PRODUCT_COUNT = 24;
//
//    private ProductReviewStatsMigrationUtil() {
//        // Utility class
//    }
//
//    public interface Callback {
//        void onSuccess();
//
//        void onFailure(Exception e);
//    }
//
//    public static void updateAllProductReviewStats(FirebaseFirestore db, Callback callback) {
//        if (db == null) {
//            if (callback != null) {
//                callback.onFailure(new IllegalArgumentException("FirebaseFirestore must not be null"));
//            }
//            return;
//        }
//
//        db.collection("reviews")
//                .get()
//                .addOnSuccessListener(querySnapshot -> applyMigration(db, querySnapshot, callback))
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "Failed to load reviews", e);
//                    if (callback != null) {
//                        callback.onFailure(e);
//                    }
//                });
//    }
//
//    private static void applyMigration(FirebaseFirestore db,
//                                       QuerySnapshot querySnapshot,
//                                       Callback callback) {
//        Map<String, ReviewStats> statsByProductId = new HashMap<>();
//
//        if (querySnapshot != null) {
//            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                Review review = document.toObject(Review.class);
//                if (review == null || review.getProductId() == null) {
//                    continue;
//                }
//
//                String productId = review.getProductId().trim();
//                if (productId.isEmpty()) {
//                    continue;
//                }
//
//                ReviewStats stats = statsByProductId.computeIfAbsent(productId, key -> new ReviewStats());
//                stats.addReview(review);
//            }
//        }
//
//        WriteBatch batch = db.batch();
//        for (int index = 1; index <= PRODUCT_COUNT; index++) {
//            String productId = String.format(Locale.US, "prod_%03d", index);
//            ReviewStats stats = statsByProductId.get(productId);
//
//            Map<String, Object> updates = new HashMap<>();
//            updates.put("rating", stats != null ? stats.getAverageRating() : 0.0);
//            updates.put("reviewCount", stats != null ? stats.getReviewCount() : 0L);
//
//            batch.set(db.collection("products").document(productId), updates, SetOptions.merge());
//        }
//
//        batch.commit()
//                .addOnSuccessListener(unused -> {
//                    Log.d(TAG, "✅ Review stats migrated successfully for prod_001..prod_024");
//                    if (callback != null) {
//                        callback.onSuccess();
//                    }
//                })
//                .addOnFailureListener(e -> {
//                    Log.e(TAG, "❌ Failed to write migrated review stats", e);
//                    if (callback != null) {
//                        callback.onFailure(e);
//                    }
//                });
//    }
//
//    private static final class ReviewStats {
//        private long reviewCount = 0L;
//        private double ratingSum = 0.0;
//        private long ratedReviewCount = 0L;
//
//        void addReview(Review review) {
//            reviewCount++;
//            if (review.getRating() != null) {
//                ratingSum += review.getRating();
//                ratedReviewCount++;
//            }
//        }
//
//        long getReviewCount() {
//            return reviewCount;
//        }
//
//        double getAverageRating() {
//            return ratedReviewCount > 0 ? ratingSum / ratedReviewCount : 0.0;
//        }
//    }
//}
//
