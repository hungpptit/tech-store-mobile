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
import com.example.tech_store_mobile.Model.HoaDon;
import com.example.tech_store_mobile.Model.Order;
import com.example.tech_store_mobile.Model.OrderItem;
import com.example.tech_store_mobile.Model.OrderSummary;
import com.example.tech_store_mobile.Model.LichSuThanhToan;
import com.example.tech_store_mobile.Model.PaymentMethod;
import com.example.tech_store_mobile.Model.ShippingAddressSnapshot;
import com.example.tech_store_mobile.Model.TrackingHistoryItem;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.MainNavigationHelper;
import com.example.tech_store_mobile.utils.StripeConfig;
import com.example.tech_store_mobile.utils.StripePaymentApiClient;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestoreException;
import androidx.appcompat.widget.AppCompatButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.Timestamp;
import java.util.HashMap;
import java.util.Map;

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
    private String currentUserFullName;
    private String currentUserPhoneNumber;
    private Address selectedAddress;
    private PaymentMethod selectedPaymentMethod;
    private NotificationBadgeManager.BadgeListener badgeListener;
    private TextView notificationBadgeView;
    private ArrayList<String> selectedCartDocIds = new ArrayList<>();
    private String currentReservationId;

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

                    currentUserFullName = snapshot.getString("fullName");
                    currentUserPhoneNumber = snapshot.getString("phoneNumber");
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
            tvAddressNickname.setText(safeText(selectedAddress.getNickname(), "Địa chỉ giao hàng"));
            tvAddressDetail.setText(selectedAddress.getFullAddress());
            tvAddressDetail.setTextColor(Color.parseColor("#9B9B9B"));
        } else {
            tvAddressNickname.setText("Chưa có địa chỉ nhận hàng");
            tvAddressDetail.setText("Vui lòng thêm địa chỉ giao hàng để tiếp tục!");
            tvAddressDetail.setTextColor(Color.parseColor("#D32F2F"));
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
        btnBack.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }

        btnChangeAddress.setOnClickListener(v -> replaceFragment(new AddressFragment()));

        btnPaymentCard.setOnClickListener(v -> applyPaymentMode(true));
        btnPaymentCash.setOnClickListener(v -> applyPaymentMode(false));

        btnEditCard.setOnClickListener(v -> replaceFragment(new PaymentMethodFragment()));
        btnPlaceOrder.setOnClickListener(v -> {
            if (selectedAddress == null) {
                Toast.makeText(requireContext(), "Vui lòng chọn hoặc thêm địa chỉ nhận hàng trước khi thanh toán!", Toast.LENGTH_LONG).show();
                return;
            }

            if (isCardPaymentSelected) {
                String userId = AuthManager.getCurrentUid();
                if (userId == null) {
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnPlaceOrder.setEnabled(false);
                btnPlaceOrder.setText(R.string.checkout_processing);

                preLoadCheckoutItems(userId, 0, new ArrayList<>(), new OnItemsLoadedListener() {
                    @Override
                    public void onLoaded(List<OrderItem> items) {
                        performStockReservation(userId, items);
                    }

                    @Override
                    public void onFailure(String error) {
                        btnPlaceOrder.setEnabled(true);
                        btnPlaceOrder.setText(R.string.checkout_place_order);
                        Toast.makeText(requireContext(), "Lỗi tải thông tin sản phẩm: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
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

    @Override
    public void onStart() {
        super.onStart();
        startNotificationBadgeListener();
    }

    @Override
    public void onStop() {
        super.onStop();
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
        if (!isAdded()) {
            return;
        }

        loadSelectedOrderItems(userId, orderId, response, 0, new ArrayList<>());
    }

    private void loadSelectedOrderItems(String userId, String orderId, CreatePaymentIntentResponse response,
                                        int index, List<OrderItem> orderItems) {
        if (!isAdded()) {
            return;
        }

        if (selectedCartDocIds == null || index >= selectedCartDocIds.size()) {
            persistOrderInvoiceAndHistory(userId, orderId, response, orderItems);
            return;
        }

        String cartDocId = selectedCartDocIds.get(index);
        if (TextUtils.isEmpty(cartDocId)) {
            loadSelectedOrderItems(userId, orderId, response, index + 1, orderItems);
            return;
        }

        db.collection("carts").document(cartDocId)
                .get()
                .addOnSuccessListener(cartSnapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    String productId = cartSnapshot.getString("productId");
                    Long quantity = cartSnapshot.getLong("quantity");
                    String selectedColor = safeText(cartSnapshot.getString("selectedColor"), "");
                    Double priceAtAdded = cartSnapshot.getDouble("priceAtAdded");

                    if (TextUtils.isEmpty(productId)) {
                        loadSelectedOrderItems(userId, orderId, response, index + 1, orderItems);
                        return;
                    }

                    db.collection("products").document(productId)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                if (!isAdded()) {
                                    return;
                                }

                                String productName = safeText(productSnapshot.getString("productName"), productId);
                                String imageUrl = productSnapshot.getString("imageUrl");
                                Double price = priceAtAdded != null ? priceAtAdded : productSnapshot.getDouble("finalPrice");

                                orderItems.add(new OrderItem(
                                        productId,
                                        productName,
                                        quantity != null ? quantity : 1L,
                                        price != null ? price : 0.0,
                                        imageUrl,
                                        selectedColor
                                ));

                                loadSelectedOrderItems(userId, orderId, response, index + 1, orderItems);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to load product for order item", e);
                                orderItems.add(new OrderItem(
                                        productId,
                                        productId,
                                        quantity != null ? quantity : 1L,
                                        priceAtAdded != null ? priceAtAdded : 0.0,
                                        null,
                                        selectedColor
                                ));
                                loadSelectedOrderItems(userId, orderId, response, index + 1, orderItems);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load cart item for order", e);
                    loadSelectedOrderItems(userId, orderId, response, index + 1, orderItems);
                });
    }

    private void persistOrderInvoiceAndHistory(String userId, String orderId, CreatePaymentIntentResponse response,
                                               List<OrderItem> orderItems) {
        if (!isAdded()) {
            return;
        }

        if (orderItems == null || orderItems.isEmpty()) {
            Log.w(TAG, "No order items found while saving checkout data.");
            Toast.makeText(requireContext(), "Không thể lưu đơn hàng vì không có sản phẩm hợp lệ.", Toast.LENGTH_SHORT).show();
            return;
        }

        Timestamp now = Timestamp.now();
        String paymentIntentId = !TextUtils.isEmpty(response.getPaymentIntentId()) ? response.getPaymentIntentId() : "";
        String currency = !TextUtils.isEmpty(response.getCurrency()) ? response.getCurrency() : StripeConfig.CURRENCY_USD;
        double amount = response.getAmount() != null ? response.getAmount() / 100.0 : total;
        String paymentMethodLabel = buildPaymentMethodLabel();
        String orderDocumentId = !TextUtils.isEmpty(orderId) ? orderId : db.collection("orders").document().getId();
        String hoaDonId = db.collection("hoa_dons").document().getId();
        String paymentHistoryId = db.collection("lich_su_thanh_toans").document().getId();
        String invoiceNumber = "INV-" + System.currentTimeMillis();

        Order order = new Order(
                orderDocumentId,
                userId,
                now,
                "Packing",
                orderItems,
                new OrderSummary(subtotal, shipping, vat, total),
                buildShippingAddressSnapshot(),
                paymentMethodLabel,
                buildTrackingHistory(now)
        );

        HoaDon hoaDon = new HoaDon(
                hoaDonId,
                userId,
                orderDocumentId,
                invoiceNumber,
                now,
                null,
                "Paid",
                paymentMethodLabel,
                "Stripe",
                paymentIntentId,
                orderItems,
                new OrderSummary(subtotal, shipping, vat, total),
                buildShippingAddressSnapshot(),
                "Thanh toán thành công qua Stripe",
                now
        );

        LichSuThanhToan history = new LichSuThanhToan(
                paymentHistoryId,
                userId,
                hoaDonId,
                orderDocumentId,
                paymentMethodLabel,
                "Stripe",
                "Succeeded",
                paymentIntentId,
                paymentIntentId,
                amount,
                currency,
                now,
                now,
                null,
                ""
        );

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderDocumentId), order);
        batch.set(db.collection("hoa_dons").document(hoaDonId), hoaDon);
        batch.set(db.collection("lich_su_thanh_toans").document(paymentHistoryId), history);

        if (selectedCartDocIds != null) {
            for (String docId : selectedCartDocIds) {
                if (!TextUtils.isEmpty(docId)) {
                    batch.delete(db.collection("carts").document(docId));
                }
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    markCartNeedsReload();
                    showPaymentSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save checkout documents", e);
                    Toast.makeText(requireContext(), "Thanh toán thành công nhưng không lưu được đơn/hóa đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    MainNavigationHelper.navigateBackToHome(this);
                });
    }

    private ShippingAddressSnapshot buildShippingAddressSnapshot() {
        String receiverName = safeText(currentUserFullName, safeText(selectedAddress != null ? selectedAddress.getNickname() : null, "Customer"));
        String phoneNumber = safeText(currentUserPhoneNumber, "");
        String fullAddress = safeText(selectedAddress != null ? selectedAddress.getFullAddress() : null, "");
        String note = safeText(selectedAddress != null ? selectedAddress.getNickname() : null, "");

        return new ShippingAddressSnapshot(receiverName, phoneNumber, fullAddress, note);
    }

    private String buildPaymentMethodLabel() {
        if (selectedPaymentMethod == null) {
            return isCardPaymentSelected ? "Card" : "Cash";
        }

        String cardType = safeText(selectedPaymentMethod.getCardType(), isCardPaymentSelected ? "Card" : "Cash");
        String cardNumber = safeText(selectedPaymentMethod.getCardNumber(), "");
        return TextUtils.isEmpty(cardNumber) ? cardType : cardType + " " + cardNumber;
    }

    private List<TrackingHistoryItem> buildTrackingHistory(Timestamp now) {
        List<TrackingHistoryItem> trackingHistory = new ArrayList<>();
        trackingHistory.add(new TrackingHistoryItem("Packing", "Tech Store", now));
        return trackingHistory;
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


    @Override
    public void onDestroyView() {
        if (currentReservationId != null) {
            releaseReservationImmediately(currentReservationId);
            currentReservationId = null;
        }
        super.onDestroyView();
    }

    private interface OnItemsLoadedListener {
        void onLoaded(List<OrderItem> items);
        void onFailure(String error);
    }

    private void preLoadCheckoutItems(String userId, int index, List<OrderItem> loadedItems, OnItemsLoadedListener listener) {
        if (!isAdded()) {
            return;
        }

        if (selectedCartDocIds == null || index >= selectedCartDocIds.size()) {
            listener.onLoaded(loadedItems);
            return;
        }

        String cartDocId = selectedCartDocIds.get(index);
        if (TextUtils.isEmpty(cartDocId)) {
            preLoadCheckoutItems(userId, index + 1, loadedItems, listener);
            return;
        }

        db.collection("carts").document(cartDocId)
                .get()
                .addOnSuccessListener(cartSnapshot -> {
                    if (!isAdded()) {
                        return;
                    }

                    String productId = cartSnapshot.getString("productId");
                    Long quantity = cartSnapshot.getLong("quantity");
                    String selectedColor = safeText(cartSnapshot.getString("selectedColor"), "");
                    Double priceAtAdded = cartSnapshot.getDouble("priceAtAdded");

                    if (TextUtils.isEmpty(productId)) {
                        preLoadCheckoutItems(userId, index + 1, loadedItems, listener);
                        return;
                    }

                    db.collection("products").document(productId)
                            .get()
                            .addOnSuccessListener(productSnapshot -> {
                                if (!isAdded()) {
                                    return;
                                }

                                String productName = safeText(productSnapshot.getString("productName"), productId);
                                String imageUrl = productSnapshot.getString("imageUrl");
                                Double price = priceAtAdded != null ? priceAtAdded : productSnapshot.getDouble("finalPrice");

                                loadedItems.add(new OrderItem(
                                        productId,
                                        productName,
                                        quantity != null ? quantity : 1L,
                                        price != null ? price : 0.0,
                                        imageUrl,
                                        selectedColor
                                ));

                                preLoadCheckoutItems(userId, index + 1, loadedItems, listener);
                            })
                            .addOnFailureListener(e -> {
                                Log.w(TAG, "Failed to load product for order item", e);
                                loadedItems.add(new OrderItem(
                                        productId,
                                        productId,
                                        quantity != null ? quantity : 1L,
                                        priceAtAdded != null ? priceAtAdded : 0.0,
                                        null,
                                        selectedColor
                                ));
                                preLoadCheckoutItems(userId, index + 1, loadedItems, listener);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Failed to load cart item for order", e);
                    preLoadCheckoutItems(userId, index + 1, loadedItems, listener);
                });
    }

    private void performStockReservation(String userId, List<OrderItem> items) {
        String reservationId = db.collection("stock_reservations").document().getId();

        db.runTransaction(transaction -> {
            List<Long> currentStocks = new ArrayList<>();
            for (OrderItem item : items) {
                DocumentReference productRef = db.collection("products").document(item.getProductId());
                DocumentSnapshot productSnap = transaction.get(productRef);

                if (!productSnap.exists()) {
                    throw new FirebaseFirestoreException("Sản phẩm không tồn tại: " + item.getProductName(), FirebaseFirestoreException.Code.NOT_FOUND);
                }

                Long stock = productSnap.getLong("stockQuantity");
                if (stock == null) stock = 0L;

                if (stock < item.getQuantity()) {
                    throw new FirebaseFirestoreException("Sản phẩm " + item.getProductName() + " chỉ còn " + stock + " sản phẩm trong kho!", FirebaseFirestoreException.Code.ABORTED);
                }
                currentStocks.add(stock);
            }

            for (int i = 0; i < items.size(); i++) {
                OrderItem item = items.get(i);
                Long currentStock = currentStocks.get(i);
                DocumentReference productRef = db.collection("products").document(item.getProductId());
                transaction.update(productRef, "stockQuantity", currentStock - item.getQuantity());
            }

            DocumentReference reservationRef = db.collection("stock_reservations").document(reservationId);

            Map<String, Object> reservation = new HashMap<>();
            reservation.put("reservationId", reservationId);
            reservation.put("userId", userId);

            List<Map<String, Object>> reservationItems = new ArrayList<>();
            for (OrderItem item : items) {
                Map<String, Object> m = new HashMap<>();
                m.put("productId", item.getProductId());
                m.put("productName", item.getProductName());
                m.put("quantity", item.getQuantity());
                m.put("price", item.getPrice());
                m.put("imageUrl", item.getImageUrl());
                m.put("color", item.getColor());
                reservationItems.add(m);
            }
            reservation.put("items", reservationItems);
            reservation.put("status", "pending");
            reservation.put("createdAt", Timestamp.now());

            long fiveMinutesMillis = 5 * 60 * 1000;
            Timestamp expiresAt = new Timestamp(new java.util.Date(System.currentTimeMillis() + fiveMinutesMillis));
            reservation.put("expiresAt", expiresAt);

            transaction.set(reservationRef, reservation);
            return reservationId;
        })
        .addOnSuccessListener(resId -> {
            if (!isAdded()) return;
            Log.d(TAG, "Stock reserved successfully: " + resId);
            currentReservationId = resId;
            startStripePaymentFlowWithReservation(userId, items);
        })
        .addOnFailureListener(e -> {
            if (!isAdded()) return;
            Log.e(TAG, "Stock reservation failed", e);
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText(R.string.checkout_place_order);
            showNotificationDialog(
                    "Hết hàng",
                    e.getMessage(),
                    "Quay lại giỏ hàng",
                    () -> {
                        if (isAdded()) {
                            getParentFragmentManager().popBackStack();
                        }
                    }
            );
        });
    }

    private void startStripePaymentFlowWithReservation(String userId, List<OrderItem> items) {
        if (!isAdded()) {
            return;
        }

        if (!StripeConfig.isConfigured()) {
            Toast.makeText(requireContext(), "Stripe chưa được cấu hình. Hãy điền key test trong StripeConfig.", Toast.LENGTH_SHORT).show();
            if (currentReservationId != null) {
                releaseReservationImmediately(currentReservationId);
                currentReservationId = null;
            }
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText(R.string.checkout_place_order);
            return;
        }

        if (selectedPaymentMethod == null || TextUtils.isEmpty(selectedPaymentMethod.getPaymentId())) {
            Toast.makeText(requireContext(), "Chưa có thẻ đã lưu hợp lệ. Hãy thêm thẻ mới.", Toast.LENGTH_SHORT).show();
            if (currentReservationId != null) {
                releaseReservationImmediately(currentReservationId);
                currentReservationId = null;
            }
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText(R.string.checkout_place_order);
            return;
        }

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

                String status = response.getStatus();
                if (status != null && status.equalsIgnoreCase("succeeded")) {
                    persistOrderInvoiceAndHistoryWithReservation(userId, orderId, response, items);
                    return;
                }

                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText(R.string.checkout_place_order);

                if (status != null && status.equalsIgnoreCase("processing")) {
                    Toast.makeText(requireContext(), "Thanh toán đang được xử lý, vui lòng chờ một chút.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(requireContext(), "Thanh toán chưa hoàn tất: " + (status != null ? status : "unknown"), Toast.LENGTH_SHORT).show();
                if (currentReservationId != null) {
                    releaseReservationImmediately(currentReservationId);
                    currentReservationId = null;
                }
            }

            @Override
            public void onError(String message) {
                if (!isAdded()) {
                    return;
                }

                btnPlaceOrder.setEnabled(true);
                btnPlaceOrder.setText(R.string.checkout_place_order);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                if (currentReservationId != null) {
                    releaseReservationImmediately(currentReservationId);
                    currentReservationId = null;
                }
            }
        });
    }

    private void persistOrderInvoiceAndHistoryWithReservation(String userId, String orderId, CreatePaymentIntentResponse response,
                                                               List<OrderItem> orderItems) {
        if (!isAdded()) {
            return;
        }

        if (orderItems == null || orderItems.isEmpty()) {
            Log.w(TAG, "No order items found while saving checkout data.");
            Toast.makeText(requireContext(), "Không thể lưu đơn hàng vì không có sản phẩm hợp lệ.", Toast.LENGTH_SHORT).show();
            if (currentReservationId != null) {
                releaseReservationImmediately(currentReservationId);
                currentReservationId = null;
            }
            btnPlaceOrder.setEnabled(true);
            btnPlaceOrder.setText(R.string.checkout_place_order);
            return;
        }

        Timestamp now = Timestamp.now();
        String paymentIntentId = !TextUtils.isEmpty(response.getPaymentIntentId()) ? response.getPaymentIntentId() : "";
        String currency = !TextUtils.isEmpty(response.getCurrency()) ? response.getCurrency() : StripeConfig.CURRENCY_USD;
        double amount = response.getAmount() != null ? response.getAmount() / 100.0 : total;
        String paymentMethodLabel = buildPaymentMethodLabel();
        String orderDocumentId = !TextUtils.isEmpty(orderId) ? orderId : db.collection("orders").document().getId();
        String hoaDonId = db.collection("hoa_dons").document().getId();
        String paymentHistoryId = db.collection("lich_su_thanh_toans").document().getId();
        String invoiceNumber = "INV-" + System.currentTimeMillis();

        Order order = new Order(
                orderDocumentId,
                userId,
                now,
                "Packing",
                orderItems,
                new OrderSummary(subtotal, shipping, vat, total),
                buildShippingAddressSnapshot(),
                paymentMethodLabel,
                buildTrackingHistory(now)
        );

        HoaDon hoaDon = new HoaDon(
                hoaDonId,
                userId,
                orderDocumentId,
                invoiceNumber,
                now,
                null,
                "Paid",
                paymentMethodLabel,
                "Stripe",
                paymentIntentId,
                orderItems,
                new OrderSummary(subtotal, shipping, vat, total),
                buildShippingAddressSnapshot(),
                "Thanh toán thành công qua Stripe",
                now
        );

        LichSuThanhToan history = new LichSuThanhToan(
                paymentHistoryId,
                userId,
                hoaDonId,
                orderDocumentId,
                paymentMethodLabel,
                "Stripe",
                "Succeeded",
                paymentIntentId,
                paymentIntentId,
                amount,
                currency,
                now,
                now,
                null,
                ""
        );

        WriteBatch batch = db.batch();
        batch.set(db.collection("orders").document(orderDocumentId), order);
        batch.set(db.collection("hoa_dons").document(hoaDonId), hoaDon);
        batch.set(db.collection("lich_su_thanh_toans").document(paymentHistoryId), history);

        if (!TextUtils.isEmpty(currentReservationId)) {
            batch.update(db.collection("stock_reservations").document(currentReservationId), "status", "completed");
        }

        if (selectedCartDocIds != null) {
            for (String docId : selectedCartDocIds) {
                if (!TextUtils.isEmpty(docId)) {
                    batch.delete(db.collection("carts").document(docId));
                }
            }
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    currentReservationId = null;
                    markCartNeedsReload();
                    showPaymentSuccessDialog();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to save checkout documents", e);
                    Toast.makeText(requireContext(), "Thanh toán thành công nhưng không lưu được đơn/hóa đơn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    currentReservationId = null;
                    MainNavigationHelper.navigateBackToHome(this);
                });
    }

    private void releaseReservationImmediately(String reservationId) {
        if (TextUtils.isEmpty(reservationId)) return;

        db.runTransaction(transaction -> {
            DocumentReference resRef = db.collection("stock_reservations").document(reservationId);
            DocumentSnapshot resSnap = transaction.get(resRef);

            if (resSnap.exists() && "pending".equals(resSnap.getString("status"))) {
                List<Map<String, Object>> items = (List<Map<String, Object>>) resSnap.get("items");
                if (items != null) {
                    for (Map<String, Object> item : items) {
                        String productId = (String) item.get("productId");
                        Long quantity = (Long) item.get("quantity");
                        if (productId != null && quantity != null) {
                            DocumentReference productRef = db.collection("products").document(productId);
                            DocumentSnapshot productSnap = transaction.get(productRef);
                            if (productSnap.exists()) {
                                Long currentStock = productSnap.getLong("stockQuantity");
                                if (currentStock == null) currentStock = 0L;
                                transaction.update(productRef, "stockQuantity", currentStock + quantity);
                            }
                        }
                    }
                }
                transaction.update(resRef, "status", "released", "releasedAt", Timestamp.now());
            }
            return null;
        })
        .addOnSuccessListener(unused -> Log.d(TAG, "Reservation " + reservationId + " released immediately."))
        .addOnFailureListener(e -> Log.e(TAG, "Failed to release reservation immediately: " + reservationId, e));
    }

    private void showNotificationDialog(String titleText, String messageText, String buttonText, Runnable onButtonClick) {
        if (!isAdded()) {
            return;
        }
        Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_success);

        TextView title = dialog.findViewById(R.id.tv_success_title);
        TextView message = dialog.findViewById(R.id.tv_success_message);
        AppCompatButton btnThanks = dialog.findViewById(R.id.btn_thanks);

        if (title != null) {
            title.setText(titleText);
        }
        if (message != null) {
            message.setText(messageText);
        }
        if (btnThanks != null) {
            btnThanks.setText(buttonText);
            btnThanks.setOnClickListener(v -> {
                dialog.dismiss();
                if (onButtonClick != null) {
                    onButtonClick.run();
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
