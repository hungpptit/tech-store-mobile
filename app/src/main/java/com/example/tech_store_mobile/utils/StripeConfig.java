package com.example.tech_store_mobile.utils;

import com.example.tech_store_mobile.BuildConfig;

public final class StripeConfig {
    private StripeConfig() {
    }

    public static String STRIPE_PUBLISHABLE_KEY = BuildConfig.STRIPE_PUBLISHABLE_KEY;
    public static String BACKEND_BASE_URL = BuildConfig.STRIPE_BACKEND_BASE_URL;
    public static String CREATE_PAYMENT_INTENT_ENDPOINT = "/api/payments/create-payment-intent";
    public static String CURRENCY_USD = "USD";

    public static boolean isConfigured() {
        return !isPlaceholder(STRIPE_PUBLISHABLE_KEY)
                && !isPlaceholder(BACKEND_BASE_URL);
    }

    private static boolean isPlaceholder(String value) {
        return value == null
                || value.trim().isEmpty()
                || value.contains("YOUR_PUBLISHABLE_KEY")
                || value.contains("your-backend.example.com");
    }
}





