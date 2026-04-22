package com.example.tech_store_mobile.ui.fragments.main;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.ProductAdapter;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private ImageView btnBack;
    private RecyclerView rvSaved;
    private View emptyStateContainer;

    private final List<Product> savedProducts = new ArrayList<>();
    private ProductAdapter savedAdapter;

    public SavedFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable android.os.Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        initializeViews(view);
        setupRecyclerView();
        loadFakeSavedItems();
        setupBackAction();

        return view;
    }

    private void initializeViews(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        ImageView btnNotification = view.findViewById(R.id.btnNotification);
        TextView tvTitle = view.findViewById(R.id.tvTitle);
        rvSaved = view.findViewById(R.id.rvSaved);
        emptyStateContainer = view.findViewById(R.id.emptyStateContainer);

        tvTitle.setText(R.string.saved_title);
        if (btnNotification != null) {
            btnNotification.setVisibility(View.VISIBLE);
        }
    }

    private void setupRecyclerView() {
        rvSaved.setLayoutManager(new GridLayoutManager(getContext(), 2));
        rvSaved.setNestedScrollingEnabled(true);
        savedAdapter = new ProductAdapter(savedProducts, true);
        rvSaved.setAdapter(savedAdapter);
    }

    private void loadFakeSavedItems() {
        savedProducts.clear();
        savedProducts.add(new Product("saved_001", "laptop", "MacBook Pro M4", "Apple", "", 2499.00, 18.0, 2049.18, null, "", 5.0, 125L, 10L, true, true, null));
        savedProducts.add(new Product("saved_002", "audio", "AirPods Pro", "Apple", "", 249.00, 15.0, 211.65, null, "", 4.8, 98L, 32L, true, true, null));
        savedProducts.add(new Product("saved_003", "watch", "Apple Watch Series", "Apple", "", 399.00, 10.0, 359.10, null, "", 4.9, 76L, 18L, true, false, null));

        savedAdapter.notifyItemRangeInserted(0, savedProducts.size());
        updateEmptyState();
    }

    private void updateEmptyState() {
        boolean hasItems = !savedProducts.isEmpty();
        rvSaved.setVisibility(hasItems ? View.VISIBLE : View.GONE);
        emptyStateContainer.setVisibility(hasItems ? View.GONE : View.VISIBLE);
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
                }
            }
        });
    }
}