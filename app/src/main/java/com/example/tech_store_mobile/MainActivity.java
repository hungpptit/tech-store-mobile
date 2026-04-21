package com.example.tech_store_mobile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.tech_store_mobile.adapters.ViewpagerAdapter;
import com.example.tech_store_mobile.ui.fragments.main.HomeFragment;
import com.example.tech_store_mobile.utils.FirebaseConfig;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;
    private long lastBackPressedTime = 0;
    private static final long BACK_PRESS_INTERVAL = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 Cấu hình Firebase
        FirebaseConfig.configureFirestore();

        // 🔥 Khởi tạo Firebase data lần đầu tiên
        // DISABLED: Bạn đã có data rồi, không cần auto-create
        // initializeFirebaseDataIfNeeded();

        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1. Thiết lập Adapter cho ViewPager
        ViewpagerAdapter adapter = new ViewpagerAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(adapter);

        // 2. Khi bấm vào Menu -> Chuyển trang ViewPager
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            // Pop all fragments from back stack first (để quay về ViewPager)
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                Log.d(TAG, "   Popping " + getSupportFragmentManager().getBackStackEntryCount() + " fragments from back stack");
                getSupportFragmentManager().popBackStack(null, getSupportFragmentManager().POP_BACK_STACK_INCLUSIVE);
            }

            // Show ViewPager again and hide fragment container
            mViewPager.setVisibility(View.VISIBLE);
            findViewById(R.id.fragment_container).setVisibility(View.GONE);

            // Switch ViewPager page
            if (id == R.id.nav_home) mViewPager.setCurrentItem(0);
            else if (id == R.id.nav_search) mViewPager.setCurrentItem(1);
            else if (id == R.id.nav_saved) mViewPager.setCurrentItem(2);
            else if (id == R.id.nav_cart) mViewPager.setCurrentItem(3);
            else if (id == R.id.nav_account) mViewPager.setCurrentItem(4);
            return true;
        });

        // 3. Khi vuốt ViewPager -> Cập nhật icon Menu tương ứng
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0: mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true); break;
                    case 1: mBottomNavigationView.getMenu().findItem(R.id.nav_search).setChecked(true); break;
                    case 2: mBottomNavigationView.getMenu().findItem(R.id.nav_saved).setChecked(true); break;
                    case 3: mBottomNavigationView.getMenu().findItem(R.id.nav_cart).setChecked(true); break;
                    case 4: mBottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true); break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // 4. Listen for back stack changes to reload HomeFragment when returning
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Log.d(TAG, "🔄 Back stack changed - back stack count: " + getSupportFragmentManager().getBackStackEntryCount());

            if (mBottomNavigationView != null) {
                mBottomNavigationView.setVisibility(getSupportFragmentManager().getBackStackEntryCount() == 0
                        ? View.VISIBLE
                        : View.GONE);
            }

            // Khi back stack trống (tất cả fragments đã pop), reload HomeFragment
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                Log.d(TAG, "   All fragments popped - reloading HomeFragment data");

                // Show ViewPager again
                mViewPager.setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_container).setVisibility(View.GONE);

                // Trigger HomeFragment reload by finding it and calling reload
                HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.view_pager + ":0");
                if (homeFragment != null && homeFragment.isVisible()) {
                    Log.d(TAG, "   HomeFragment found and visible - reloading data");
                    homeFragment.reloadData();
                }
            }
        });
<<<<<<< HEAD

        // 5. Handle back press using AndroidX OnBackPressedDispatcher
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBackPress();
            }
        });
=======
//
//        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
//            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
//                // Hiện lại mọi thứ khi quay về màn hình chính
//                findViewById(R.id.fragment_container).setVisibility(View.GONE);
//                findViewById(R.id.view_pager).setVisibility(View.VISIBLE);
//                findViewById(R.id.bottom_navigation).setVisibility(View.VISIBLE); // HIỆN LẠI THANH MENU
//            }
//        });
>>>>>>> origin/QuyCoding
    }

    // Handle back press logic
    private void handleBackPress() {
        // Check xem có fragments trong back stack không
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            Log.d(TAG, "🔙 Back pressed - back stack not empty, popping fragment");
            getSupportFragmentManager().popBackStack();
        } else {
            // Back stack trống, check xem có ở Home tab không
            int currentPage = mViewPager.getCurrentItem();
            if (currentPage != 0) {
                // Không ở Home, chuyển về Home
                Log.d(TAG, "🔙 Back pressed - not on Home page, switching to Home (page 0)");
                mViewPager.setCurrentItem(0);
                mBottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
                lastBackPressedTime = 0; // Reset timer
            } else {
                // Ở Home tab
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastBackPressedTime < BACK_PRESS_INTERVAL) {
                    // Back 2 lần trong 2 giây, thoát app
                    Log.d(TAG, "🔙 Back pressed twice - exiting app");
                    getOnBackPressedDispatcher().onBackPressed();
                } else {
                    // Back lần 1, show toast
                    Log.d(TAG, "🔙 Back pressed once on Home - press again to exit");
                    Toast.makeText(this, "Bấm lại để thoát", Toast.LENGTH_SHORT).show();
                    lastBackPressedTime = currentTime;
                }
            }
        }
    }

    /**
     * Khởi tạo Firebase data lần đầu tiên
     */
//    private void initializeFirebaseDataIfNeeded() {
//        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
//
//        // Development mode: LUÔN khởi tạo dữ liệu mới
//        // Sau này khi deploy, hãy thay đổi thành:
//        // boolean isDataInitialized = prefs.getBoolean("firebase_data_initialized", false);
////        boolean isDataInitialized = false; // DEBUG: Set to false để luôn reinitialize
//
////        if (!isDataInitialized) {
////            FirebaseInitializer.initializeAllData();
////            prefs.edit().putBoolean("firebase_data_initialized", true).apply();
////        }
//    }
}