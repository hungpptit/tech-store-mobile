package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.utils.AuthManager;
import com.example.tech_store_mobile.utils.AuthUiHelper;
import com.example.tech_store_mobile.utils.MainNavigationHelper;
import com.example.tech_store_mobile.utils.NotificationBadgeManager;
import com.example.tech_store_mobile.utils.NotificationBadgeUtils;
import com.google.android.material.button.MaterialButton;

public class AccountFragment extends Fragment {

    private View accountContentContainer;
    private View accountGuestState;
    private TextView notificationBadgeView;
    private NotificationBadgeManager.BadgeListener badgeListener;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView btnBack = view.findViewById(R.id.btn_back_search);
        accountContentContainer = view.findViewById(R.id.accountContentContainer);
        accountGuestState = view.findViewById(R.id.accountGuestState);
        MaterialButton btnAccountSignIn = view.findViewById(R.id.btnAccountSignIn);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> navigateBack());
        }

        // Setup notification button
        ImageView btnNotification = view.findViewById(R.id.btn_notification);
        if (btnNotification != null) {
            notificationBadgeView = NotificationBadgeUtils.attachBadgeToImageView(btnNotification, requireContext());
            btnNotification.setOnClickListener(v -> navigateToNotifications());
        }

        if (btnAccountSignIn != null) {
            btnAccountSignIn.setOnClickListener(v -> AuthUiHelper.openLogin(this));
        }

        // 1. My Orders
        setupMenuItem(view.findViewById(R.id.item_my_orders), "My Orders", R.drawable.box);

        // 2. My Details
        setupMenuItem(view.findViewById(R.id.item_my_details), "My Details", R.drawable.details);

        // 3. Address Book
        View addressView = view.findViewById(R.id.item_address_book);
        setupMenuItem(addressView, "Address", R.drawable.address);
        if (addressView != null) {
            addressView.setOnClickListener(v -> replaceFragment(new AddressFragment()));
        }

        // 4. Payment Methods
        View paymentView = view.findViewById(R.id.item_payment_methods);
        setupMenuItem(paymentView, "Payment Methods", R.drawable.card_1_black);
        paymentView.setOnClickListener(v -> replaceFragment(new PaymentMethodFragment()));

        // 5. Help Center
        View helpCenterView = view.findViewById(R.id.item_help_center);
        setupMenuItem(helpCenterView, "Help Center", R.drawable.headphones);
        if (helpCenterView != null) {
            helpCenterView.setOnClickListener(v -> replaceFragment(new HelpCenterFragment()));
        }

        // 6. Logout
        View logoutView = view.findViewById(R.id.item_logout);
        setupMenuItem(logoutView, "Logout", R.drawable.logout);
        if (logoutView != null) {
            logoutView.setOnClickListener(v -> AuthManager.logoutSafely(() -> {
                if (!isAdded()) {
                    return;
                }
                renderAuthState();
                Toast.makeText(requireContext(), R.string.auth_logout_success, Toast.LENGTH_SHORT).show();
            }));

            TextView tvLogout = logoutView.findViewById(R.id.tv_menu_title);
            if (tvLogout != null) {
                tvLogout.setTextColor(android.graphics.Color.RED);
            }

            ImageView imgLogout = logoutView.findViewById(R.id.img_menu_icon);
            if (imgLogout != null) {
                imgLogout.setColorFilter(android.graphics.Color.RED);
            }
        }

        renderAuthState();
    }

    @Override
    public void onResume() {
        super.onResume();
        renderAuthState();
        startNotificationBadgeListener();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopNotificationBadgeListener();
    }

    private void setupMenuItem(View itemView, String title, int iconRes) {
        if (itemView != null) {
            TextView tvTitle = itemView.findViewById(R.id.tv_menu_title);
            ImageView imgIcon = itemView.findViewById(R.id.img_menu_icon);
            if (tvTitle != null) {
                tvTitle.setText(title);
            }
            if (imgIcon != null) {
                imgIcon.setImageResource(iconRes);
            }
        }
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
        NotificationsFragment fragment = new NotificationsFragment();
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void renderAuthState() {
        boolean loggedIn = AuthManager.isLoggedIn();

        if (accountContentContainer != null) {
            accountContentContainer.setVisibility(loggedIn ? View.VISIBLE : View.GONE);
        }
        if (accountGuestState != null) {
            accountGuestState.setVisibility(loggedIn ? View.GONE : View.VISIBLE);
        }
    }

    private void navigateBack() {
        MainNavigationHelper.navigateBackToHome(this);
    }

}