package com.example.tech_store_mobile.utils;

import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.example.tech_store_mobile.MainActivity;
import com.example.tech_store_mobile.R;

public final class MainNavigationHelper {

    private MainNavigationHelper() {
    }

    public static void navigateBackToHome(Fragment fragment) {
        if (fragment == null || !fragment.isAdded()) {
            return;
        }

        FragmentActivity activity = fragment.requireActivity();

        if (fragment.getParentFragmentManager().getBackStackEntryCount() > 0) {
            fragment.getParentFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            return;
        }

        View viewPagerView = activity.findViewById(R.id.view_pager);
        if (viewPagerView instanceof ViewPager) {
            ((ViewPager) viewPagerView).setCurrentItem(0);
            viewPagerView.setVisibility(View.VISIBLE);
        } else if (viewPagerView != null) {
            viewPagerView.setVisibility(View.VISIBLE);
        }

        View fragmentContainer = activity.findViewById(R.id.fragment_container);
        if (fragmentContainer != null) {
            fragmentContainer.setVisibility(View.GONE);
        }

        if (activity instanceof MainActivity) {
            ((MainActivity) activity).syncBottomNavigationVisibility();
        }
    }
}

