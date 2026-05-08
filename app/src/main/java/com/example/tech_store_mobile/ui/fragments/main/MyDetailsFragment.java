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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.Model.User;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
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

        // Khóa ô Email không cho sửa (thường Email là cố định)
        etEmail.setEnabled(false);

        view.findViewById(R.id.btn_back_my_details).setOnClickListener(v -> handleBack());
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

        // Sử dụng Theme mặc định của hệ thống có hỗ trợ chọn năm nhanh
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year1, monthOfYear, dayOfMonth) -> {
                    String selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, (monthOfYear + 1), year1);
                    etDob.setText(selectedDate);
                }, year, month, day);

        // Dòng quan trọng: Giới hạn ngày chọn không vượt quá ngày hiện tại (nếu là ngày sinh)
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());

        datePickerDialog.show();
    }

    private void loadUserData() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (isAdded() && documentSnapshot.exists()) {
                        // FIX: Lấy Email trực tiếp từ Document nếu Object User bị lỗi
                        String email = documentSnapshot.getString("email");
                        etEmail.setText(safeString(email));

                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            etFullName.setText(safeString(user.getFullName()));
                            etDob.setText(safeString(user.getDateOfBirth()));

                            // Xử lý hiển thị SĐT khi load (cắt 0 để khớp với UI +84)
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

        // LOGIC YÊU CẦU: Cắt số 0 trên UI nhưng giữ cho Server
        String phoneForServer = inputPhone;

        if (inputPhone.startsWith("0")) {
            // Cập nhật UI: Biến mất số 0
            etPhone.setText(inputPhone.substring(1));
            // Giữ nguyên bản gốc (có số 0) để gửi lên Server
            phoneForServer = inputPhone;
        } else {
            // Nếu người dùng nhập không có số 0 (ví dụ 585...), Server vẫn cần có 0 ở đầu
            phoneForServer = "0" + inputPhone;
        }

        // Validate: Sau khi bỏ 0 phải còn 9 số (hoặc check tổng chiều dài)
        if (inputPhone.replace("0", "").length() < 8) {
            etPhone.setError("Invalid phone number length");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("dateOfBirth", dob);
        updates.put("gender", gender);
        updates.put("phoneNumber", phoneForServer); // Lưu vào Server bản có số 0

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
}