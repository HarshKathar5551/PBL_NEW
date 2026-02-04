package com.example.upiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.upiapp.models.TransferRequest;
import com.example.upiapp.models.TransferResponse;
import com.example.upiapp.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPinActivity extends AppCompatActivity {

    private EditText editUpiPin;
    private Button btnConfirmPayment;
    private TextView textPaymentSummary;

    private String receiverId;
    private double amount; // Note: Contract uses int, ensure alignment
    private String transactionType = "TRANSFER"; // Defaulting per contract example [cite: 59]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_pin);

        editUpiPin = findViewById(R.id.edit_upi_pin);
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment);
        textPaymentSummary = findViewById(R.id.text_payment_summary);

        Intent intent = getIntent();
        receiverId = intent.getStringExtra("RECEIVER_ID");
        amount = intent.getDoubleExtra("AMOUNT", 0.0);

        textPaymentSummary.setText(String.format("Paying ₹%.2f to %s", amount, receiverId));

        btnConfirmPayment.setOnClickListener(v -> initiateFinalTransaction());


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

    private void initiateFinalTransaction() {
        String inputPin = editUpiPin.getText().toString().trim();

        if (inputPin.length() != 4) {
            Toast.makeText(this, "Please enter a 4-digit PIN.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. Build the Request Object following the Contract [cite: 53-68]
        TransferRequest request = new TransferRequest();
        request.toUpi = receiverId;
        request.amount = (int) amount; // Casting to int for contract compatibility
        request.pin = inputPin;
        request.transactionType = transactionType;

        // Mocking Device Metadata [cite: 60-63]
        request.device = new TransferRequest.Device();
        request.device.deviceId = "DEVICE_A";
        request.device.deviceType = "ANDROID";

        // Mocking Location Metadata [cite: 64-67]
        request.location = new TransferRequest.Location();
        request.location.city = "Mumbai";
        request.location.country = "IN";

        // 2. Call the API using the context-aware ApiClient
        ApiService apiService = ApiClient.getClient(this);
        apiService.transfer(request).enqueue(new Callback<TransferResponse>() {
            @Override
            public void onResponse(Call<TransferResponse> call, Response<TransferResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TransferResponse res = response.body();

                    // 3. Redirect to Result Screen with server data [cite: 70-75]
                    Intent resultIntent = new Intent(ConfirmPinActivity.this, ResultActivity.class);
                    resultIntent.putExtra("TRANSACTION_ID", res.transactionId);
                    resultIntent.putExtra("TRANSACTION_STATUS", res.status);
                    resultIntent.putExtra("TRANSACTION_RISK", res.riskScore);
                    resultIntent.putExtra("TRANSACTION_REASON", res.message);
                    startActivity(resultIntent);
                    finish();
                } else {
                    Toast.makeText(ConfirmPinActivity.this, "Transaction Failed: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransferResponse> call, Throwable t) {
                Log.e("TRANSFER", "Error", t);
                Toast.makeText(ConfirmPinActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}