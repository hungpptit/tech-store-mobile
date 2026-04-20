package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tech_store_mobile.R;
import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private EditText edtSearchActive;
    private LinearLayout layoutRecentSearches, layoutEmptySearch;
    private RecyclerView rvRecentSearches, rvSearchResults;
    private TextView tvClearAll;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // [cite: 1313, 1315] Nạp giao diện từ file XML fragment_search mà bạn đã gửi
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ các View từ XML dựa trên ID bạn đã đặt
        initViews(view);

        // 2. [cite: 1261] Tự động hiện bàn phím khi vừa vào màn hình
        edtSearchActive.requestFocus();

        // 3. [cite: 1317] Xử lý logic tìm kiếm khi gõ chữ (Trạng thái 1, 2, 3)
        setupSearchLogic();

        // 4. [cite: 1263] Xử lý nút Clear All lịch sử
        tvClearAll.setOnClickListener(v -> {
            // Sau này gọi DatabaseHelper để xóa sạch bảng SearchHistory tại đây
            layoutRecentSearches.setVisibility(View.GONE);
        });
    }

    private void initViews(View view) {
        edtSearchActive = view.findViewById(R.id.edt_search_active);
        layoutRecentSearches = view.findViewById(R.id.layout_recent_searches);
        layoutEmptySearch = view.findViewById(R.id.layout_empty_search);
        rvRecentSearches = view.findViewById(R.id.rv_recent_searches);
        rvSearchResults = view.findViewById(R.id.rv_search_results);
        tvClearAll = view.findViewById(R.id.tv_clear_all);

        // [cite: 1243] Kết quả tìm kiếm dạng lưới 2 cột giống trang Home
        rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));

        // [cite: 1244] Lịch sử tìm kiếm dạng danh sách dọc
        rvRecentSearches.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void setupSearchLogic() {
        // [cite: 1259] Lắng nghe thay đổi văn bản để chuyển đổi 3 màn hình
        edtSearchActive.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                if (query.isEmpty()) {
                    // [cite: 1284] TRẠNG THÁI 1: Hiện lại lịch sử tìm kiếm khi xóa hết chữ
                    layoutRecentSearches.setVisibility(View.VISIBLE);
                    rvSearchResults.setVisibility(View.GONE);
                    layoutEmptySearch.setVisibility(View.GONE);
                } else {
                    // Chuyển sang trạng thái tìm kiếm (Trạng thái 2 hoặc 3)
                    performSearch(query);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        // [cite: 1285] Ẩn vùng lịch sử khi bắt đầu nhập liệu
        layoutRecentSearches.setVisibility(View.GONE);

        // Logic giả lập: Nếu gõ "dominhkhoa" thì hiện trạng thái trống (như ảnh bạn gửi)
        if (query.equalsIgnoreCase("dominhkhoa")) {
            // [cite: 1286] TRẠNG THÁI 3: Không tìm thấy kết quả
            rvSearchResults.setVisibility(View.GONE);
            layoutEmptySearch.setVisibility(View.VISIBLE);
        } else {
            // [cite: 1285] TRẠNG THÁI 2: Hiện danh sách kết quả tìm kiếm (Active)
            layoutEmptySearch.setVisibility(View.GONE);
            rvSearchResults.setVisibility(View.VISIBLE);

            // [cite: 1321] Ở đây bạn sẽ gọi ProductAdapter để đổ dữ liệu thật vào rvSearchResults
        }
    }
}