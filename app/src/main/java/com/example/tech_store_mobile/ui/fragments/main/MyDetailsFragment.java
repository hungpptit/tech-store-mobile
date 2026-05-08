package com.example.tech_store_mobile.ui.fragments.main;

import android.app.DatePickerDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tech_store_mobile.Model.User;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MyDetailsFragment extends Fragment {

    private EditText etFullName, etEmail, etDob, etPhone;
    private Spinner spinnerGender;
    private AppCompatButton btnSubmit;
    private FirebaseFirestore db;
    private String userId;
    private TextView notificationBadgeView;
    private NotificationBadgeManager.BadgeListener badgeListener;

    private final String[] genders = {"Male", "Female", "Other"};

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
        btnSubmit = view.findViewById(R.id.btn_submit_my_details);

        // Khóa ô Email không cho sửa
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
        btnSubmit.setOnClickListener(v -> updateProfile());
    }

    private void showDatePicker() {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
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

                            String phoneFromDb = user.getPhoneNumber();
                            if (phoneFromDb != null && phoneFromDb.startsWith("0")) {
                                etPhone.setText(phoneFromDb.substring(1));
                            } else {
                                etPhone.setText(safeString(phoneFromDb));
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

    private void updateProfile() {
        String fullName = etFullName.getText().toString().trim();
        String dob = etDob.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String inputPhone = etPhone.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Please enter your name");
            return;
        }

        String phoneForServer = inputPhone.startsWith("0") ? inputPhone : "0" + inputPhone;
        if (inputPhone.replace("0", "").length() < 8) {
            etPhone.setError("Invalid phone number");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("dateOfBirth", dob);
        updates.put("gender", gender);
        updates.put("phoneNumber", phoneForServer);

        btnSubmit.setEnabled(false);
        btnSubmit.setText("Updating...");

        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                        btnSubmit.setEnabled(true);
                        btnSubmit.setText("Submit");
                    }
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
