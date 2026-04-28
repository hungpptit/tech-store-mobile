package com.example.tech_store_mobile.notifications;

import androidx.annotation.NonNull;

import com.example.tech_store_mobile.utils.FcmTokenSyncHelper;
import com.google.firebase.messaging.FirebaseMessagingService;

public class TechStoreMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        FcmTokenSyncHelper.saveTokenForCurrentUser(token);
    }
}


