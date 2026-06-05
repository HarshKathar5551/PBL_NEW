package com.example.upiapp;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.upiapp.utils.SecurePrefManager;

public class UPIApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Preference Manager
        SecurePrefManager prefManager = new SecurePrefManager(this);
        
        // Apply Night Mode globally on app start
        if (prefManager.isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
