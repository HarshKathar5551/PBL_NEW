package com.example.upiapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class SendMoneyActivity extends AppCompatActivity {

    private EditText editReceiverUpiId, editAmount, editMessage;
    private AutoCompleteTextView dropdownCategory;
    private Button btnPay, btnScanQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        // 🔹 Bind Views
        editReceiverUpiId = findViewById(R.id.edit_receiver_upi_id);
        editAmount = findViewById(R.id.edit_amount);
        editMessage = findViewById(R.id.edit_message);
        dropdownCategory = findViewById(R.id.dropdown_category);
        btnPay = findViewById(R.id.btn_pay);
        btnScanQr = findViewById(R.id.btn_scan_qr);

        setupCategoryDropdown();

        btnScanQr.setOnClickListener(v -> startQrScanner());
        btnPay.setOnClickListener(v -> initiatePaymentFlow());

        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());
    }

    // 🔹 SETUP CATEGORY DROPDOWN
    private void setupCategoryDropdown() {
        String[] categories = getResources().getStringArray(R.array.payment_categories);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        dropdownCategory.setAdapter(adapter);
        dropdownCategory.setThreshold(1);
    }

    // 🔹 START QR SCANNER
    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan UPI QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureActivityPortrait.class);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);

        qrScannerLauncher.launch(options);
    }

    // 🔹 QR RESULT HANDLER
    private final ActivityResultLauncher<ScanOptions> qrScannerLauncher =
            registerForActivityResult(new ScanContract(), result -> {

                if (result.getContents() == null) {
                    Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                String scannedText = result.getContents().trim();

                if (scannedText.startsWith("UPI:")) {
                    try {
                        editReceiverUpiId.setText(extractUpiId(scannedText));
                        Toast.makeText(this, "UPI ID detected", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Toast.makeText(this, "Invalid UPI QR format", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Invalid QR Code", Toast.LENGTH_SHORT).show();
                }
            });

    // 🔹 PARSE UPI ID FROM QR
    private String extractUpiId(String qrText) {
        qrText = qrText.replace("UPI:", "").trim();
        String[] parts = qrText.split(";");

        for (String part : parts) {
            if (part.startsWith("ID=")) {
                return part.replace("ID=", "").trim();
            }
        }
        throw new IllegalArgumentException("UPI ID not found");
    }

    // 🔹 PAYMENT VALIDATION + FLOW
    private void initiatePaymentFlow() {
        String receiverId = editReceiverUpiId.getText().toString().trim();
        String amountStr = editAmount.getText().toString().trim();
        String message = editMessage.getText().toString().trim();
        String category = dropdownCategory.getText().toString().trim();

        // ✅ Mandatory checks
        if (receiverId.isEmpty()) {
            Toast.makeText(this, "Receiver UPI ID required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Amount required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a payment category", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔹 Move to PIN confirmation
        Intent intent = new Intent(this, ConfirmPinActivity.class);
        intent.putExtra("RECEIVER_ID", receiverId);
        intent.putExtra("AMOUNT", amount);
        intent.putExtra("MESSAGE", message);
        intent.putExtra("CATEGORY", category); // 🔥 IMPORTANT
        intent.putExtra("IS_DEV_MODE", false);

        startActivity(intent);
    }
}
