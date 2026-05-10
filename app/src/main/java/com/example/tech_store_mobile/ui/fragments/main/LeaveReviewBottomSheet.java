package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.tech_store_mobile.Model.Review;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

public class LeaveReviewBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_PRODUCT_ID = "product_id";
    private static final String ARG_PRODUCT_NAME = "product_name";

    private String productId;
    private String productName;
    private RatingBar ratingBar;
    private EditText etComment;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static LeaveReviewBottomSheet newInstance(String productId, String productName) {
        LeaveReviewBottomSheet fragment = new LeaveReviewBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_PRODUCT_ID, productId);
        args.putString(ARG_PRODUCT_NAME, productName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            productId = getArguments().getString(ARG_PRODUCT_ID);
            productName = getArguments().getString(ARG_PRODUCT_NAME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_leave_review_bottom_sheet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ratingBar = view.findViewById(R.id.rating_bar_leave_review);
        etComment = view.findViewById(R.id.et_review_comment);
        MaterialButton btnSubmit = view.findViewById(R.id.btn_submit_review);
        ImageView btnClose = view.findViewById(R.id.btn_close_review);

        btnClose.setOnClickListener(v -> dismiss());

        btnSubmit.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        float rating = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();
        String userId = AuthManager.getCurrentUid();

        if (rating == 0) {
            Toast.makeText(getContext(), "Please provide a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userId == null) {
            Toast.makeText(getContext(), "Please login to submit a review", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview(userId, rating, comment);
    }

    private void btnSubmitReview(String userId, float rating, String comment) {
        String reviewId = db.collection("reviews").document().getId();
        
        // We might want to fetch user name here, but for now we use a placeholder or check AuthManager
        String userName = "User"; // Should ideally be current user's name

        db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String name = documentSnapshot.getString("fullName");
            String finalName = TextUtils.isEmpty(name) ? "Anonymous" : name;

            Review review = new Review(
                    reviewId,
                    productId,
                    userId,
                    finalName,
                    (double) rating,
                    comment,
                    Timestamp.now()
            );

            db.collection("reviews").document(reviewId).set(review)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Review submitted successfully!", Toast.LENGTH_SHORT).show();
                        dismiss();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to submit review", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}
