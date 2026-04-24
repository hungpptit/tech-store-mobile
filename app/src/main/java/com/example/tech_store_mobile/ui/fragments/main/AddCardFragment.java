package com.example.tech_store_mobile.ui.fragments.main;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
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
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.StripeConfig;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;
import com.stripe.android.ApiResultCallback;
import com.stripe.android.Stripe;
import com.stripe.android.model.CardParams;
import com.stripe.android.model.Token;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddCardFragment extends Fragment {
    private static final String TAG = "AddCardFragment";

    private EditText etCardNumber, etExpiryDate, etSecurityCode;
    private CheckBox cbDefault;
    private AppCompatButton btnAddCard;
    private FirebaseFirestore db;
    private Stripe stripe;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_card, container, false);

        db = FirebaseFirestore.getInstance();
        stripe = isStripeReady()
                ? new Stripe(requireContext().getApplicationContext(), StripeConfig.STRIPE_PUBLISHABLE_KEY)
                : null;

        etCardNumber = view.findViewById(R.id.et_card_number);
        etExpiryDate = view.findViewById(R.id.et_expiry_date);
        etSecurityCode = view.findViewById(R.id.et_security_code);
        cbDefault = view.findViewById(R.id.cb_default_payment);
        btnAddCard = view.findViewById(R.id.btn_add_card_submit);

        setupValidation();

        view.findViewById(R.id.btn_back_add_card).setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

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
        String cardNumber = normalizeDigits(etCardNumber.getText().toString().trim());
        String expiryDate = etExpiryDate.getText().toString().trim();
        String securityCode = normalizeDigits(etSecurityCode.getText().toString().trim());

        boolean isValid = isValidCardNumber(cardNumber) && isValidExpiryDate(expiryDate) && isValidSecurityCode(securityCode);

        btnAddCard.setEnabled(isValid);
        btnAddCard.setBackgroundTintList(ColorStateList.valueOf(isValid ? Color.BLACK : Color.parseColor("#CCCCCC")));
    }

    private void saveCardToFirestore() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isStripeReady()) {
            Toast.makeText(getContext(), "Stripe chưa được cấu hình. Hãy điền publishable key test.", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardNumber = normalizeDigits(etCardNumber.getText().toString().trim());
        String expiryDate = etExpiryDate.getText().toString().trim();
        String securityCode = normalizeDigits(etSecurityCode.getText().toString().trim());
        boolean isDefault = cbDefault.isChecked();

        ExpiryParts expiryParts = parseExpiryDate(expiryDate);
        if (expiryParts == null) {
            Toast.makeText(getContext(), "Định dạng ngày hết hạn phải là MM/YY hoặc MM/YYYY", Toast.LENGTH_SHORT).show();
            return;
        }

        String cardHolderName = resolveCardHolderName();

        btnAddCard.setEnabled(false);
        btnAddCard.setText(R.string.add_card_processing);

        CardParams cardParams = new CardParams(cardNumber, expiryParts.month, expiryParts.year, securityCode, cardHolderName);

        stripe.createCardToken(cardParams, new ApiResultCallback<>() {
            @Override
            public void onSuccess(@NonNull Token token) {
                if (!isAdded()) {
                    return;
                }

                persistPaymentMethod(userId, token, cardHolderName, expiryDate, isDefault);
            }

            @Override
            public void onError(@NonNull Exception e) {
                if (!isAdded()) {
                    return;
                }

                restoreAddCardButton();
                String message = e.getMessage() != null ? e.getMessage() : "Không thể tạo payment method.";
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void persistPaymentMethod(String userId, Token token, String cardHolderName,
                                      String expiryDate, boolean isDefault) {
        String paymentId = token.getId();
        com.stripe.android.model.Card stripeCard = token.getCard();
        String brand = stripeCard != null
                ? stripeCard.getBrand().getDisplayName().toUpperCase(Locale.US)
                : detectCardBrand(etCardNumber.getText().toString().trim());
        String last4 = stripeCard != null ? stripeCard.getLast4() : cardNumberLast4(etCardNumber.getText().toString().trim());
        String maskedCardNumber = buildMaskedCardNumber(brand, last4);
        String finalExpiryDate = stripeCard != null && stripeCard.getExpMonth() != null && stripeCard.getExpYear() != null
                ? String.format(Locale.US, "%02d/%04d", stripeCard.getExpMonth(), stripeCard.getExpYear())
                : expiryDate;

        PaymentMethod newCard = new PaymentMethod(paymentId, userId, brand, maskedCardNumber, finalExpiryDate, cardHolderName, isDefault);

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

                        batch.set(db.collection("payment_methods").document(paymentId), newCard);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("defaultPaymentId", paymentId);
                        batch.set(db.collection("users").document(userId), userData, SetOptions.merge());

                        batch.commit()
                                .addOnSuccessListener(aVoid -> showSuccessDialog())
                                .addOnFailureListener(e -> {
                                    restoreAddCardButton();
                                    Log.e(TAG, "Failed to save default payment method", e);
                                    Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .addOnFailureListener(e -> {
                        restoreAddCardButton();
                        Log.e(TAG, "Failed to query current default payment method", e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("payment_methods").document(paymentId).set(newCard)
                    .addOnSuccessListener(aVoid -> showSuccessDialog())
                    .addOnFailureListener(e -> {
                        restoreAddCardButton();
                        Log.e(TAG, "Failed to save payment method", e);
                        Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void restoreAddCardButton() {
        btnAddCard.setEnabled(true);
        btnAddCard.setText(R.string.add_card_button);
    }

    private boolean isValidCardNumber(String cardNumber) {
        String normalized = normalizeDigits(cardNumber);
        return normalized.matches("\\d{12,19}");
    }

    private boolean isValidExpiryDate(String expiryDate) {
        return parseExpiryDate(expiryDate) != null;
    }

    private boolean isValidSecurityCode(String securityCode) {
        return normalizeDigits(securityCode).matches("\\d{3,4}");
    }

    private String normalizeDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D+", "");
    }

    private ExpiryParts parseExpiryDate(String expiryDate) {
        if (TextUtils.isEmpty(expiryDate)) {
            return null;
        }

        String[] parts = expiryDate.trim().split("/");
        if (parts.length != 2) {
            return null;
        }

        try {
            int month = Integer.parseInt(parts[0].trim());
            int year = Integer.parseInt(parts[1].trim());
            if (parts[1].trim().length() == 2) {
                year += 2000;
            }
            if (month < 1 || month > 12 || year < 2000) {
                return null;
            }
            return new ExpiryParts(month, year);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String detectCardBrand(String cardNumber) {
        String normalized = normalizeDigits(cardNumber);
        if (normalized.startsWith("4")) {
            return "VISA";
        }
        if (normalized.startsWith("5")) {
            return "MASTERCARD";
        }
        return "CARD";
    }

    private String buildMaskedCardNumber(String brand, String last4) {
        if (TextUtils.isEmpty(last4)) {
            return brand;
        }
        return String.format(Locale.US, "%s **** **** **** %s", brand, last4);
    }

    private String cardNumberLast4(String cardNumber) {
        String normalized = normalizeDigits(cardNumber);
        if (normalized.length() < 4) {
            return normalized;
        }
        return normalized.substring(normalized.length() - 4);
    }

    private String resolveCardHolderName() {
        if (AuthManager.getCurrentUser() != null && !TextUtils.isEmpty(AuthManager.getCurrentUser().getDisplayName())) {
            return AuthManager.getCurrentUser().getDisplayName().trim();
        }
        return "Card Holder";
    }

    private boolean isStripeReady() {
        return !TextUtils.isEmpty(StripeConfig.STRIPE_PUBLISHABLE_KEY)
                && !StripeConfig.STRIPE_PUBLISHABLE_KEY.contains("YOUR_PUBLISHABLE_KEY");
    }

    private static class ExpiryParts {
        final int month;
        final int year;

        ExpiryParts(int month, int year) {
            this.month = month;
            this.year = year;
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
            requireActivity().getOnBackPressedDispatcher().onBackPressed();
        });

        dialog.show();
    }
}
