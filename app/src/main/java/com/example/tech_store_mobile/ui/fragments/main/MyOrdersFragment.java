package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.tech_store_mobile.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MyOrdersFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public MyOrdersFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btn_back_orders);
        btnBack.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());

        tabLayout = view.findViewById(R.id.tab_layout_orders);
        viewPager = view.findViewById(R.id.view_pager_orders);

        setupViewPager();
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                if (position == 0) {
                    return OrdersListFragment.newInstance(true); // Ongoing
                } else {
                    return OrdersListFragment.newInstance(false); // Completed
                }
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            if (position == 0) {
                tab.setText("Ongoing");
            } else {
                tab.setText("Completed");
            }
        }).attach();
    }
}
