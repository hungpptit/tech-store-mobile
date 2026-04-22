package com.example.tech_store_mobile.ui.fragments.main;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.Model.PaymentMethod;
import com.example.tech_store_mobile.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddCardFragment extends Fragment {

    private EditText etCardNumber, etExpiryDate, etSecurityCode;
    private CheckBox cbDefault;
    private AppCompatButton btnAddCard;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etCardNumber = view.findViewById(R.id.et_card_number);
        etExpiryDate = view.findViewById(R.id.et_expiry_date);
        etSecurityCode = view.findViewById(R.id.et_security_code);
        cbDefault = view.findViewById(R.id.cb_default_payment);
        btnAddCard = view.findViewById(R.id.btn_add_card_submit);

        setupValidation();

        view.findViewById(R.id.btn_back_add_card).setOnClickListener(v -> requireActivity().onBackPressed());

        btnAddCard.setOnClickListener(v -> saveCardToFirestore());

        return view;
    }

    private void setupValidation() {
        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                checkInputs();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etCardNumber.addTextChangedListener(watcher);
        etExpiryDate.addTextChangedListener(watcher);
        etSecurityCode.addTextChangedListener(watcher);

        btnAddCard.setEnabled(false);
    }

    private void checkInputs() {
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        String securityCode = etSecurityCode.getText().toString().trim();

        boolean isValid = cardNumber.length() >= 12 && !expiryDate.isEmpty() && securityCode.length() >= 3;

        btnAddCard.setEnabled(isValid);
        btnAddCard.setBackgroundTintList(ColorStateList.valueOf(isValid ? Color.BLACK : Color.parseColor("#CCCCCC")));
    }

    private void saveCardToFirestore() {
        String userId = "user_001"; 
        String cardNumber = etCardNumber.getText().toString().trim();
        String expiryDate = etExpiryDate.getText().toString().trim();
        boolean isDefault = cbDefault.isChecked();
        String cardId = UUID.randomUUID().toString();

        String cardType = cardNumber.startsWith("4") ? "VISA" : "MasterCard";

        PaymentMethod newCard = new PaymentMethod(cardId, userId, cardType, cardNumber, expiryDate, "Card Holder", isDefault);

        btnAddCard.setEnabled(false);
        btnAddCard.setText("Processing...");

        if (isDefault) {
            db.collection("payment_methods")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("isDefault", true)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        WriteBatch batch = db.batch();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            batch.update(doc.getReference(), "isDefault", false);
                        }
                        batch.set(db.collection("payment_methods").document(cardId), newCard);
                        
                        Map<String, Object> userData = new HashMap<>();
                        userData.put("defaultPaymentId", cardId);
                        batch.set(db.collection("users").document(userId), userData, SetOptions.merge());

                        batch.commit().addOnSuccessListener(aVoid -> showSuccessDialog());
                    });
        } else {
            db.collection("payment_methods").document(cardId).set(newCard)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> {
                        btnAddCard.setEnabled(true);
                        btnAddCard.setText("Add Card");
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void showSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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
