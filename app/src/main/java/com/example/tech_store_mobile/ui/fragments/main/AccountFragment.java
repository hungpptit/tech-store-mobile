package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.tech_store_mobile.R;

public class AccountFragment extends Fragment {

    public AccountFragment() {
        // Required empty public constructor
    }

    // --- BƯỚC QUAN TRỌNG NHẤT BỊ THIẾU ---
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Nạp file fragment_account.xml vào fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. My Orders
        setupMenuItem(view.findViewById(R.id.item_my_orders), "My Orders", R.drawable.box);

        // 2. My Details
        setupMenuItem(view.findViewById(R.id.item_my_details), "My Details", R.drawable.details);

        // 3. Address Book
        setupMenuItem(view.findViewById(R.id.item_address_book), "Address", R.drawable.address);

        // 4. Payment Methods
        setupMenuItem(view.findViewById(R.id.item_payment_methods), "Payment Methods", R.drawable.card);

        // 5. Help Center
        setupMenuItem(view.findViewById(R.id.item_help_center), "Help Center", R.drawable.headphones);

        // 6. Logout
        View logoutView = view.findViewById(R.id.item_logout);
        setupMenuItem(logoutView, "Logout", R.drawable.logout);

        // Chỉnh màu chữ Logout sang đỏ
        TextView tvLogout = logoutView.findViewById(R.id.tv_menu_title);
        tvLogout.setTextColor(android.graphics.Color.RED);

        // Đổi màu icon Logout sang đỏ (nếu là ảnh vector)
        ImageView imgLogout = logoutView.findViewById(R.id.img_menu_icon);
        imgLogout.setColorFilter(android.graphics.Color.RED);
    }

    private void setupMenuItem(View itemView, String title, int iconRes) {
        if (itemView != null) {
            TextView tvTitle = itemView.findViewById(R.id.tv_menu_title);
            ImageView imgIcon = itemView.findViewById(R.id.img_menu_icon);
            tvTitle.setText(title);
            imgIcon.setImageResource(iconRes);
        }
    }
}