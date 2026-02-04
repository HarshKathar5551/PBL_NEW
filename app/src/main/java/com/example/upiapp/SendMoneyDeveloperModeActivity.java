package com.example.upiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.upiapp.models.TransferRequest;
import com.example.upiapp.models.TransferResponse;
import com.example.upiapp.service.ApiService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SendMoneyDeveloperModeActivity extends AppCompatActivity {

    private EditText editJsonArea;
    private Button btnInitiatePayment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money_developer_mode);

        editJsonArea = findViewById(R.id.edit_dev_json_area);
        btnInitiatePayment = findViewById(R.id.btn_dev_initiate_payment);

        // 1. Populate the text area with a sample request based on the API Contract
        populateSampleJson();

        btnInitiatePayment.setOnClickListener(v -> sendRawJsonRequest());


        ImageButton btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Animation
                v.animate()
                        .scaleX(0.85f)
                        .scaleY(0.85f)
                        .setDuration(100)
                        .withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                v.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(100)
                                        .start();

                                finish(); // Just closes current activity
                            }
                        })
                        .start();
            }
        });
    }

    private void populateSampleJson() {
        // Creating a sample object to convert to JSON string
        TransferRequest sample = new TransferRequest();
        sample.toUpi = "userB@mockupi"; // [cite: 56]
        sample.amount = 500;            // [cite: 57]
        sample.pin = "1234";            // [cite: 58]
        sample.transactionType = "TRANSFER"; // [cite: 59]

        sample.device = new TransferRequest.Device();
        sample.device.deviceId = "DEVICE_A"; // [cite: 61]
        sample.device.deviceType = "ANDROID"; // [cite: 62]

        sample.location = new TransferRequest.Location();
        sample.location.city = "Mumbai";      // [cite: 65]
        sample.location.country = "IN";       // [cite: 66]

        // Format JSON with indentation for readability
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        editJsonArea.setText(gson.toJson(sample));
    }

    private void sendRawJsonRequest() {
        String rawJson = editJsonArea.getText().toString().trim();

        if (rawJson.isEmpty()) {
            Toast.makeText(this, "JSON cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // 2. Parse the raw JSON string back into a TransferRequest object
            Gson gson = new Gson();
            TransferRequest request = gson.fromJson(rawJson, TransferRequest.class);

            // 3. Send to Backend via the standard ApiClient
            ApiService apiService = ApiClient.getClient(this);
            apiService.transfer(request).enqueue(new Callback<TransferResponse>() {
                @Override
                public void onResponse(Call<TransferResponse> call, Response<TransferResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        TransferResponse res = response.body(); // [cite: 70-75]

                        // Redirect to Result Screen
                        Intent intent = new Intent(SendMoneyDeveloperModeActivity.this, ResultActivity.class);
                        intent.putExtra("TRANSACTION_ID", res.transactionId); // [cite: 71]
                        intent.putExtra("TRANSACTION_STATUS", res.status);   // [cite: 72]
                        intent.putExtra("TRANSACTION_RISK", res.riskScore);  // [cite: 73]
                        intent.putExtra("TRANSACTION_REASON", res.message);  // [cite: 74]
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(SendMoneyDeveloperModeActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<TransferResponse> call, Throwable t) {
                    Log.e("DEV_MODE", "API Failure", t);
                    Toast.makeText(SendMoneyDeveloperModeActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            // Handle invalid JSON syntax
            Toast.makeText(this, "Invalid JSON format: " + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("DEV_MODE", "JSON Parse Error", e);
        }
    }
}