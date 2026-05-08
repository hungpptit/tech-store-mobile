package com.example.tech_store_mobile.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.view.ViewCompat;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Helper to attach a small badge TextView to an ImageView at its top-end corner.
 * Returns the created TextView so callers can update text/visibility.
 */
public class NotificationBadgeUtils {
    private static Map<ImageView, TextView> attachedBadges = new WeakHashMap<>();

    public static TextView attachBadgeToImageView(ImageView imageView, Context context) {
        if (imageView == null || context == null) return null;

        ViewGroup parent = (ViewGroup) imageView.getParent();
        if (parent == null) return null;

        android.util.Log.d("NotificationBadgeUtils", "attachBadgeToImageView parent=" + parent.getClass().getSimpleName());

        // If already attached, return existing
        TextView existing = attachedBadges.get(imageView);
        if (existing != null) {
            return existing;
        }

        // Create badge
        TextView badge = new TextView(context);
        badge.setTextColor(Color.WHITE);
        badge.setTextSize(12);
        badge.setTypeface(Typeface.DEFAULT_BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(createBadgeBackground());
        badge.setVisibility(TextView.GONE);

        // Ensure unique id
        badge.setId(ViewCompat.generateViewId());

        // Position badge in a FrameLayout container over the ImageView
        FrameLayout container = new FrameLayout(context);
        ViewGroup.LayoutParams imgLp = imageView.getLayoutParams();
        container.setLayoutParams(imgLp);

        // Replace imageView in parent with container, keeping same index
        int index = parent.indexOfChild(imageView);
        parent.removeViewAt(index);
        container.addView(imageView);

        // Add badge layout params (top-end)
        FrameLayout.LayoutParams badgeLp = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.END | Gravity.TOP
        );
        int offset = dpToPx(context, -6); // nudge slightly
        badgeLp.setMargins(0, offset, offset, 0);
        container.addView(badge, badgeLp);

        parent.addView(container, index);

        // remember mapping to avoid double-attach
        attachedBadges.put(imageView, badge);

        android.util.Log.d("NotificationBadgeUtils", "badge attached, container index=" + index);

        return badge;
    }

    private static android.graphics.drawable.Drawable createBadgeBackground() {
        android.graphics.drawable.GradientDrawable gd = new android.graphics.drawable.GradientDrawable();
        gd.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        gd.setColor(Color.parseColor("#FF3B30")); // red
        gd.setSize(48, 48);
        return gd;
    }

    private static int dpToPx(Context ctx, int dp) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}



