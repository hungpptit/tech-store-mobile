package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

// Notification badge is managed centrally by MainActivity
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;

public class HelpCenterFragment extends Fragment {
    private NotificationBadgeManager.BadgeListener badgeListener;
    private TextView notificationBadgeView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_center, container, false);

        view.findViewById(R.id.btn_back_help_center)
                .setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }

        bindHelpCard(view.findViewById(R.id.item_customer_service), R.drawable.headphones, R.string.help_center_customer_service,
                true);
        bindHelpCard(view.findViewById(R.id.item_website), R.drawable.search_home, R.string.help_center_website);
        bindHelpCard(view.findViewById(R.id.item_facebook), R.drawable.user, R.string.help_center_facebook);

        return view;
    }

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

    private void bindHelpCard(View cardView, int iconRes, int titleRes) {
        bindHelpCard(cardView, iconRes, titleRes, false);
    }

    private void bindHelpCard(View cardView, int iconRes, int titleRes, boolean openChat) {
        if (cardView == null) {
            return;
        }

        ImageView icon = cardView.findViewById(R.id.img_help_icon);
        TextView title = cardView.findViewById(R.id.tv_help_title);

        if (icon != null) {
            icon.setImageResource(iconRes);
        }
        if (title != null) {
            title.setText(titleRes);
        }

        cardView.setOnClickListener(v -> {
            if (openChat) {
                replaceFragment(new CustomerServiceFragment());
            } else if (titleRes == R.string.help_center_facebook) {
                try {
                    android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.facebook.com/tuanhung.pham.9678"));
                    startActivity(intent);
                } catch (Exception e) {
                    showComingSoon();
                }
            } else {
                showComingSoon();
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        if (!isAdded() || getActivity() == null) {
            return;
        }

        View container = requireActivity().findViewById(R.id.fragment_container);
        View viewPager = requireActivity().findViewById(R.id.view_pager);
        View bottomNav = requireActivity().findViewById(R.id.bottom_navigation);

        if (container != null && viewPager != null && bottomNav != null) {
            container.setVisibility(View.VISIBLE);
            viewPager.setVisibility(View.GONE);
            bottomNav.setVisibility(View.GONE);
        }

        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private void showComingSoon() {
        if (getContext() == null) {
            return;
        }
        Toast.makeText(requireContext(), R.string.auth_feature_coming_soon, Toast.LENGTH_SHORT).show();
    }
}

