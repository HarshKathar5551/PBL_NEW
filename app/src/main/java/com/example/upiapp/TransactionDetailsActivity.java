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
        ImageButton btnShare = findViewById(R.id.btn_share); // Initialize Share Button

        TextView textStatus = findViewById(R.id.text_detail_status);
        TextView textSender = findViewById(R.id.text_detail_sender);
        TextView textReceiver = findViewById(R.id.text_detail_receiver);
        TextView textAmount = findViewById(R.id.text_detail_amount);
        TextView textMessage = findViewById(R.id.text_detail_message);
        TextView textRiskScore = findViewById(R.id.text_detail_risk_score);
        TextView textReason = findViewById(R.id.text_detail_reason);

        // Get data from Intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String status = extras.getString("STATUS", "UNKNOWN");
            String sender = extras.getString("SENDER", "N/A");
            String receiver = extras.getString("RECEIVER", "N/A");
            // Changed to getString because Adapter passes it as String for compatibility
            String amount = extras.getString("AMOUNT", "0.0");
            String message = extras.getString("MESSAGE", "No message provided.");
            String riskScore = extras.getString("RISK_SCORE", "N/A");
            String reason = extras.getString("REASON", "Data not available.");

            // Populate UI
            textStatus.setText(status);
            textReceiver.setText(receiver);
            textSender.setText(sender);
            textAmount.setText(String.format("₹ %s", amount));
            textMessage.setText(message);
            textRiskScore.setText(riskScore);
            textReason.setText(reason);

            // Set color based on status
            int color;
            if (status.equalsIgnoreCase("SUCCESS") || status.equalsIgnoreCase("APPROVED")) {
                color = Color.parseColor("#4CAF50"); // Green
            } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("FLAGGED")) {
                color = Color.parseColor("#FF9800"); // Orange
            } else { // FAILURE, BLOCKED or UNKNOWN
                color = Color.parseColor("#D81B60"); // Red
            }
            textStatus.setTextColor(color);
            textRiskScore.setTextColor(color);
        }

        // Back Button Listener with Animation
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                finish();
                            }
                        })
                        .start();
            }
        });

        // Share Button Listener
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareTransactionDetails();
            }
        });
    }

    /**
     * Captures a screenshot of the current view and opens the share intent.
     */
    private void shareTransactionDetails() {
        // 1. Capture the screenshot of the root view
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        Bitmap bitmap = Bitmap.createBitmap(rootView.getWidth(), rootView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        rootView.draw(canvas);

        try {
            // 2. Save the bitmap to the cache directory
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs(); // create directory if it doesn't exist
            File file = new File(cachePath, "transaction_receipt.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // 3. Get the URI using FileProvider
            Uri contentUri = FileProvider.getUriForFile(this, "com.example.upiapp.fileprovider", file);

            if (contentUri != null) {
                // 4. Create the Share Intent
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Important for security
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