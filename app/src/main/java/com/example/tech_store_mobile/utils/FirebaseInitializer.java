package com.example.tech_store_mobile.utils;

import android.util.Log;

import com.example.tech_store_mobile.Model.Category;
import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.Model.Review;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * FirebaseInitializer - Tạo sample data lên Firebase
 * Chạy lần đầu khi app khởi động
 */
public class FirebaseInitializer {
    private static final String TAG = "FirebaseInitializer";
    private static FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static void initializeAllData() {
        Log.d(TAG, "🚀 Starting Firebase data initialization...");

        initializeCategories();
        initializeProducts();
        initializeReviews();
    }

    // ==================== CATEGORIES ====================
    private static void initializeCategories() {
        List<Category> categories = new ArrayList<>();
        categories.add(new Category("cat_001", "Smartphone", "", 1L));
        categories.add(new Category("cat_002", "Laptop", "", 2L));
        categories.add(new Category("cat_003", "Watch", "", 3L));
        categories.add(new Category("cat_004", "Screen", "", 4L));

        for (Category cat : categories) {
            db.collection("categories")
                    .document(cat.getCategoryId())
                    .set(cat)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Category added: " + cat.getCategoryName());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error adding category", e);
                    });
        }
    }

    // ==================== PRODUCTS ====================
    private static void initializeProducts() {
        List<Product> products = new ArrayList<>();

        // Smartphone products
        products.add(new Product(
                "prod_001", "cat_001", "iPhone 15 Pro", "Apple",
                "Flagship Apple smartphone with A17 Pro chip",
                1099.99, 10.0, 989.99,
                new ArrayList<>(Arrays.asList("Black", "Gold", "Silver")),
                "", 4.8, 250L, 50L, true, true,
                new ArrayList<>(Arrays.asList("phone", "apple", "smartphone"))
        ));

        products.add(new Product(
                "prod_002", "cat_001", "Samsung Galaxy S24", "Samsung",
                "Latest Samsung flagship with advanced camera",
                899.99, 15.0, 764.99,
                new ArrayList<>(Arrays.asList("Blue", "Gray", "White")),
                "", 4.6, 180L, 60L, true, true,
                new ArrayList<>(Arrays.asList("phone", "samsung", "android"))
        ));

        products.add(new Product(
                "prod_003", "cat_001", "Google Pixel 8", "Google",
                "Google's flagship with AI-powered camera",
                799.99, 20.0, 639.99,
                new ArrayList<>(Arrays.asList("Black", "Green")),
                "", 4.5, 120L, 40L, true, false,
                new ArrayList<>(Arrays.asList("phone", "google", "pixel"))
        ));

        // Laptop products
        products.add(new Product(
                "prod_004", "cat_002", "MacBook Pro 14", "Apple",
                "Powerful laptop with M3 Max processor",
                1999.99, 5.0, 1899.99,
                new ArrayList<>(Arrays.asList("Silver", "Space Gray")),
                "", 4.9, 300L, 30L, false, true,
                new ArrayList<>(Arrays.asList("laptop", "apple", "macbook"))
        ));

        products.add(new Product(
                "prod_005", "cat_002", "Dell XPS 15", "Dell",
                "Premium Windows laptop with great display",
                1499.99, 12.0, 1319.99,
                new ArrayList<>(Arrays.asList("Silver", "Platinum")),
                "", 4.7, 200L, 25L, false, true,
                new ArrayList<>(Arrays.asList("laptop", "dell", "windows"))
        ));

        products.add(new Product(
                "prod_006", "cat_002", "ThinkPad X1 Carbon", "Lenovo",
                "Business laptop with excellent keyboard",
                1299.99, 10.0, 1169.99,
                new ArrayList<>(Arrays.asList("Black")),
                "", 4.6, 150L, 35L, false, false,
                new ArrayList<>(Arrays.asList("laptop", "lenovo", "business"))
        ));

        // Watch products
        products.add(new Product(
                "prod_007", "cat_003", "Apple Watch Series 9", "Apple",
                "Latest Apple smartwatch with health features",
                429.99, 16.0, 359.99,
                new ArrayList<>(Arrays.asList("Silver", "Gold", "Space Gray")),
                "", 4.8, 280L, 40L, false, true,
                new ArrayList<>(Arrays.asList("watch", "apple", "smartwatch"))
        ));

        products.add(new Product(
                "prod_008", "cat_003", "Samsung Galaxy Watch 6", "Samsung",
                "Android smartwatch with AMOLED display",
                329.99, 22.0, 254.99,
                new ArrayList<>(Arrays.asList("Silver", "Black")),
                "", 4.6, 200L, 50L, false, true,
                new ArrayList<>(Arrays.asList("watch", "samsung", "smartwatch"))
        ));

        products.add(new Product(
                "prod_009", "cat_003", "Garmin Forerunner 955", "Garmin",
                "Advanced sports watch with GPS",
                599.99, 23.0, 459.99,
                new ArrayList<>(Arrays.asList("Black", "Gray")),
                "", 4.7, 150L, 20L, false, false,
                new ArrayList<>(Arrays.asList("watch", "garmin", "sports"))
        ));

        for (Product prod : products) {
            db.collection("products")
                    .document(prod.getProductId())
                    .set(prod)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Product added: " + prod.getProductName());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error adding product", e);
                    });
        }
    }

    // ==================== REVIEWS ====================
    private static void initializeReviews() {
        List<Review> reviews = new ArrayList<>();

        reviews.add(new Review("rev_001", "prod_001", "user_001", "John Doe", 5.0, "Best phone ever!", Timestamp.now()));
        reviews.add(new Review("rev_002", "prod_001", "user_002", "Jane Smith", 4.0, "Great camera quality", Timestamp.now()));
        reviews.add(new Review("rev_003", "prod_001", "user_003", "Mike Johnson", 5.0, "Excellent performance", Timestamp.now()));

        reviews.add(new Review("rev_004", "prod_002", "user_004", "Sarah Williams", 4.0, "Good value for money", Timestamp.now()));
        reviews.add(new Review("rev_005", "prod_002", "user_005", "Tom Brown", 4.0, "Very good display", Timestamp.now()));

        reviews.add(new Review("rev_006", "prod_004", "user_006", "Emily Davis", 5.0, "Perfect for work", Timestamp.now()));
        reviews.add(new Review("rev_007", "prod_004", "user_007", "David Wilson", 5.0, "Powerful machine", Timestamp.now()));

        reviews.add(new Review("rev_008", "prod_007", "user_008", "Lisa Anderson", 5.0, "Great companion", Timestamp.now()));

        for (Review review : reviews) {
            db.collection("reviews")
                    .document(review.getReviewId())
                    .set(review)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Review added: " + review.getReviewId());
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Error adding review", e);
                    });
        }
    }
}





