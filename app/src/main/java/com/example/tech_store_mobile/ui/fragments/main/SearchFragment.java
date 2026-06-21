package com.example.tech_store_mobile.ui.fragments.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.ListView;
import android.widget.AdapterView;

import com.example.tech_store_mobile.Model.Product;
import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.adapters.SearchAdapter;
import com.example.tech_store_mobile.adapters.RecentSearchAdapter;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText edtSearchActive;
    private LinearLayout layoutRecentSearches, layoutEmptySearch;
    private ListView rvRecentSearches, rvSearchResults;
    private TextView tvClearAll;
    private ImageView btnBackSearch;

    // Data & Adapters
    private FirebaseFirestore db;
    private SearchAdapter searchAdapter;
    private RecentSearchAdapter historyAdapter;

    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResultList = new ArrayList<>();
    private List<String> historyList = new ArrayList<>();
    private TextView notificationBadgeView;
    private NotificationBadgeManager.BadgeListener badgeListener;

    private static final String PREFS_NAME = "search_history_prefs";
    private static final String KEY_HISTORY = "recent_searches";

    public SearchFragment() {}

    @Override
    public void onResume() {
        super.onResume();
        startNotificationBadgeListener();
    }

    @Override
    public void onPause() {
        super.onPause();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        initViews(view);
        loadSearchHistory();
        loadAllProductsFromServer();
        setupSearchLogic();

        btnBackSearch.setOnClickListener(v -> {
            // Xóa nội dung search
            edtSearchActive.setText("");

            // Quay về Home
            androidx.viewpager.widget.ViewPager viewPager = requireActivity().findViewById(R.id.view_pager);
            if (viewPager != null) {
                viewPager.setCurrentItem(0);
            }
        });

        // Setup notification button
        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }

        tvClearAll.setOnClickListener(v -> {
            historyList.clear();
            saveSearchHistoryToDisk();
            layoutRecentSearches.setVisibility(View.GONE);
        });
    }

    private void initViews(View view) {
        btnBackSearch = view.findViewById(R.id.btn_back_search);
        edtSearchActive = view.findViewById(R.id.edt_search_active);
        layoutRecentSearches = view.findViewById(R.id.layout_recent_searches);
        layoutEmptySearch = view.findViewById(R.id.layout_empty_search);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tvClearAll = view.findViewById(R.id.tv_clear_all);

        // 1. Setup Adapter Kết quả
        searchAdapter = new SearchAdapter(getContext(), searchResultList, product -> {
            if (product == null || TextUtils.isEmpty(product.getProductId())) {
                Toast.makeText(getContext(), "Lỗi: Không tìm thấy ID sản phẩm", Toast.LENGTH_SHORT).show();
                return;
            }

            // Lưu từ khóa hiện tại vào lịch sử
            addToHistory(edtSearchActive.getText().toString());

            // Ẩn bàn phím
            hideKeyboard();

            navigateToProductDetail(product.getProductId());
        });
        rvSearchResults.setAdapter(searchAdapter);

        // 2. Setup Adapter Lịch sử
        historyAdapter = new RecentSearchAdapter(getContext(), historyList, new RecentSearchAdapter.OnHistoryClickListener() {
            @Override
            public void onKeywordClick(String keyword) {
                edtSearchActive.setText(keyword);
                edtSearchActive.setSelection(keyword.length());
                performSmartSearch(keyword);
            }

            @Override
            public void onDeleteClick(int position) {
                historyList.remove(position);
                historyAdapter.notifyDataSetChanged();
                saveSearchHistoryToDisk();
                if (historyList.isEmpty()) layoutRecentSearches.setVisibility(View.GONE);
            }
        });
        rvRecentSearches.setAdapter(historyAdapter);

        edtSearchActive.requestFocus();
    }

    private void loadAllProductsFromServer() {
        db.collection("products").get().addOnSuccessListener(queryDocumentSnapshots -> {
            allProducts.clear();
            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                Product p = doc.toObject(Product.class);
                // GÁN ID CỦA DOCUMENT VÀO MODEL ĐỂ TRUYỀN SANG TRANG DETAIL
                p.setProductId(doc.getId());
                allProducts.add(p);
            }
            Log.d("SearchFragment", "✅ Đã tải " + allProducts.size() + " sản phẩm.");
        });
    }

    private void setupSearchLogic() {
        edtSearchActive.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showState(1); // Trạng thái: Hiện lịch sử
                } else {
                    performSmartSearch(query); // Trạng thái: Hiện kết quả lọc
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void performSmartSearch(String query) {
        String queryLower = query.toLowerCase().trim();
        searchResultList.clear();

        for (Product p : allProducts) {
            boolean matches = false;

            // 1. Kiểm tra tên sản phẩm
            String name = (p.getProductName() != null) ? p.getProductName().toLowerCase() : "";
            if (name.contains(queryLower)) matches = true;

            // 2. Kiểm tra từ khóa tìm kiếm (searchKeywords)
            if (!matches && p.getSearchKeywords() != null) {
                for (String kw : p.getSearchKeywords()) {
                    if (kw.toLowerCase().contains(queryLower)) {
                        matches = true;
                        break;
                    }
                }
            }

            // 3. Kiểm tra thương hiệu (brand)
            if (!matches && p.getBrand() != null) {
                if (p.getBrand().toLowerCase().contains(queryLower)) matches = true;
            }

            // 4. Kiểm tra mô tả (description)
            if (!matches && p.getDescription() != null) {
                if (p.getDescription().toLowerCase().contains(queryLower)) matches = true;
            }

            if (matches) {
                searchResultList.add(p);
            }
        }

        if (searchResultList.isEmpty()) showState(3); // Trạng thái: Trống
        else {
            showState(2); // Trạng thái: Có kết quả
            searchAdapter.notifyDataSetChanged();
        }
    }

    // --- LOGIC LỊCH SỬ ---
    private void addToHistory(String query) {
        String q = query.trim();
        if (q.isEmpty()) return;
        historyList.remove(q);
        historyList.add(0, q);
        if (historyList.size() > 5) historyList.remove(5);
        saveSearchHistoryToDisk();
        historyAdapter.notifyDataSetChanged();
    }

    private void saveSearchHistoryToDisk() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_HISTORY, new Gson().toJson(historyList)).apply();
    }

    private void loadSearchHistory() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_HISTORY, null);
        if (json != null) {
            Type type = new TypeToken<List<String>>(){}.getType();
            List<String> saved = new Gson().fromJson(json, type);
            historyList.clear();
            historyList.addAll(saved);
        }
    }

    // --- TRẠNG THÁI HIỂN THỊ ---
    private void showState(int state) {
        layoutRecentSearches.setVisibility(state == 1 ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(state == 2 ? View.VISIBLE : View.GONE);
        layoutEmptySearch.setVisibility(state == 3 ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        View v = getActivity().getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    private void navigateToProductDetail(String productId) {
        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View fragmentContainer = requireActivity().findViewById(R.id.fragment_container);

        if (viewPager != null) viewPager.setVisibility(View.GONE);
        if (fragmentContainer != null) fragmentContainer.setVisibility(View.VISIBLE);

        ProductDetailFragment detailFragment = ProductDetailFragment.newInstance(productId);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }
}