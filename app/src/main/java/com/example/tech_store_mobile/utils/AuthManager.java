package com.example.tech_store_mobile.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public final class AuthManager {

    private AuthManager() {
    }

    public static FirebaseAuth getAuth() {
        return FirebaseAuth.getInstance();
    }

    public static FirebaseUser getCurrentUser() {
        return getAuth().getCurrentUser();
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    public static String getCurrentUid() {
        FirebaseUser user = getCurrentUser();
        return user != null ? user.getUid() : null;
    }

    public static void signOut() {
        getAuth().signOut();
    }

    public static void logoutSafely(Runnable onComplete) {
        Runnable safeCallback = onComplete != null ? onComplete : () -> { };

        if (!isLoggedIn()) {
            signOut();
            safeCallback.run();
            return;
        }

        FcmTokenSyncHelper.deactivateCurrentToken(() -> {
            signOut();
            safeCallback.run();
        });
    }
}

