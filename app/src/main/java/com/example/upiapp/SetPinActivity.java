package com.example.upiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.upiapp.models.SetPinRequest;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.SecurePrefManager; // Use the secure manager

public class SetPinActivity extends AppCompatActivity {

    private EditText editPassword;
    private EditText editPin;
    private Button btnSetPin;
    protected SecurePrefManager prefManager; // For secure token handling

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_pin);

        // Initialize SecurePrefManager
        prefManager = new SecurePrefManager(this);

        editPassword = findViewById(R.id.edit_new_pin); // Note: Ensure IDs match your XML
        editPin = findViewById(R.id.edit_confirm_pin);
        btnSetPin = findViewById(R.id.btn_set_pin);

        btnSetPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSetPin();
            }
        });
    }

    private void attemptSetPin() {
        String password = editPassword.getText().toString().trim();
        String pin = editPin.getText().toString().trim();

        if (pin.length() != 4) {
            Toast.makeText(this, "PIN must be exactly 4 digits.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Initialize API Service using the context-aware Client
        // Passing 'this' allows the ApiClient to access SecurePrefManager and inject the JWT token
//        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        ApiService apiService = ApiClient.getClient(this);

        // 2. Prepare the Request Data: password and newPin [cite: 34, 35]
        SetPinRequest request = new SetPinRequest(password, pin);

        // 3. Execute the POST request to /auth/set-pin [cite: 31]
        apiService.setPin(request).enqueue(new retrofit2.Callback<Void>() {
            @Override
            public void onResponse(retrofit2.Call<Void> call, retrofit2.Response<Void> response) {
                if (response.isSuccessful()) { // 200 OK [cite: 37]
                    Log.d("SET_PIN", "UPI PIN updated successfully");
                    Toast.makeText(getApplicationContext(), "PIN Set Successfully", Toast.LENGTH_SHORT).show();

                    // Redirect to MainActivity only after successful API response
                    Intent intent = new Intent(SetPinActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (response.code() == 401) {
                    Log.e("SET_PIN", "Unauthorized: Token may be expired");
                    Toast.makeText(getApplicationContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                } else {
                    Log.e("SET_PIN", "Failed to set PIN. Error code: " + response.code());
                    Toast.makeText(getApplicationContext(), "Failed to update PIN", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(retrofit2.Call<Void> call, Throwable t) {
                Log.e("SET_PIN", "Network Error: ", t);
                Toast.makeText(getApplicationContext(), "Network Error. Check Server.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}