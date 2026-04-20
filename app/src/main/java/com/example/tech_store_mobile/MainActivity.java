package com.example.tech_store_mobile;

import android.os.Bundle;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.example.tech_store_mobile.adapters.ViewpagerAdapter;
import com.example.tech_store_mobile.ui.fragments.main.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mViewPager = findViewById(R.id.view_pager);
        mBottomNavigationView = findViewById(R.id.bottom_navigation);

        // 1. Thiết lập Adapter cho ViewPager
        ViewpagerAdapter adapter = new ViewpagerAdapter(getSupportFragmentManager(),
                FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mViewPager.setAdapter(adapter);

        // 2. Khi bấm vào Menu -> Chuyển trang ViewPager
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
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
    }
}