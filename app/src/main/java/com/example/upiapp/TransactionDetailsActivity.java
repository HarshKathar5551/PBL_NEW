package com.example.upiapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class TransactionDetailsActivity extends AppCompatActivity {

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_details);

        // Initialize UI Components
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnShare = findViewById(R.id.btn_share);

        TextView textStatus = findViewById(R.id.text_detail_status);
        ImageView imageStatusIcon = findViewById(R.id.image_status_icon);
        TextView textSender = findViewById(R.id.text_detail_sender);
        TextView textReceiver = findViewById(R.id.text_detail_receiver);
        TextView textAmount = findViewById(R.id.text_detail_amount);
        TextView textMessage = findViewById(R.id.text_detail_message);
        TextView textCategory = findViewById(R.id.text_detail_category);

        // Get data from Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String status = extras.getString("STATUS", "UNKNOWN");
            String sender = extras.getString("SENDER", "N/A");
            String receiver = extras.getString("RECEIVER", "N/A");
            String amount = extras.getString("AMOUNT", "0.0");
            String message = extras.getString("MESSAGE", "No message provided.");
            String category = extras.getString("CATEGORY", "OTHERS");
            boolean isCredit = extras.getBoolean("IS_CREDIT", false);

            // Populate UI
            textStatus.setText(status.toUpperCase());
            textReceiver.setText(receiver);
            textSender.setText(sender);
            textAmount.setText(String.format("₹ %s", amount));
            textMessage.setText(message);
            textCategory.setText(category);

            // Set color for Status Text and Icon
            int statusColor;
            if (status.equalsIgnoreCase("SUCCESS") || status.equalsIgnoreCase("APPROVED")) {
                statusColor = Color.parseColor("#10B981"); // Green
            } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("FLAGGED")) {
                statusColor = Color.parseColor("#FF9800"); // Orange
            } else { // FAILURE, BLOCKED or UNKNOWN
                statusColor = Color.parseColor("#DC2626"); // Red
            }

            textStatus.setTextColor(statusColor);
            if (imageStatusIcon != null) {
                imageStatusIcon.setColorFilter(statusColor);
            }

            // Set color for Amount Text
            int amountColor;
            if (status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("FLAGGED") || status.equalsIgnoreCase("FAILURE")) {
                amountColor = Color.parseColor("#DC2626"); // Red
            } else if (isCredit) {
                amountColor = Color.parseColor("#10B981"); // Green for credited
            } else {
                amountColor = Color.parseColor("#6B7280"); // Gray for sent
            }
            textAmount.setTextColor(amountColor);
        }

        // Back Button Listener
        btnBack.setOnClickListener(v -> finish());

        // Share Button Listener
        btnShare.setOnClickListener(v -> shareTransactionDetails());
    }

    private void shareTransactionDetails() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "transaction_receipt.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, "com.example.upiapp.fileprovider", file);
            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                shareIntent.putExtra(Intent.EXTRA_TEXT, "Transaction Receipt from UPI App");
                shareIntent.setType("image/png");
                startActivity(Intent.createChooser(shareIntent, "Share Receipt via"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share receipt", Toast.LENGTH_SHORT).show();
        }
    }
}
