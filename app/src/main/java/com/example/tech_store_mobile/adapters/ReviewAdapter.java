package com.example.tech_store_mobile.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Review;
import com.example.tech_store_mobile.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {
    private static final String TAG = "ReviewAdapter";
    private final List<Review> reviewList;

    public ReviewAdapter(List<Review> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        Log.d(TAG, "onBindViewHolder - position: " + position + ", reviewer: " + review.getUserName());

        // Set reviewer name
        holder.tvReviewerName.setText(review.getUserName());

        // Set rating stars
        int ratingInt = Math.round(review.getRating().floatValue());
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            if (i < ratingInt) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        holder.tvRatingStars.setText(stars.toString());

        // Set comment
        holder.tvComment.setText(review.getComment());

        // Set created time (relative time)
        if (review.getCreatedAt() != null) {
            String relativeTime = getRelativeTime(review.getCreatedAt().toDate().getTime());
            holder.tvCreatedAt.setText(relativeTime);
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    /**
     * Calculate relative time (e.g., "2 days ago")
     */
    private String getRelativeTime(long createdAtMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long diffMillis = currentTimeMillis - createdAtMillis;

        long diffSeconds = diffMillis / 1000;
        long diffMinutes = diffSeconds / 60;
        long diffHours = diffMinutes / 60;
        long diffDays = diffHours / 24;
        long diffWeeks = diffDays / 7;

        if (diffSeconds < 60) {
            return "Just now";
        } else if (diffMinutes < 60) {
            return diffMinutes + " minute" + (diffMinutes > 1 ? "s" : "") + " ago";
        } else if (diffHours < 24) {
            return diffHours + " hour" + (diffHours > 1 ? "s" : "") + " ago";
        } else if (diffDays < 7) {
            return diffDays + " day" + (diffDays > 1 ? "s" : "") + " ago";
        } else if (diffWeeks < 4) {
            return diffWeeks + " week" + (diffWeeks > 1 ? "s" : "") + " ago";
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            return sdf.format(createdAtMillis);
        }
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvRatingStars, tvReviewerName, tvComment, tvCreatedAt;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRatingStars = itemView.findViewById(R.id.tvRatingStars);
            tvReviewerName = itemView.findViewById(R.id.tvReviewerName);
            tvComment = itemView.findViewById(R.id.tvComment);
            tvCreatedAt = itemView.findViewById(R.id.tvCreatedAt);
        }
    }
}


