package com.example.upiapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.upiapp.R;
import com.example.upiapp.ReceiveMoneyActivity;
import com.example.upiapp.SendMoneyActivity;
import com.example.upiapp.SendMoneyDeveloperModeActivity;
import com.example.upiapp.utils.SecurePrefManager;
import com.google.android.material.card.MaterialCardView;

public class HomeFragment extends Fragment {

    private SecurePrefManager prefManager;
    private MaterialCardView cardSendMoney, cardReceiveMoney;
    private ImageButton btnNotifications;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize SecurePrefManager
        prefManager = new SecurePrefManager(getActivity());

        // Initialize new card views
        cardSendMoney = view.findViewById(R.id.card_send_money);
        cardReceiveMoney = view.findViewById(R.id.card_receive_money);
        btnNotifications = view.findViewById(R.id.btn_notifications);

        // Keep original buttons for backward compatibility (hidden in XML)
        Button btnSendMoney = view.findViewById(R.id.btn_send_money);
        Button btnReceiveMoney = view.findViewById(R.id.btn_receive_money);

        // Animate entrance
        animateEntrance();

        // 1. Handle Send Money click (Dynamic Redirection) - NEW CARD
        cardSendMoney.setOnClickListener(v -> {
            animateCardPress(cardSendMoney);
            Intent intent;

            // Check if Developer Mode is toggled ON in Secure Preferences
            if (prefManager.isDeveloperModeEnabled()) {
                // Redirect to the RAW JSON Editor Activity
                intent = new Intent(getActivity(), SendMoneyDeveloperModeActivity.class);
                Toast.makeText(getActivity(), "Dev Mode: Direct JSON Access", Toast.LENGTH_SHORT).show();
            } else {
                // Redirect to the standard UPI payment flow
                intent = new Intent(getActivity(), SendMoneyActivity.class);
            }

            startActivity(intent);
        });

        // 1. Handle Send Money click (Original Button - for backward compatibility)
        btnSendMoney.setOnClickListener(v -> {
            Intent intent;

            // Check if Developer Mode is toggled ON in Secure Preferences
            if (prefManager.isDeveloperModeEnabled()) {
                // Redirect to the RAW JSON Editor Activity
                intent = new Intent(getActivity(), SendMoneyDeveloperModeActivity.class);
                Toast.makeText(getActivity(), "Dev Mode: Direct JSON Access", Toast.LENGTH_SHORT).show();
            } else {
                // Redirect to the standard UPI payment flow
                intent = new Intent(getActivity(), SendMoneyActivity.class);
            }

            startActivity(intent);
        });

        // 2. Handle Receive Money click - NEW CARD
        cardReceiveMoney.setOnClickListener(v -> {
            animateCardPress(cardReceiveMoney);
            startActivity(new Intent(getActivity(), ReceiveMoneyActivity.class));
        });

        // 2. Handle Receive Money click (Original Button - for backward compatibility)
        btnReceiveMoney.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), ReceiveMoneyActivity.class));
        });

        // 3. Handle Notifications click
        btnNotifications.setOnClickListener(v -> {
            animateButtonPress(btnNotifications);
            Toast.makeText(getActivity(), "No new notifications", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to notifications activity when implemented
        });
    }

    private void animateEntrance() {
        // Animate Send Money Card
        if (cardSendMoney != null) {
            cardSendMoney.setAlpha(0f);
            cardSendMoney.setTranslationX(-100f);
            cardSendMoney.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(200)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        // Animate Receive Money Card
        if (cardReceiveMoney != null) {
            cardReceiveMoney.setAlpha(0f);
            cardReceiveMoney.setTranslationX(-100f);
            cardReceiveMoney.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setStartDelay(300)
                    .setDuration(500)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animateCardPress(View view) {
        view.animate()
                .scaleX(0.97f)
                .scaleY(0.97f)
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

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
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