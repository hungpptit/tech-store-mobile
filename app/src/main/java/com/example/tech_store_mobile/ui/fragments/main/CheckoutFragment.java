package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import android.content.res.ColorStateList;

import com.example.tech_store_mobile.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class CheckoutFragment extends Fragment {

    private static final String ARG_SUBTOTAL = "arg_subtotal";
    private static final String ARG_VAT = "arg_vat";
    private static final String ARG_SHIPPING = "arg_shipping";
    private static final String ARG_TOTAL = "arg_total";

    private static final double DEFAULT_SUBTOTAL = 2089.00;
    private static final double DEFAULT_VAT = 0.00;
    private static final double DEFAULT_SHIPPING = 80.00;

    private TextView btnChangeAddress;
    private TextView btnChangePayment;
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

    private double subtotal = DEFAULT_SUBTOTAL;
    private double vat = DEFAULT_VAT;
    private double shipping = DEFAULT_SHIPPING;
    private double total = DEFAULT_SUBTOTAL + DEFAULT_VAT + DEFAULT_SHIPPING;
    public static CheckoutFragment newInstance(double subtotal, double vat, double shipping, double total) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_SUBTOTAL, subtotal);
        args.putDouble(ARG_VAT, vat);
        args.putDouble(ARG_SHIPPING, shipping);
        args.putDouble(ARG_TOTAL, total);
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

    private void readArguments() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        subtotal = args.getDouble(ARG_SUBTOTAL, DEFAULT_SUBTOTAL);
        vat = args.getDouble(ARG_VAT, DEFAULT_VAT);
        shipping = args.getDouble(ARG_SHIPPING, DEFAULT_SHIPPING);
        total = args.getDouble(ARG_TOTAL, subtotal + vat + shipping);
    }

    private void initializeViews(View view) {
        btnChangeAddress = view.findViewById(R.id.btn_change_address);
        btnChangePayment = view.findViewById(R.id.btn_change_payment);
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
    }

    private void setupListeners(View view) {
        ImageView btnBack = view.findViewById(R.id.btn_back_checkout);
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageView btnNotification = view.findViewById(R.id.btn_notification_checkout);
        btnNotification.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.checkout_notifications_toast, Toast.LENGTH_SHORT).show());

        btnChangeAddress.setOnClickListener(v -> replaceFragment(new AddressFragment()));
        btnChangePayment.setOnClickListener(v -> replaceFragment(new PaymentMethodFragment()));

        btnPaymentCard.setOnClickListener(v -> applyPaymentMode(true));
        btnPaymentCash.setOnClickListener(v -> applyPaymentMode(false));

        btnEditCard.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.checkout_edit_card_toast, Toast.LENGTH_SHORT).show());
        btnPlaceOrder.setOnClickListener(v -> Toast.makeText(requireContext(), R.string.checkout_place_order_toast, Toast.LENGTH_SHORT).show());
    }

    private void applyPaymentMode(boolean cardSelected) {
        if (cardSelected) {
            btnPaymentCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCard.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            btnPaymentCard.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));
            btnPaymentCash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)));
            btnPaymentCash.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            btnPaymentCash.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.grey_light)));
            btnPaymentCash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));

            paymentDetailsCard.setVisibility(View.VISIBLE);
            tvCardNumber.setText(R.string.checkout_card_number_placeholder);
            btnEditCard.setVisibility(View.VISIBLE);
        } else {
            btnPaymentCard.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), android.R.color.white)));
            btnPaymentCard.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            btnPaymentCard.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            btnPaymentCash.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.black)));
            btnPaymentCash.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.white)));

            paymentDetailsCard.setVisibility(View.VISIBLE);
            tvCardNumber.setText(R.string.checkout_cash_text);
            btnEditCard.setVisibility(View.GONE);
        }
    }

    private String formatMoney(double amount) {
        return String.format(Locale.US, "$ %.2f", amount);
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}







