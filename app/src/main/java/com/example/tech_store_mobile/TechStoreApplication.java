package com.example.tech_store_mobile;

import android.app.Application;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class TechStoreApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dh2p3vkt4");
        config.put("api_key", "684494175143643");
        config.put("api_secret", "yq5mn1YCHgA068vx76fVw6qEEog");
        config.put("secure", "true");
        MediaManager.init(this, config);
    }
}
