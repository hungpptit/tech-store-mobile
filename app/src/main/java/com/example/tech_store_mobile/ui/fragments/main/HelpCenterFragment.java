package com.example.tech_store_mobile.ui.fragments.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.tech_store_mobile.R;

public class HelpCenterFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_center, container, false);

        view.findViewById(R.id.btn_back_help_center)
                .setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

        view.findViewById(R.id.btn_notification_help_center)
                .setOnClickListener(v -> showComingSoon());

        view.findViewById(R.id.btn_help_floating)
                .setOnClickListener(v -> showComingSoon());

        bindHelpCard(view.findViewById(R.id.item_customer_service), R.drawable.headphones, R.string.help_center_customer_service,
                true);
        bindHelpCard(view.findViewById(R.id.item_website), R.drawable.search_home, R.string.help_center_website);
        bindHelpCard(view.findViewById(R.id.item_facebook), R.drawable.user, R.string.help_center_facebook);

        return view;
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

