package com.example.tech_store_mobile.ui.fragments.main;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.example.tech_store_mobile.Model.User;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MyDetailsFragment extends Fragment {

    private EditText etFullName, etEmail, etDob, etPhone;
    private Spinner spinnerGender;
    private ShapeableImageView ivAvatar;
    private View btnEditAvatar;
    private AppCompatButton btnSubmit;
    private FirebaseFirestore db;
    private String userId;
    private TextView notificationBadgeView;
    private NotificationBadgeManager.BadgeListener badgeListener;
    private Uri selectedImageUri;

    private final String[] genders = {"Male", "Female", "Other"};

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivAvatar.setImageURI(uri);
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userId = AuthManager.getCurrentUid();

        initViews(view);
        setupGenderSpinner();
        loadUserData();
        setupListeners();
    }

    private void initViews(View view) {
        etFullName = view.findViewById(R.id.et_full_name);
        etEmail = view.findViewById(R.id.et_email);
        etDob = view.findViewById(R.id.et_dob);
        etPhone = view.findViewById(R.id.et_phone);
        spinnerGender = view.findViewById(R.id.spinner_gender);
        ivAvatar = view.findViewById(R.id.iv_user_avatar);
        btnEditAvatar = view.findViewById(R.id.btn_edit_avatar);
        btnSubmit = view.findViewById(R.id.btn_submit_my_details);

        // Khóa ô Email không cho sửa
        // Email is usually read-only
        etEmail.setEnabled(false);

        view.findViewById(R.id.btn_back_my_details).setOnClickListener(v -> handleBack());

        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }
    }

    private void handleBack() {
        if (getActivity() != null) {
            View viewPager = getActivity().findViewById(R.id.view_pager);
            View bottomNav = getActivity().findViewById(R.id.bottom_navigation);
            View container = getActivity().findViewById(R.id.fragment_container);

            if (viewPager != null) viewPager.setVisibility(View.VISIBLE);
            if (bottomNav != null) bottomNav.setVisibility(View.VISIBLE);
            if (container != null) container.setVisibility(View.GONE);

            getParentFragmentManager().popBackStack();
        }
    }

    private void setupGenderSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void setupListeners() {
        etDob.setOnClickListener(v -> showDatePicker());
        ivAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnEditAvatar.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSubmit.setOnClickListener(v -> attemptUpdateProfile());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.US, "%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                    etDob.setText(selectedDate);
                }, year, month, day);

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        String email = documentSnapshot.getString("email");
                        etEmail.setText(safeString(email));

                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            etFullName.setText(safeString(user.getFullName()));
                            etDob.setText(safeString(user.getDateOfBirth()));

                            // Load Avatar from Cloudinary URL stored in avatarUrl
                            if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                                Glide.with(this)
                                        .load(user.getAvatarUrl())
                                        .placeholder(R.drawable.user)
                                        .circleCrop()
                                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                                        .into(ivAvatar);
                            }

                            // Phone Number handling
                            String phoneFromDb = user.getPhoneNumber();
                            if (phoneFromDb != null && !phoneFromDb.isEmpty()) {
                                if (phoneFromDb.startsWith("+84")) {
                                    etPhone.setText(phoneFromDb.substring(3));
                                } else if (phoneFromDb.startsWith("0")) {
                                    etPhone.setText(phoneFromDb.substring(1));
                                } else {
                                    etPhone.setText(phoneFromDb);
                                }
                            }

                            String userGender = user.getGender();
                            if (userGender != null && !userGender.isEmpty()) {
                                for (int i = 0; i < genders.length; i++) {
                                    if (genders[i].equalsIgnoreCase(userGender)) {
                                        spinnerGender.setSelection(i);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private String safeString(String value) {
        return (value == null) ? "" : value;
    }

    private void attemptUpdateProfile() {
        String fullName = etFullName.getText().toString().trim();
        if (fullName.isEmpty()) {
            etFullName.setError("Please enter your name");
            return;
        }

        String phoneForServer = inputPhone.startsWith("0") ? inputPhone : "0" + inputPhone;
        if (inputPhone.replace("0", "").length() < 8) {
            etPhone.setError("Invalid phone number");
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Updating...");

        if (selectedImageUri != null) {
            uploadImageToCloudinary(selectedImageUri);
        } else {
            saveProfileToFirestore(null);
        }
    }

    private void uploadImageToCloudinary(Uri uri) {
        Log.d("Cloudinary", "📤 Uploading image to Cloudinary...");
        
        MediaManager.get().upload(uri)
                .option("folder", "user_avatars")
                .option("public_id", userId) // Overwrites previous avatar for this user
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d("Cloudinary", "Upload started");
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d("Cloudinary", "✅ Upload success: " + imageUrl);
                        saveProfileToFirestore(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e("Cloudinary", "❌ Upload error: " + error.getDescription());
                        if (isAdded()) {
                            Toast.makeText(getContext(), "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                            btnSubmit.setEnabled(true);
                            btnSubmit.setText("Submit");
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                    }
                }).dispatch();
    }

    private void saveProfileToFirestore(@Nullable String avatarUrl) {
        String dob = etDob.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String inputPhone = etPhone.getText().toString().trim();

        String phoneDigits = inputPhone;
        if (phoneDigits.startsWith("0")) {
            phoneDigits = phoneDigits.substring(1);
        }

        // Basic validation: 9 digits after +84
        if (!phoneDigits.isEmpty() && !phoneDigits.matches("\\d{9}")) {
            etPhone.setError("Phone must have 9 digits");
            btnSubmit.setEnabled(true);
            btnSubmit.setText("Submit");
            return;
        }

        String phoneForServer = phoneDigits.isEmpty() ? "" : "+84" + phoneDigits;

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", etFullName.getText().toString().trim());
        updates.put("dateOfBirth", dob);
        updates.put("gender", gender);
        updates.put("phoneNumber", phoneForServer);

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Updating...");
        if (avatarUrl != null) {
            updates.put("avatarUrl", avatarUrl);
        }

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Log.d("MyDetailsFragment", "✅ Firestore updated successfully");
                        Toast.makeText(getContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                        selectedImageUri = null;
                        
                        // Force refresh image with Glide if URL changed
                        if (avatarUrl != null) {
                            Glide.with(this).load(avatarUrl)
                                    .skipMemoryCache(true)
                                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                                    .circleCrop()
                                    .into(ivAvatar);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Log.e("MyDetailsFragment", "❌ Firestore update failed: " + e.getMessage());
                        Toast.makeText(getContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        startNotificationBadgeListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNotificationBadgeListener();
    }

    private void startNotificationBadgeListener() {
        badgeListener = unreadCount -> {
            if (notificationBadgeView == null) return;
            if (unreadCount <= 0) {
                notificationBadgeView.setVisibility(View.GONE);
            } else {
                notificationBadgeView.setVisibility(View.VISIBLE);
                notificationBadgeView.setText(unreadCount > 99 ? "99+" : String.valueOf(unreadCount));
            }
        };
        NotificationBadgeManager.getInstance().addListener(badgeListener);
        NotificationBadgeManager.getInstance().start();
    }

    private void stopNotificationBadgeListener() {
        if (badgeListener != null) {
            NotificationBadgeManager.getInstance().removeListener(badgeListener);
        }
        NotificationBadgeManager.getInstance().stop();
    }

    private void navigateToNotifications() {
        if (!isAdded() || getActivity() == null) return;
        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);
        if (bottomNav != null) bottomNav.setVisibility(View.GONE);

        NotificationsFragment fragment = new NotificationsFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
