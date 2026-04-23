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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.AuthUiHelper;

import com.example.tech_store_mobile.adapters.CartAdapter;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
    private FirebaseFirestore db;
    private double currentSubtotal = 0.0;
    private double currentVat = 0.0;
    private double currentShipping = 0.0;
    private double currentTotal = 0.0;

    private final List<CartAdapter.CartEntry> cartEntries = new ArrayList<>();
    private CartAdapter cartAdapter;

    public CartFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        db = FirebaseFirestore.getInstance();
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
        showBottomNavigation();
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
            btnCheckout.setOnClickListener(v -> openCheckoutScreen());
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
                removeCartItem(position);
            }
        });
        cartAdapter.setOnCartItemClickListener(item -> {
            if (item != null && item.getProductId() != null && !item.getProductId().trim().isEmpty()) {
                navigateToProductDetail(item.getProductId());
            }
        });
        rvCart.setAdapter(cartAdapter);
    }

    private void setupAuthPrompt() {
        if (btnCartSignIn != null) {
            btnCartSignIn.setOnClickListener(v -> AuthUiHelper.openLogin(this));
        }
    }

    private void renderAuthState() {
        boolean loggedIn = AuthManager.isLoggedIn();

        if (cartAppBar != null) cartAppBar.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (rvCart != null) rvCart.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (cartSummaryContainer != null) cartSummaryContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        if (cartGuestState != null) cartGuestState.setVisibility(loggedIn ? View.GONE : View.VISIBLE);

        if (loggedIn) {
            loadCartItemsFromFirestore();
        } else {
            int previousSize = cartEntries.size();
            cartEntries.clear();
            if (previousSize > 0) {
                cartAdapter.notifyDataSetChanged();
            }
            updateCartSummary();
        }
    }

    private void loadCartItemsFromFirestore() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            cartEntries.clear();
            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
            return;
        }

        db.collection("carts")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartEntries.clear();

                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        docs.add(doc);
                    }

                    if (docs.isEmpty()) {
                        cartAdapter.notifyDataSetChanged();
                        updateCartSummary();
                        return;
                    }

                    loadCartItemAtIndex(docs, 0);
                })
                .addOnFailureListener(e -> {
                    cartEntries.clear();
                    cartAdapter.notifyDataSetChanged();
                    updateCartSummary();
                });
    }

    private void loadCartItemAtIndex(List<QueryDocumentSnapshot> docs, int index) {
        if (index >= docs.size()) {
            cartAdapter.notifyDataSetChanged();
            updateCartSummary();
            return;
        }

        QueryDocumentSnapshot doc = docs.get(index);
        String productId = doc.getString("productId");
        String selectedColor = doc.getString("selectedColor") != null ? doc.getString("selectedColor") : "";
        Long quantity = doc.getLong("quantity");
        Double priceAtAdded = doc.getDouble("priceAtAdded");

        if (productId == null || productId.trim().isEmpty()) {
            loadCartItemAtIndex(docs, index + 1);
            return;
        }

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(productSnapshot -> {
                    String productName = productSnapshot.getString("productName") != null ? productSnapshot.getString("productName") : "Unknown item";
                    String imageUrl = productSnapshot.getString("imageUrl");
                    Double price = priceAtAdded != null ? priceAtAdded : productSnapshot.getDouble("finalPrice");

                    cartEntries.add(new CartAdapter.CartEntry(
                            productId,
                            productName,
                            selectedColor,
                            imageUrl,
                            price != null ? price : 0.0,
                            quantity != null ? quantity.intValue() : 1
                    ));

                    loadCartItemAtIndex(docs, index + 1);
                })
                .addOnFailureListener(e -> loadCartItemAtIndex(docs, index + 1));
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

        currentSubtotal = subtotal;
        currentVat = vat;
        currentShipping = shipping;
        currentTotal = total;

        if (tvSubtotalValue != null) tvSubtotalValue.setText(String.format(java.util.Locale.US, "$ %.2f", subtotal));
        if (tvVatValue != null) tvVatValue.setText(String.format(java.util.Locale.US, "$ %.2f", vat));
        if (tvShippingValue != null) tvShippingValue.setText(String.format(java.util.Locale.US, "$ %.2f", shipping));
        if (tvTotalValue != null) tvTotalValue.setText(String.format(java.util.Locale.US, "$ %.2f", total));
    }

    private void openCheckoutScreen() {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        CheckoutFragment checkoutFragment = CheckoutFragment.newInstance(currentSubtotal, currentVat, currentShipping, currentTotal);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, checkoutFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void removeCartItem(int position) {
        if (position < 0 || position >= cartEntries.size()) {
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        CartAdapter.CartEntry item = cartEntries.get(position);
        if (item == null || item.getProductId() == null || item.getProductId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Không thể xóa sản phẩm này.", Toast.LENGTH_SHORT).show();
            return;
        }

        String selectedColor = item.getSelectedColor() != null ? item.getSelectedColor() : "";
        String docId = userId + "_" + item.getProductId() + "_" + selectedColor;

        db.collection("carts").document(docId).delete()
                .addOnSuccessListener(unused -> {
                    cartEntries.remove(position);
                    cartAdapter.notifyItemRemoved(position);
                    if (position < cartEntries.size()) {
                        cartAdapter.notifyItemRangeChanged(position, cartEntries.size() - position);
                    }
                    updateCartSummary();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Không thể xóa khỏi giỏ hàng: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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

    private void navigateToProductDetail(String productId) {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

        if (viewPager != null) {
            viewPager.setVisibility(View.GONE);
        }
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.VISIBLE);
        }

        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(productId);
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }


    private void showBottomNavigation() {
        if (getActivity() == null) return;
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            bottomNav.setVisibility(View.VISIBLE);
        }
    }
}