package com.example.tech_store_mobile.ui.fragments.main;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.ProductAdapter;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.AuthUiHelper;
import com.example.tech_store_mobile.utils.MainNavigationHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

public class SavedFragment extends Fragment {

    private ImageView btnBack;
    private RecyclerView rvSaved;
    private View guestStateContainer;
    private View emptyStateContainer;
    private View btnSavedSignIn;

    private final List<Product> savedProducts = new ArrayList<>();
    private ProductAdapter savedAdapter;
    private FirebaseFirestore db;
    private final Set<String> loadedProductIds = new HashSet<>();
    private int savedLoadGeneration = 0;

    public SavedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable android.os.Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        db = FirebaseFirestore.getInstance();
        initializeViews(view);
        setupRecyclerView();
        setupAuthPrompt();
        renderAuthState();
        setupBackAction();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        renderAuthState();
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        ImageView btnNotification = view.findViewById(R.id.btnNotification);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        rvSaved = view.findViewById(R.id.rvSaved);
        guestStateContainer = view.findViewById(R.id.guestStateContainer);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);
        btnSavedSignIn = view.findViewById(R.id.btnSavedSignIn);

        tvTitle.setText(R.string.saved_title);
        if (btnNotification != null) {
            btnNotification.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        rvSaved.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSaved.setNestedScrollingEnabled(true);
        savedAdapter = new ProductAdapter(savedProducts, true);
        savedAdapter.setOnProductClickListener(product -> {
            if (product != null && product.getProductId() != null && !product.getProductId().trim().isEmpty()) {
                navigateToProductDetail(product.getProductId());
            }
        });
        savedAdapter.setOnHeartClickListener(this::removeSavedItem);
        rvSaved.setAdapter(savedAdapter);
    }

    private void setupAuthPrompt() {
        if (btnSavedSignIn != null) {
            btnSavedSignIn.setOnClickListener(v -> AuthUiHelper.openLogin(this));
        }
    }

    private void renderAuthState() {
        boolean loggedIn = AuthManager.isLoggedIn();

        if (rvSaved != null) {
            rvSaved.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        }
        if (guestStateContainer != null) {
            guestStateContainer.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }
        if (emptyStateContainer != null) {
            emptyStateContainer.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }

        if (loggedIn) {
            loadSavedItemsFromFirestore();
        } else {
            int previousSize = savedProducts.size();
            savedProducts.clear();
            if (previousSize > 0) {
                savedAdapter.notifyDataSetChanged();
            }
        }
    }

    private void loadSavedItemsFromFirestore() {
        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            savedProducts.clear();
            loadedProductIds.clear();
            savedAdapter.notifyDataSetChanged();
            return;
        }

        final int generation = ++savedLoadGeneration;

        db.collection("saved_items")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (generation != savedLoadGeneration) return;

                    savedProducts.clear();
                    loadedProductIds.clear();

                    List<QueryDocumentSnapshot> docs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        docs.add(doc);
                    }

                    if (docs.isEmpty()) {
                        savedAdapter.notifyDataSetChanged();
                        if (rvSaved != null) rvSaved.setVisibility(View.GONE);
                        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                        return;
                    }

                    loadSavedItemAtIndex(docs, 0, generation);
                })
                .addOnFailureListener(e -> {
                    savedProducts.clear();
                    savedAdapter.notifyDataSetChanged();
                    if (rvSaved != null) rvSaved.setVisibility(View.GONE);
                    if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                });
    }

    private void loadSavedItemAtIndex(List<QueryDocumentSnapshot> docs, int index, int generation) {
        if (generation != savedLoadGeneration) return;

        if (index >= docs.size()) {
            savedAdapter.notifyDataSetChanged();
            if (savedProducts.isEmpty()) {
                if (rvSaved != null) rvSaved.setVisibility(View.GONE);
                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
            } else {
                if (rvSaved != null) rvSaved.setVisibility(View.VISIBLE);
                if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
            }
            return;
        }

        QueryDocumentSnapshot doc = docs.get(index);
        String productId = doc.getString("productId");

        if (productId == null || productId.trim().isEmpty()) {
            loadSavedItemAtIndex(docs, index + 1, generation);
            return;
        }

        if (loadedProductIds.contains(productId)) {
            db.collection("saved_items").document(doc.getId()).delete();
            loadSavedItemAtIndex(docs, index + 1, generation);
            return;
        }

        db.collection("products")
                .document(productId)
                .get()
                .addOnSuccessListener(productSnapshot -> {
                    if (generation != savedLoadGeneration) return;

                    Product product = productSnapshot.toObject(Product.class);
                    if (product != null) {
                        product.setProductId(productSnapshot.getId());
                        savedProducts.add(product);
                        loadedProductIds.add(productId);
                    }
                    loadSavedItemAtIndex(docs, index + 1, generation);
                })
                .addOnFailureListener(e -> {
                    if (generation != savedLoadGeneration) return;
                    loadSavedItemAtIndex(docs, index + 1, generation);
                });
    }

    private void removeSavedItem(Product product, int position) {
        if (position < 0 || position >= savedProducts.size()) {
            return;
        }

        String userId = AuthManager.getCurrentUid();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (product == null || product.getProductId() == null || product.getProductId().trim().isEmpty()) {
            Toast.makeText(getContext(), "Không thể bỏ lưu sản phẩm này.", Toast.LENGTH_SHORT).show();
            return;
        }

        String docId = userId + "_" + product.getProductId();
        db.collection("saved_items").document(docId).delete()
                .addOnSuccessListener(unused -> {
                    if (position < savedProducts.size()) {
                        savedProducts.remove(position);
                        loadedProductIds.remove(product.getProductId());
                        savedAdapter.notifyItemRemoved(position);
                        if (position < savedProducts.size()) {
                            savedAdapter.notifyItemRangeChanged(position, savedProducts.size() - position);
                        }
                    }

                    if (savedProducts.isEmpty()) {
                        if (rvSaved != null) rvSaved.setVisibility(View.GONE);
                        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.VISIBLE);
                    } else {
                        if (rvSaved != null) rvSaved.setVisibility(View.VISIBLE);
                        if (emptyStateContainer != null) emptyStateContainer.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Không thể xóa khỏi danh sách đã lưu: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupBackAction() {
        btnBack.setOnClickListener(v -> MainNavigationHelper.navigateBackToHome(this));
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
}