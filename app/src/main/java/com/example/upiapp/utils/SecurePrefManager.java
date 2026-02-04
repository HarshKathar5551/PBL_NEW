package com.example.upiapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey; // Use this instead of MasterKeys

import java.io.IOException;
import java.security.GeneralSecurityException;

public class SecurePrefManager {
    private static final String PREF_NAME = "secure_upi_prefs";
    private static final String KEY_TOKEN = "jwt_token";
    private SharedPreferences securePrefs;

    public SecurePrefManager(Context context) {
        try {
            // Step 1: Initialize the MasterKey using the Builder (Modern Way)
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Step 2: Initialize EncryptedSharedPreferences
            securePrefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_NAME,
                    masterKey, // Pass the MasterKey object directly
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToken(String token) {
        if (securePrefs != null) {
            securePrefs.edit().putString(KEY_TOKEN, token).apply();
        }
    }

    public String getToken() {
        return securePrefs != null ? securePrefs.getString(KEY_TOKEN, null) : null;
    }

    // Inside SecurePrefManager.java
    // Inside SecurePrefManager.java
    public void setDeveloperMode(boolean enabled) {
        securePrefs.edit().putBoolean("developer_mode_enabled", enabled).apply();
    }

    public boolean isDeveloperModeEnabled() {
        // Defaults to false as per standard hidden developer options behavior
        return securePrefs.getBoolean("developer_mode_enabled", false);
    }

    public void clearData() {
        securePrefs.edit().clear();
    }


    public void setNightMode(boolean enabled) {
        securePrefs.edit().putBoolean("night_mode_enabled", enabled).apply();
    }

    public boolean isNightModeEnabled() {
        return securePrefs.getBoolean("night_mode_enabled", false);
    }
}
