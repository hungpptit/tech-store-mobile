package com.example.tech_store_mobile.ui.fragments.main;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddAddressFragment extends Fragment {

    private Spinner spinnerNickname;
    private EditText etFullAddress;
    private CheckBox cbDefault;
    private AppCompatButton btnAdd;
    private FirebaseFirestore db;

    private String[] nicknames = {"Home", "Office", "Apartment", "Parent's House"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_address, container, false);

        db = FirebaseFirestore.getInstance();

        spinnerNickname = view.findViewById(R.id.spinner_nickname);
        etFullAddress = view.findViewById(R.id.et_full_address);
        cbDefault = view.findViewById(R.id.cb_default_address);
        btnAdd = view.findViewById(R.id.btn_add_address_submit);

        setupSpinner();
        setupTextWatcher();

        view.findViewById(R.id.btn_back_add_address).setOnClickListener(v -> requireActivity().onBackPressed());
        view.findViewById(R.id.btn_close_add_address).setOnClickListener(v -> requireActivity().onBackPressed());

        btnAdd.setOnClickListener(v -> saveAddress());

        return view;
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, nicknames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNickname.setAdapter(adapter);
    }

    private void setupTextWatcher() {
        etFullAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String address = s.toString().trim();
                boolean isValid = !address.isEmpty();
                btnAdd.setEnabled(isValid);
                btnAdd.setBackgroundTintList(ColorStateList.valueOf(isValid ? Color.BLACK : Color.parseColor("#CCCCCC")));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        btnAdd.setEnabled(false);
    }

    private void saveAddress() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAdd.setEnabled(false);

        String nickname = spinnerNickname.getSelectedItem().toString();
        String fullAddress = etFullAddress.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();
        String addressId = UUID.randomUUID().toString();

        Address newAddress = new Address(addressId, userId, nickname, fullAddress, isDefault);

        if (isDefault) {
            db.collection("addresses")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isDefault", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        WriteBatch batch = db.batch();

                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            batch.update(doc.getReference(), "isDefault", false);
                        }

                        batch.set(db.collection("addresses").document(addressId), newAddress);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("defaultAddressId", addressId);
                        batch.set(db.collection("users").document(userId), userData, SetOptions.merge());

                        batch.commit()
                                .addOnSuccessListener(aVoid -> showSuccessDialog())
                                .addOnFailureListener(e -> {
                                    btnAdd.setEnabled(true);
                                    Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    });
        } else {
            db.collection("addresses").document(addressId).set(newAddress)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> {
                        btnAdd.setEnabled(true);
                        Toast.makeText(getContext(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        }
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            // Quan trọng: Căn giữa Dialog
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.setCancelable(false);

        AppCompatButton btnThanks = dialog.findViewById(R.id.btn_thanks);
        btnThanks.setOnClickListener(v -> {
            dialog.dismiss();
            requireActivity().onBackPressed();
        });

        dialog.show();
    }
}
