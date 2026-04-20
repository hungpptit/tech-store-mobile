package com.example.tech_store_mobile.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.tech_store_mobile.ui.fragments.main.AccountFragment;
import com.example.tech_store_mobile.ui.fragments.main.CartFragment;
import com.example.tech_store_mobile.ui.fragments.main.HomeFragment;
import com.example.tech_store_mobile.ui.fragments.main.SavedFragment;
import com.example.tech_store_mobile.ui.fragments.main.SearchFragment;

public class ViewpagerAdapter extends FragmentStatePagerAdapter {

    public ViewpagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        // Trả về Fragment tương ứng với vị trí icon trên Menu
        switch (position) {
            case 0: return new HomeFragment();
            case 1: return new SearchFragment();
            case 2: return new SavedFragment();
            case 3: return new CartFragment();
            case 4: return new AccountFragment();
            default: return new HomeFragment();
        }
    }

    @Override
    public int getCount() {
        return 5; // Số lượng item trong Bottom Navigation Menu của bạn
    }
}