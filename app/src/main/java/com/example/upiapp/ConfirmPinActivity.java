package com.example.upiapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.upiapp.models.TransferRequest;
import com.example.upiapp.models.TransferResponse;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.SecurePrefManager;
import com.example.upiapp.utils.LocalDataStore;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfirmPinActivity extends AppCompatActivity {

    private EditText editUpiPin;
    private Button btnConfirmPayment;
    private TextView textPaymentSummary;

    private String receiverId;
    private String category;
    private double amount;
    private String transactionType = "TRANSFER";

    private String pendingStatus;
    private String pendingTxnId;

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
        category = intent.getStringExtra("CATEGORY");

        textPaymentSummary.setText(
                String.format("Paying ₹%.2f to %s (%s)", amount, receiverId, category)
        );

        btnConfirmPayment.setOnClickListener(v -> initiateFinalTransaction());

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    private void initiateFinalTransaction() {
        String inputPin = editUpiPin.getText().toString().trim();

        if (inputPin.length() != 4) {
            Toast.makeText(this, "Please enter a 4-digit PIN", Toast.LENGTH_SHORT).show();
            return;
        }

        TransferRequest request = new TransferRequest();
        request.toUpi = receiverId;
        request.amount = (int) Math.round(amount);
        request.pin = inputPin;
        request.transactionType = transactionType;
        request.category = category;

        request.device = new TransferRequest.Device();
        request.device.deviceId = "DEVICE_A";
        request.device.deviceType = "ANDROID";

        request.location = new TransferRequest.Location();
        request.location.city = "Mumbai";
        request.location.country = "IN";

        ApiService apiService = ApiClient.getClient(this);

        apiService.transfer(request).enqueue(new Callback<TransferResponse>() {
            @Override
            public void onResponse(Call<TransferResponse> call, Response<TransferResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    TransferResponse res = response.body();
//
//                    // ✅ Check if status is FLAGGED or BLOCKED
//                    if ("FLAGGED".equalsIgnoreCase(res.status) || "BLOCKED".equalsIgnoreCase(res.status)) {
//                        checkPermissionAndSendSms(res.status.toUpperCase(), res.transactionId);
//                    }

                if (response.isSuccessful() && response.body() != null) {
                    TransferResponse res = response.body();

                    // ✅ Check if status is FLAGGED or BLOCKED and send SMS
                    if ("FLAGGED".equalsIgnoreCase(res.status) || "BLOCKED".equalsIgnoreCase(res.status)) {
                        checkPermissionAndSendSms(res.status.toUpperCase(), res.transactionId);
                    }

                    Intent resultIntent = new Intent(ConfirmPinActivity.this, ResultActivity.class);
                    resultIntent.putExtra("TRANSACTION_ID", res.transactionId);
                    resultIntent.putExtra("TRANSACTION_STATUS", res.status);
                    resultIntent.putExtra("TRANSACTION_RISK", res.riskScore);
                    resultIntent.putExtra("TRANSACTION_REASON", res.message);

                    startActivity(resultIntent);
                    finish();
                } else {
                    Toast.makeText(ConfirmPinActivity.this, "Transaction Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<TransferResponse> call, Throwable t) {
                Log.e("TRANSFER", "API ERROR", t);
                Toast.makeText(ConfirmPinActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkPermissionAndSendSms(String status, String txnId) {
        this.pendingStatus = status;
        this.pendingTxnId = txnId;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, 101);
        } else {
            sendSecuritySms(status, txnId);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (pendingStatus != null && pendingTxnId != null) {
                    sendSecuritySms(pendingStatus, pendingTxnId);
                }
            } else {
                Toast.makeText(this, "SMS Permission Denied. Security alert could not be sent.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendSecuritySms(String status, String txnId) {
        SecurePrefManager prefManager = new SecurePrefManager(this);
        String phoneNumber = prefManager.getMobile();

        // Fallback to LocalDataStore if not found in SecurePref (Username is stored as mobile during signup)
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            LocalDataStore dataStore = new LocalDataStore(this);
            phoneNumber = dataStore.getSavedUsername();
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e("SMS_ALERT", "Mobile number not found for current user.");
            return;
        }

        String message;
        if ("BLOCKED".equalsIgnoreCase(status)) {
            message = "⚠️ SECURITY ALERT: Your transaction of ₹" + amount + " to " + receiverId +
                    " was BLOCKED due to high fraud risk. Txn ID: " + txnId + ". If this wasn't you, contact support immediately.";
        } else if ("FLAGGED".equalsIgnoreCase(status)) {
            message = "⚠️ WARNING: A suspicious transaction of ₹" + amount + " to " + receiverId +
                    " has been FLAGGED for review. Txn ID: " + txnId + ". Monitor your account closely.";
        } else {
            return;
        }

        try {
            SmsManager smsManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                smsManager = getSystemService(SmsManager.class);
            } else {
                smsManager = SmsManager.getDefault();
            }
            
            if (smsManager != null) {
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                Log.d("SMS_ALERT", "Security SMS sent to " + phoneNumber);
                Toast.makeText(this, "Security Alert sent to your mobile: " + phoneNumber, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("SMS_ALERT", "Failed to send SMS", e);
            Toast.makeText(this, "Failed to send security SMS", Toast.LENGTH_SHORT).show();
        }
    }
}
