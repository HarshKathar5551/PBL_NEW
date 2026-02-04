package com.example.upiapp.utils;


import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.example.upiapp.models.Transaction;

public class LocalDataStore {
    private static final String PREF_NAME = "UpiDemoPrefs";
    private static final String KEY_USER_PIN = "user_secure_pin";
    private static final String KEY_LOGGED_IN = "isLoggedIn";
    private static final String KEY_TRANSACTIONS = "transactionsList";

    // --- KEYS FOR USER CREDENTIALS ---
    private static final String KEY_USERNAME = "user_username";
    private static final String KEY_PASSWORD = "user_password";
    private static final String KEY_DISPLAY_NAME = "user_display_name"; // <-- ADDED KEY
    // ---------------------------------

    // Inside com.example.upiapp.utils.LocalDataStore.java

    private static final String KEY_DEVELOPER_MODE = "is_developer_mode_enabled"; // New Key

    // Method to save the Developer Mode status
    public void setDeveloperMode(boolean isEnabled) {
        editor.putBoolean(KEY_DEVELOPER_MODE, isEnabled).apply();
    }

    // Method to retrieve the Developer Mode status
    public boolean isDeveloperModeEnabled() {
        return sharedPrefs.getBoolean(KEY_DEVELOPER_MODE, false); // Default to off
    }

    private SharedPreferences sharedPrefs;
    private SharedPreferences.Editor editor;
    private Gson gson;

    // Method to save the PIN
    public void savePin(String pin) {
        editor.putString(KEY_USER_PIN, pin);
        editor.apply();
    }

    // Method to retrieve the PIN (for future payment validation)
    public String getSavedPin() {
        return sharedPrefs.getString(KEY_USER_PIN, null);
    }

    public void saveAuthToken(String token) {
        sharedPrefs.edit().putString("AUTH_TOKEN", token).apply();
    }

    public String getAuthToken() {
        return sharedPrefs.getString("AUTH_TOKEN", null);
    }


    public LocalDataStore(Context context) {
        sharedPrefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPrefs.edit();
        gson = new Gson();
    }

    // --- Authentication ---
    public boolean isLoggedIn() {
        return sharedPrefs.getBoolean(KEY_LOGGED_IN, false);
    }

    public void setLoggedIn(boolean loggedIn) {
        editor.putBoolean(KEY_LOGGED_IN, loggedIn).apply();
    }

    public void logout() {
        editor.remove(KEY_LOGGED_IN).apply();
    }

    // --- METHODS FOR SIGNUP/LOGIN CREDENTIALS ---

    /**
     * Saves the username (Mobile No.), password, AND display name provided during signup.
     * * NOTE: This method signature was updated to include 'name'.
     */
    public void saveUserCredentials(String username, String password, String name) {
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.putString(KEY_DISPLAY_NAME, name); // <-- ADDED LOGIC TO SAVE NAME
        editor.apply();
    }

    /**
     * Retrieves the saved username (Mobile No.) for login validation.
     */
    public String getSavedUsername() {
        return sharedPrefs.getString(KEY_USERNAME, null);
    }

    /**
     * Retrieves the saved password for login validation.
     */
    public String getSavedPassword() {
        return sharedPrefs.getString(KEY_PASSWORD, null);
    }

    /**
     * Retrieves the saved display name for use in the profile fragment.
     */
    public String getSavedDisplayName() { // <-- ADDED METHOD TO RETRIEVE NAME
        // Defaults to "User" if the key is not found (e.g., if signup hasn't occurred)
        return sharedPrefs.getString(KEY_DISPLAY_NAME, "Demo User");
    }

    // ----------------------------------------------------


    // --- Fraud Engine Simulation (No change to logic) ---
//    public Transaction simulateFraudCheck(String receiverId, double amount, String message) {
//        java.util.Random random = new java.util.Random();
//        int decisionInt = random.nextInt(100);
//
//        String status;
//        int riskScore;
//        String reason;
//
//        if (decisionInt < 60) {
//            status = "APPROVED";
//            riskScore = random.nextInt(30);
//            reason = "Standard transaction pattern.";
//        } else if (decisionInt < 85) {
//            status = "FLAGGED";
//            riskScore = random.nextInt(30) + 50;
//            reason = "Unusual amount/new receiver.";
//        } else {
//            status = "BLOCKED";
//            riskScore = random.nextInt(20) + 80;
//            reason = "High velocity transaction alert.";
//        }
//
//        return new Transaction(receiverId, amount, message, status, riskScore, reason);
//    }

    // --- Local Transaction History (No change) ---

    public void saveTransaction(Transaction transaction) {
        List<Transaction> transactions = getTransactions();
        transactions.add(0, transaction); // Add to the start so the newest is first

        String json = gson.toJson(transactions);
        editor.putString(KEY_TRANSACTIONS, json).apply();
    }

    public List<Transaction> getTransactions() {
        String json = sharedPrefs.getString(KEY_TRANSACTIONS, null);
        if (json == null) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<ArrayList<Transaction>>() {}.getType();
        return gson.fromJson(json, type);
    }
}