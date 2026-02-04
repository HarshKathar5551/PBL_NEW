package com.example.upiapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class ResultActivity extends AppCompatActivity {

    private RelativeLayout resultLayout;
    private ImageView iconStatus;
    private TextView textStatusTitle, textReason, textTransactionId;
    private MaterialButton btnDone;
    private MaterialCardView cardStatusIcon, cardTransactionDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        resultLayout = findViewById(R.id.result_layout);
        iconStatus = findViewById(R.id.icon_status);
        textStatusTitle = findViewById(R.id.text_status_title);
        textReason = findViewById(R.id.text_reason);
        textTransactionId = findViewById(R.id.text_transaction_id);
        btnDone = findViewById(R.id.btn_done);
        cardStatusIcon = findViewById(R.id.cardStatusIcon);
        cardTransactionDetails = findViewById(R.id.cardTransactionDetails);

        // Get data passed from SendMoneyActivity
        String status = getIntent().getStringExtra("TRANSACTION_STATUS");
        String reason = getIntent().getStringExtra("TRANSACTION_REASON");
        String txnId = getIntent().getStringExtra("TRANSACTION_ID");
        Double riskScore = getIntent().getDoubleExtra("TRANSACTION_RISK", 0.0);


        displayResult(status, reason, txnId, riskScore);

        // Animate entrance
        animateEntrance();

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Button press animation
                animateButtonPress(v);

                // Go back to the main home dashboard
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayResult(String status, String reason, String txnId, Double riskScore) {
        String fullReason = reason + " (Risk Score: " + riskScore + ")";

        if ("APPROVED".equals(status)) {
            // APPROVED UI
            iconStatus.setImageResource(android.R.drawable.checkbox_on_background);
            iconStatus.setColorFilter(Color.parseColor("#10B981")); // Green
            textStatusTitle.setText("Payment Successful");
            textStatusTitle.setTextColor(Color.parseColor("#10B981"));
            textReason.setText(fullReason);
            btnDone.setBackgroundColor(Color.parseColor("#10B981"));

        } else if ("FLAGGED".equals(status)) {
            // FLAGGED UI
            iconStatus.setImageResource(android.R.drawable.ic_dialog_alert);
            iconStatus.setColorFilter(Color.parseColor("#F59E0B")); // Orange/Yellow
            textStatusTitle.setText("Suspicious Activity Detected");
            textStatusTitle.setTextColor(Color.parseColor("#F59E0B"));
            textReason.setText("Decision: FLAGGED. " + fullReason);
            btnDone.setBackgroundColor(Color.parseColor("#F59E0B"));

        } else if ("BLOCKED".equals(status)) {
            // BLOCKED UI
            iconStatus.setImageResource(android.R.drawable.ic_delete);
            iconStatus.setColorFilter(Color.parseColor("#EF4444")); // Red
            textStatusTitle.setText("Transaction Blocked - High Fraud Risk");
            textStatusTitle.setTextColor(Color.parseColor("#EF4444"));
            textReason.setText("Decision: BLOCKED. " + fullReason);
            btnDone.setBackgroundColor(Color.parseColor("#EF4444"));
        }

        textTransactionId.setText(txnId);
    }

    private void animateEntrance() {
        // Animate status icon card
        if (cardStatusIcon != null) {
            cardStatusIcon.setAlpha(0f);
            cardStatusIcon.setScaleX(0f);
            cardStatusIcon.setScaleY(0f);
            cardStatusIcon.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(600)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }

        // Animate title
        textStatusTitle.setAlpha(0f);
        textStatusTitle.setTranslationY(-30f);
        textStatusTitle.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Animate reason
        textReason.setAlpha(0f);
        textReason.setTranslationY(-20f);
        textReason.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(400)
                .setDuration(500)
                .start();

        // Animate transaction details card
        if (cardTransactionDetails != null) {
            cardTransactionDetails.setAlpha(0f);
            cardTransactionDetails.setTranslationY(30f);
            cardTransactionDetails.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(500)
                    .setDuration(500)
                    .start();
        }

        // Animate button
        btnDone.setAlpha(0f);
        btnDone.setTranslationY(30f);
        btnDone.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(600)
                .setDuration(500)
                .start();
    }

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start();
                    }
                })
                .start();
    }
}