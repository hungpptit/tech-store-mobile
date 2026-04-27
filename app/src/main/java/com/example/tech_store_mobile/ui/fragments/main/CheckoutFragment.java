package com.example.tech_store_mobile.ui.fragments.main;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.tech_store_mobile.Model.Address;
import com.example.tech_store_mobile.Model.CreatePaymentIntentRequest;
import com.example.tech_store_mobile.Model.CreatePaymentIntentResponse;
import com.example.tech_store_mobile.Model.LichSuThanhToan;
import com.example.tech_store_mobile.Model.PaymentMethod;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.MainNavigationHelper;
import com.example.tech_store_mobile.utils.StripeConfig;
import com.example.tech_store_mobile.utils.StripePaymentApiClient;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CheckoutFragment extends Fragment {
    private static final String TAG = "CheckoutFragment";
    private static final String PREFS_NAME = "checkout_refresh_prefs";
    private static final String KEY_CART_NEEDS_RELOAD = "key_cart_needs_reload";

    private static final String ARG_SUBTOTAL = "arg_subtotal";
    private static final String ARG_VAT = "arg_vat";
    private static final String ARG_SHIPPING = "arg_shipping";
    private static final String ARG_TOTAL = "arg_total";
    private static final String ARG_SELECTED_CART_DOC_IDS = "arg_selected_cart_doc_ids";

    private static final double DEFAULT_SUBTOTAL = 2089.00;
    private static final double DEFAULT_VAT = 0.00;
    private static final double DEFAULT_SHIPPING = 80.00;

    private TextView btnChangeAddress;
    private TextView tvAddressNickname;
    private TextView tvAddressDetail;
    private MaterialButton btnPaymentCard;
    private MaterialButton btnPaymentCash;
    private MaterialCardView paymentDetailsCard;
    private TextView tvCardNumber;
    private View btnEditCard;
    private TextView tvSubtotalValue;
    private TextView tvVatValue;
    private TextView tvShippingValue;
    private TextView tvTotalValue;
    private MaterialButton btnPlaceOrder;
    private final StripePaymentApiClient stripePaymentApiClient = new StripePaymentApiClient();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private double subtotal = DEFAULT_SUBTOTAL;
    private double vat = DEFAULT_VAT;
    private double shipping = DEFAULT_SHIPPING;
    private double total = DEFAULT_SUBTOTAL + DEFAULT_VAT + DEFAULT_SHIPPING;
    private boolean isCardPaymentSelected = true;
    private String defaultAddressId;
    private String defaultPaymentId;
    private Address selectedAddress;
    private PaymentMethod selectedPaymentMethod;
    private ArrayList<String> selectedCartDocIds = new ArrayList<>();

    public static CheckoutFragment newInstance(double subtotal, double vat, double shipping, double total, ArrayList<String> selectedCartDocIds) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_SUBTOTAL, subtotal);
        args.putDouble(ARG_VAT, vat);
        args.putDouble(ARG_SHIPPING, shipping);
        args.putDouble(ARG_TOTAL, total);
        args.putStringArrayList(ARG_SELECTED_CART_DOC_IDS, selectedCartDocIds);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        readArguments();
        initializeViews(view);
        bindData();
        setupListeners(view);
        applyPaymentMode(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCheckoutData();
    }

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        subtotal = args.getDouble(ARG_SUBTOTAL, DEFAULT_SUBTOTAL);
        vat = args.getDouble(ARG_VAT, DEFAULT_VAT);
        shipping = args.getDouble(ARG_SHIPPING, DEFAULT_SHIPPING);
        total = args.getDouble(ARG_TOTAL, subtotal + vat + shipping);
        ArrayList<String> cartDocIds = args.getStringArrayList(ARG_SELECTED_CART_DOC_IDS);
        selectedCartDocIds = cartDocIds != null ? cartDocIds : new ArrayList<>();
    }

    private void initializeViews(View view) {
        btnChangeAddress = view.findViewById(R.id.btn_change_address);
        tvAddressNickname = view.findViewById(R.id.tv_checkout_address_nickname);
        tvAddressDetail = view.findViewById(R.id.tv_checkout_address_detail);
        btnPaymentCard = view.findViewById(R.id.btn_payment_card);
        btnPaymentCash = view.findViewById(R.id.btn_payment_cash);
        paymentDetailsCard = view.findViewById(R.id.card_payment_details);
        tvCardNumber = view.findViewById(R.id.tv_card_number_checkout);
        btnEditCard = view.findViewById(R.id.btn_edit_card);
        tvSubtotalValue = view.findViewById(R.id.tv_subtotal_value_checkout);
        tvVatValue = view.findViewById(R.id.tv_vat_value_checkout);
        tvShippingValue = view.findViewById(R.id.tv_shipping_value_checkout);
        tvTotalValue = view.findViewById(R.id.tv_total_value_checkout);
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);
    }

    private void bindData() {
        tvSubtotalValue.setText(formatMoney(subtotal));
        tvVatValue.setText(formatMoney(vat));
        tvShippingValue.setText(formatMoney(shipping));
        tvTotalValue.setText(formatMoney(total));
        renderAddress();
        renderPaymentDetails();
    }

    private void loadCheckoutData() {
        if (!isAdded()) {
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            selectedAddress = null;
            selectedPaymentMethod = null;
            defaultAddressId = null;
            defaultPaymentId = null;
            renderAddress();
            renderPaymentDetails();
            return;
        }

        selectedAddress = null;
        selectedPaymentMethod = null;
        defaultAddressId = null;
        defaultPaymentId = null;

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    defaultAddressId = snapshot.getString("defaultAddressId");
                    defaultPaymentId = snapshot.getString("defaultPaymentId");

                    loadDefaultAddress(userId);
                    loadDefaultPaymentMethod(userId);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load checkout defaults", e);
                    loadDefaultAddress(userId);
                    loadDefaultPaymentMethod(userId);
                });
    }

    private void loadDefaultAddress(String userId) {
        db.collection("addresses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) {
                        return;
                    }

                    List<Address> addresses = new ArrayList<>(queryDocumentSnapshots.toObjects(Address.class));
                    selectedAddress = pickPreferredAddress(addresses);
                    renderAddress();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load addresses", e);
                    selectedAddress = null;
                    renderAddress();
                });
    }

    private void loadDefaultPaymentMethod(String userId) {
        db.collection("payment_methods")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!isAdded()) {
                        return;
                    }

                    List<PaymentMethod> paymentMethods = new ArrayList<>(queryDocumentSnapshots.toObjects(PaymentMethod.class));
                    selectedPaymentMethod = pickPreferredPaymentMethod(paymentMethods);
                    renderPaymentDetails();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load payment methods", e);
                    selectedPaymentMethod = null;
                    renderPaymentDetails();
                });
    }

    private Address pickPreferredAddress(List<Address> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return null;
        }

        if (!TextUtils.isEmpty(defaultAddressId)) {
            for (Address address : addresses) {
                if (defaultAddressId.equals(address.getAddressId())) {
                    return address;
                }
            }
        }

        for (Address address : addresses) {
            if (Boolean.TRUE.equals(address.getIsDefault())) {
                return address;
            }
        }

        return addresses.get(0);
    }

    private PaymentMethod pickPreferredPaymentMethod(List<PaymentMethod> paymentMethods) {
        if (paymentMethods == null || paymentMethods.isEmpty()) {
            return null;
        }

        if (!TextUtils.isEmpty(defaultPaymentId)) {
            for (PaymentMethod paymentMethod : paymentMethods) {
                if (defaultPaymentId.equals(paymentMethod.getPaymentId()) && isReusableStripePaymentMethod(paymentMethod)) {
                    return paymentMethod;
                }
            }
        }

        for (PaymentMethod paymentMethod : paymentMethods) {
            if (isReusableStripePaymentMethod(paymentMethod)) {
                return paymentMethod;
            }
        }

        for (PaymentMethod paymentMethod : paymentMethods) {
            if (!TextUtils.isEmpty(defaultPaymentId) && defaultPaymentId.equals(paymentMethod.getPaymentId())) {
                return paymentMethod;
            }

            if (Boolean.TRUE.equals(paymentMethod.getIsDefault())) {
                return paymentMethod;
            }
        }

        return paymentMethods.get(0);
    }

    private boolean isReusableStripePaymentMethod(PaymentMethod paymentMethod) {
        return paymentMethod != null
                && !TextUtils.isEmpty(paymentMethod.getPaymentId())
                && paymentMethod.getPaymentId().startsWith("pm_");
    }

    private void renderAddress() {
        if (tvAddressNickname == null || tvAddressDetail == null) {
            return;
        }

        if (selectedAddress != null) {
            tvAddressNickname.setText(safeText(selectedAddress.getNickname(), getString(R.string.checkout_delivery_address)));
            tvAddressDetail.setText(safeText(selectedAddress.getFullAddress(), getString(R.string.checkout_address_detail)));
        } else {
            tvAddressNickname.setText(R.string.checkout_delivery_address);
            tvAddressDetail.setText(R.string.checkout_address_detail);
        }
    }

    private void renderPaymentDetails() {
        if (tvCardNumber == null) {
            return;
        }

        if (!isCardPaymentSelected) {
            tvCardNumber.setText(R.string.checkout_cash_text);
            btnEditCard.setVisibility(View.GONE);
            paymentDetailsCard.setVisibility(View.VISIBLE);
            return;
        }

        btnEditCard.setVisibility(View.VISIBLE);
        paymentDetailsCard.setVisibility(View.VISIBLE);

        if (selectedPaymentMethod == null) {
            tvCardNumber.setText(R.string.checkout_card_number_placeholder);
            btnEditCard.setVisibility(View.VISIBLE);
            return;
        }

        String cardType = safeText(selectedPaymentMethod.getCardType(), "CARD");
        String maskedNumber = maskCardNumber(selectedPaymentMethod.getCardNumber());
        tvCardNumber.setText(getString(R.string.checkout_card_number_format, cardType, maskedNumber));
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_checkout);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageView btnNotification = view.findViewById(R.id.btn_notification_checkout);
        btnNotification.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.checkout_notifications_toast, Toast.LENGTH_SHORT).show());

        btnChangeAddress.setOnClickListener(v -> replaceFragment(new AddressFragment()));

        btnPaymentCard.setOnClickListener(v -> applyPaymentMode(true));
        btnPaymentCash.setOnClickListener(v -> applyPaymentMode(false));

        btnEditCard.setOnClickListener(v -> replaceFragment(new PaymentMethodFragment()));
        btnPlaceOrder.setOnClickListener(v -> {
            if (isCardPaymentSelected) {
                startStripePaymentFlow();
            } else {
                Toast.makeText(requireContext(), R.string.checkout_place_order_toast, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyPaymentMode(boolean cardSelected) {
        isCardPaymentSelected = cardSelected;
        if (cardSelected) {
            btnPaymentCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCard.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            btnPaymentCard.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
            btnPaymentCash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)));
            btnPaymentCash.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            btnPaymentCash.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_light)));
            btnPaymentCash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
        } else {
            btnPaymentCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)));
            btnPaymentCard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            btnPaymentCard.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            btnPaymentCash.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
        }

        renderPaymentDetails();
    }

    private String safeText(String value, String fallback) {
        return TextUtils.isEmpty(value) ? fallback : value;
    }

    private String maskCardNumber(String cardNumber) {
        if (TextUtils.isEmpty(cardNumber)) {
            return "**** **** **** ****";
        }

        String normalized = cardNumber.replaceAll("\\s+", "");
        if (normalized.length() < 4) {
            return cardNumber;
        }

        return "**** **** **** " + normalized.substring(normalized.length() - 4);
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "$ %.2f", amount);
    }

    private void startStripePaymentFlow() {
        if (!isAdded()) {
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!StripeConfig.isConfigured()) {
            Toast.makeText(requireContext(), "Stripe chưa được cấu hình. Hãy điền key test trong StripeConfig.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPaymentMethod == null || TextUtils.isEmpty(selectedPaymentMethod.getPaymentId())) {
            Toast.makeText(requireContext(), "Chưa có thẻ đã lưu hợp lệ. Hãy thêm thẻ mới.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!selectedPaymentMethod.getPaymentId().startsWith("pm_")) {
            Toast.makeText(requireContext(), "Thẻ đang hiển thị là thẻ cũ, chưa thể dùng để thanh toán. Hãy thêm lại thẻ mới.", Toast.LENGTH_SHORT).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);
        btnPlaceOrder.setText(R.string.checkout_processing);

        String orderId = "CHECKOUT-" + System.currentTimeMillis();
        CreatePaymentIntentRequest request = new CreatePaymentIntentRequest(
                userId,
                orderId,
                subtotal,
                vat,
                shipping,
                total,
                StripeConfig.CURRENCY_USD,
                selectedPaymentMethod.getPaymentId()
        );

        stripePaymentApiClient.createPaymentIntent(request, new StripePaymentApiClient.Callback() {
            @Override
            public void onSuccess(CreatePaymentIntentResponse response) {
                if (!isAdded()) {
                    return;
                }

                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText(R.string.checkout_place_order);

                String status = response.getStatus();
                if (status != null && status.equalsIgnoreCase("succeeded")) {
                    savePaymentHistoryAndClearCart(userId, orderId, response);
                    return;
                }

                if (status != null && status.equalsIgnoreCase("processing")) {
                    Toast.makeText(requireContext(), "Thanh toán đang được xử lý, vui lòng chờ một chút.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(requireContext(), "Thanh toán chưa hoàn tất: " + (status != null ? status : "unknown"), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText(R.string.checkout_place_order);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void savePaymentHistoryAndClearCart(String userId, String orderId, CreatePaymentIntentResponse response) {
        String paymentHistoryId = db.collection("lich_su_thanh_toans").document().getId();
        double amount = response.getAmount() != null ? response.getAmount() / 100.0 : total;
        String currency = !TextUtils.isEmpty(response.getCurrency()) ? response.getCurrency() : StripeConfig.CURRENCY_USD;
        String paymentIntentId = !TextUtils.isEmpty(response.getPaymentIntentId()) ? response.getPaymentIntentId() : "";

        LichSuThanhToan history = new LichSuThanhToan(
                paymentHistoryId,
                userId,
                null,
                orderId,
                safeText(selectedPaymentMethod != null ? selectedPaymentMethod.getCardType() : null, "Card"),
                "Stripe",
                "Succeeded",
                paymentIntentId,
                paymentIntentId,
                amount,
                currency,
                Timestamp.now(),
                Timestamp.now(),
                null,
                ""
        );

        db.collection("lich_su_thanh_toans").document(paymentHistoryId).set(history)
                .addOnSuccessListener(unused -> clearCartAndGoHome(userId))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save payment history", e);
                    Toast.makeText(requireContext(), "Thanh toán thành công nhưng không lưu được lịch sử: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    clearCartAndGoHome(userId);
                });
    }

    private void clearCartAndGoHome(String userId) {
        if (selectedCartDocIds == null || selectedCartDocIds.isEmpty()) {
            markCartNeedsReload();
            showPaymentSuccessDialog();
            return;
        }

        WriteBatch batch = db.batch();
        for (String docId : selectedCartDocIds) {
            if (!TextUtils.isEmpty(docId)) {
                batch.delete(db.collection("carts").document(docId));
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    markCartNeedsReload();
                    showPaymentSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to clear selected cart items", e);
                    Toast.makeText(requireContext(), "Thanh toán xong nhưng không xóa được sản phẩm đã chọn: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    MainNavigationHelper.navigateBackToHome(this);
                });
    }

    private void markCartNeedsReload() {
        if (!isAdded()) {
            return;
        }

        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_CART_NEEDS_RELOAD, true).apply();
    }

    private void showPaymentSuccessDialog() {
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);

        TextView title = dialog.findViewById(R.id.tv_success_title);
        TextView message = dialog.findViewById(R.id.tv_success_message);
        AppCompatButton btnThanks = dialog.findViewById(R.id.btn_thanks);

        if (title != null) {
            title.setText(R.string.dialog_payment_success_title);
        }
        if (message != null) {
            message.setText(R.string.dialog_payment_success_message);
        }
        if (btnThanks != null) {
            btnThanks.setText(R.string.dialog_payment_success_button);
            btnThanks.setOnClickListener(v -> {
                dialog.dismiss();
                MainNavigationHelper.navigateBackToHome(this);
                ViewPager viewPager = requireActivity().findViewById(R.id.view_pager);
                if (viewPager != null) {
                    viewPager.setCurrentItem(0);
                }
            });
        }

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        dialog.setCancelable(false);
        dialog.show();
    }


    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}







