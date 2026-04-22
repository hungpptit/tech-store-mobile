package com.example.tech_store_mobile.utils;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.tech_store_mobile.R;
import com.example.tech_store_mobile.auth.LoginActivity;

public final class AuthUiHelper {

    private AuthUiHelper() {
    }


    public static void showLoginDialog(Fragment fragment, int titleRes, int messageRes) {
        if (fragment == null || fragment.getContext() == null) {
            return;
        }

        Context context = fragment.requireContext();
        new AlertDialog.Builder(context)
                .setTitle(titleRes)
                .setMessage(messageRes)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.guest_state_action, (dialog, which) -> openLogin(fragment))
                .show();
    }

    public static void openLogin(Fragment fragment) {
        if (fragment == null || fragment.getContext() == null) {
            return;
        }
        Intent intent = new Intent(fragment.requireContext(), LoginActivity.class);
        fragment.startActivity(intent);
    }
}


