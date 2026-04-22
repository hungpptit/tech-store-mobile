package com.example.tech_store_mobile.utils;

import java.util.Locale;

/**
 * Helper for normalizing rating display to 1 decimal place.
 */
public final class RatingFormatUtil {
    private RatingFormatUtil() {
        // Utility class
    }

    public static double roundToTenth(Double value) {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return 0.0;
        }
        return Math.round(value * 10.0) / 10.0;
    }

    public static String formatRating(Double value) {
        return String.format(Locale.getDefault(), "%.1f", roundToTenth(value));
    }

    public static String formatRatingWithSuffix(Double value, String suffix) {
        return formatRating(value) + suffix;
    }
}

