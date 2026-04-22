package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthUiHelper;

import com.example.tech_store_mobile.adapters.CartAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private ImageView btnBack;
    private View cartAppBar;
    private RecyclerView rvCart;
    private View cartSummaryContainer;
    private View cartGuestState;
    private MaterialButton btnCartSignIn;
    private TextView tvSubtotalValue;
    private TextView tvVatValue;
    private TextView tvShippingValue;
    private TextView tvTotalValue;

    private final List<CartAdapter.CartEntry> cartEntries = new ArrayList<>();
    private CartAdapter cartAdapter;

    public CartFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        initializeViews(view);
        setupRecyclerView();
        setupBackAction();
        setupAuthPrompt();
        renderAuthState();

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showBottomNavigation();
    }

    @Override
    public void onResume() {
        super.onResume();
<<<<<<< HEAD
        showBottomNavigation();
=======
        renderAuthState();
        hideBottomNavigation();
>>>>>>> origin/branchKieu
    }

    @Override
    public void onPause() {
        super.onPause();
        showBottomNavigation();
    }

    @Override
    public void onDestroyView() {
        showBottomNavigation();
        super.onDestroyView();
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        cartAppBar = view.findViewById(R.id.cartAppBar);
        ImageView btnNotification = view.findViewById(R.id.btnNotification);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        rvCart = view.findViewById(R.id.rvCart);
        cartSummaryContainer = view.findViewById(R.id.cartSummary);
        cartGuestState = view.findViewById(R.id.cartGuestState);
        btnCartSignIn = view.findViewById(R.id.btnCartSignIn);
        tvSubtotalValue = view.findViewById(R.id.tvSubtotalValue);
        tvVatValue = view.findViewById(R.id.tvVatValue);
        tvShippingValue = view.findViewById(R.id.tvShippingValue);
        tvTotalValue = view.findViewById(R.id.tvTotalValue);
        MaterialButton btnCheckout = view.findViewById(R.id.btnCheckout);

        tvTitle.setText(R.string.cart_title);
        if (btnNotification != null) {
            btnNotification.setVisibility(View.VISIBLE);
        }

        if (btnCheckout != null) {
            btnCheckout.setOnClickListener(v -> {
                // Temporary no-op until checkout flow exists
            });
        }
    }

    private void setupRecyclerView() {
        rvCart.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCart.setNestedScrollingEnabled(true);
        cartAdapter = new CartAdapter(cartEntries, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncreaseQuantity(int position) {
                if (position >= 0 && position < cartEntries.size()) {
                    CartAdapter.CartEntry item = cartEntries.get(position);
                    item.setQuantity(item.getQuantity() + 1);
                    cartAdapter.notifyItemChanged(position);
                    updateCartSummary();
                }
            }

            @Override
            public void onDecreaseQuantity(int position) {
                if (position >= 0 && position < cartEntries.size()) {
                    CartAdapter.CartEntry item = cartEntries.get(position);
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        cartAdapter.notifyItemChanged(position);
                    }
                    updateCartSummary();
                }
            }

            @Override
            public void onDeleteItem(int position) {
                if (position >= 0 && position < cartEntries.size()) {
                    cartEntries.remove(position);
                    if (cartEntries.isEmpty()) {
                        cartAdapter.notifyItemRemoved(position);
                        cartAdapter.notifyItemInserted(0);
                    } else {
                        cartAdapter.notifyItemRemoved(position);
                        if (position < cartEntries.size()) {
                            cartAdapter.notifyItemRangeChanged(position, cartEntries.size() - position);
                        }
                    }
                    updateCartSummary();
                }
            }
        });
        rvCart.setAdapter(cartAdapter);
    }

    private void loadFakeCartData() {
        int previousSize = cartEntries.size();
        cartEntries.clear();

        cartEntries.add(new CartAdapter.CartEntry("prod_001", "Macbook Pro M4", "Black", R.drawable.laptop, 2089.00, 1));
        cartEntries.add(new CartAdapter.CartEntry("prod_007", "AirPods Max", "Silver", R.drawable.headphones, 359.99, 2));

        if (previousSize == 0) {
            cartAdapter.notifyItemRemoved(0);
            cartAdapter.notifyItemRangeInserted(0, cartEntries.size());
        } else {
            cartAdapter.notifyItemRangeRemoved(0, previousSize);
            cartAdapter.notifyItemRangeInserted(0, cartEntries.size());
        }
        updateCartSummary();
    }

    private void setupAuthPrompt() {
        if (btnCartSignIn != null) {
            btnCartSignIn.setOnClickListener(v -> AuthUiHelper.openLogin(this));
        }
    }

    private void renderAuthState() {
        boolean loggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        if (cartAppBar != null) cartAppBar.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (rvCart != null) rvCart.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (cartSummaryContainer != null) cartSummaryContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (cartGuestState != null) cartGuestState.setVisibility(loggedIn ? View.GONE : View.VISIBLE);

        if (loggedIn) {
            if (cartEntries.isEmpty()) {
                loadFakeCartData();
            } else {
                updateCartSummary();
            }
        } else {
            int previousSize = cartEntries.size();
            cartEntries.clear();
            if (previousSize > 0) {
                cartAdapter.notifyItemRangeRemoved(0, previousSize);
                cartAdapter.notifyItemInserted(0);
            }
            updateCartSummary();
        }
    }

    private void updateCartSummary() {
        if (cartSummaryContainer == null) return;

        boolean hasItems = !cartEntries.isEmpty();
        cartSummaryContainer.setVisibility(hasItems ? View.VISIBLE : View.GONE);

        double subtotal = 0.0;
        for (CartAdapter.CartEntry item : cartEntries) {
            subtotal += item.getLineTotal();
        }
        double vat = 0.0;
        double shipping = hasItems ? 80.0 : 0.0;
        double total = subtotal + vat + shipping;

        if (tvSubtotalValue != null) tvSubtotalValue.setText(String.format(java.util.Locale.US, "$ %.2f", subtotal));
        if (tvVatValue != null) tvVatValue.setText(String.format(java.util.Locale.US, "$ %.2f", vat));
        if (tvShippingValue != null) tvShippingValue.setText(String.format(java.util.Locale.US, "$ %.2f", shipping));
        if (tvTotalValue != null) tvTotalValue.setText(String.format(java.util.Locale.US, "$ %.2f", total));
    }

    private void setupBackAction() {
        btnBack.setOnClickListener(v -> {
            if (isAdded()) {
                if (requireActivity().getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    requireActivity().getSupportFragmentManager().popBackStack();
                } else if (requireActivity() instanceof MainActivity) {
                    requireActivity().findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
                    requireActivity().findViewById(R.id.fragment_container).setVisibility(View.GONE);
                    ((MainActivity) requireActivity()).syncBottomNavigationVisibility();
                    androidx.viewpager.widget.ViewPager viewPager = requireActivity().findViewById(R.id.view_pager);
                    if (viewPager != null) {
                        viewPager.setCurrentItem(0);
                    }
                }
            }
        });
    }


    private void showBottomNavigation() {
        if (getActivity() == null) return;
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}