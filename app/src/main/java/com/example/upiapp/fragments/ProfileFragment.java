package com.example.upiapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.upiapp.ApiClient;
import com.example.upiapp.LoginActivity;
import com.example.upiapp.R;
import com.example.upiapp.SetPinActivity;
import com.example.upiapp.SecretDeveloperActivity;
import com.example.upiapp.models.ProfileResponse;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.SecurePrefManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private SecurePrefManager prefManager;
    private Button btnLogout;
    private Button btnChangeUpiPin;
    private TextView textUsername, textPhoneNumber, textUpiId;
    private View cardDeveloperMode;
    private SwitchMaterial switchDeveloperMode;

    // Secret Tap Logic
    private static final int REQUIRED_TAPS = 7;
    private static final long RESET_TIME_MS = 1000;
    private int tapCount = 0;
    private long lastTapTime = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);



    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SecurePrefManager
        prefManager = new SecurePrefManager(getActivity());

        // 1. Initialize UI components
        textUsername = view.findViewById(R.id.text_username);
        textPhoneNumber = view.findViewById(R.id.text_phone_number);
        textUpiId = view.findViewById(R.id.text_upi_id);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnChangeUpiPin = view.findViewById(R.id.btn_change_upi_pin);

        // Find Developer Card (Parent of Parent of Switch) and the Switch itself
        switchDeveloperMode = view.findViewById(R.id.switch_developer_mode);
        cardDeveloperMode = (View) switchDeveloperMode.getParent().getParent();

        // 2. Sync UI with Persisted Developer Mode Preference
        boolean isDevModeActive = prefManager.isDeveloperModeEnabled();
        if (isDevModeActive) {
            cardDeveloperMode.setVisibility(View.VISIBLE);
            switchDeveloperMode.setChecked(true);
        } else {
            cardDeveloperMode.setVisibility(View.GONE);
            switchDeveloperMode.setChecked(false);
        }

        // 3. Set Listeners
        textUsername.setOnClickListener(v -> handleSecretTap());

        btnLogout.setOnClickListener(v -> performLogout());

        btnChangeUpiPin.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SetPinActivity.class);
            startActivity(intent);
        });

        // Toggle Listener to sync Switch state with SecurePrefs and Visibility
        switchDeveloperMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefManager.setDeveloperMode(isChecked);
            if (!isChecked) {
                // Hide card immediately if turned off
                cardDeveloperMode.setVisibility(View.GONE);
                Toast.makeText(getActivity(), "Developer mode disabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), "Developer mode enabled", Toast.LENGTH_SHORT).show();
            }
        });

        // 4. Fetch real-time data from Profile API [cite: 39]
        fetchProfileData();

        // --- PASTE THIS INSIDE onViewCreated ---

// 1. Initialize the Night Mode Switch from the XML ID
        com.google.android.material.switchmaterial.SwitchMaterial switchNightMode = view.findViewById(R.id.switch_night_mode);

// 2. Sync Switch state with Saved Preference on Load
// We use SecurePrefManager to check if night mode was previously enabled
        boolean isNightMode = prefManager.isNightModeEnabled(); // Ensure this method exists in SecurePrefManager
        switchNightMode.setChecked(isNightMode);

// 3. Set the Listener to change theme and save preference
        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Save the choice in SecurePrefManager
            prefManager.setNightMode(isChecked); // Ensure this method exists in SecurePrefManager

            // Apply the theme change immediately
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                android.widget.Toast.makeText(getActivity(), "Night Mode Enabled", android.widget.Toast.LENGTH_SHORT).show();
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                android.widget.Toast.makeText(getActivity(), "Night Mode Disabled", android.widget.Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void fetchProfileData() {
        // ApiClient handles the "Authorization: Bearer <JWT>" header [cite: 5]
        ApiService apiService = ApiClient.getClient(getContext());

        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ProfileResponse profile = response.body();

                    // Update UI with data from API contract [cite: 42, 43, 44]
                    textUsername.setText(profile.upiId != null ? profile.name : "N/A");
                    textPhoneNumber.setText(profile.mobile != null ? "+91 " + profile.mobile : "N/A");
                    textUpiId.setText(profile.upiId != null ? profile.upiId : "N/A");
                } else {
                    Log.e("PROFILE_ERROR", "Error code: " + response.code());
                    Toast.makeText(getActivity(), "Failed to load profile data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Log.e("PROFILE_ERROR", "Network Failure", t);
                textUsername.setText("Error loading profile");
            }
        });
    }

    private void performLogout() {
        // Clear secure preferences to reset identity [cite: 5]
        if (prefManager != null) {
            prefManager.clearData();
        }

        Toast.makeText(getActivity(), "Logged out successfully.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void handleSecretTap() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTapTime < RESET_TIME_MS) {
            tapCount++;
            if (tapCount == REQUIRED_TAPS) {
                // Unlock Developer Mode and Persist State
                prefManager.setDeveloperMode(true);
                cardDeveloperMode.setVisibility(View.VISIBLE);
                switchDeveloperMode.setChecked(true);

                Toast.makeText(getActivity(), "Developer mode unlocked!", Toast.LENGTH_SHORT).show();

                // Open Secret Activity
                startActivity(new Intent(getActivity(), SecretDeveloperActivity.class));
                tapCount = 0;
            }
        } else {
            tapCount = 1;
        }
        lastTapTime = currentTime;
    }
}