package com.example.upiapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.upiapp.utils.LocalDataStore;
import com.google.android.material.card.MaterialCardView;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_TIME = 3000; // 2 seconds for better animation experience

    private MaterialCardView cardLogo;
    private ImageView imgLogo;
    private TextView txtAppName, txtTagline, txtLoading, txtVersion;
    private LinearLayout centerContainer, loadingContainer;
    private ProgressBar progressLoader;
    private View logoGlow, progressPulse, circleTop, circleBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        initializeViews();

        // Start animations
        startAnimations();

        LocalDataStore dataStore = new LocalDataStore(this);

        // Navigate after splash time
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Fade out animation before navigation
            fadeOutAndNavigate(dataStore);
        }, SPLASH_TIME);
    }

    private void initializeViews() {
        cardLogo = findViewById(R.id.card_logo);
        imgLogo = findViewById(R.id.img_logo);
        txtAppName = findViewById(R.id.txt_app_name);
        txtTagline = findViewById(R.id.txt_tagline);
        txtLoading = findViewById(R.id.txt_loading);
        txtVersion = findViewById(R.id.txt_version);
        centerContainer = findViewById(R.id.center_container);
        loadingContainer = findViewById(R.id.loading_container);
        progressLoader = findViewById(R.id.progress_loader);
        logoGlow = findViewById(R.id.logo_glow);
        progressPulse = findViewById(R.id.progress_pulse);
        circleTop = findViewById(R.id.circle_top);
        circleBottom = findViewById(R.id.circle_bottom);
    }

    private void startAnimations() {
        // 1. Animate decorative circles
        animateDecorativeCircles();

        // 2. Animate logo with scale and rotation
        animateLogo();

        // 3. Animate logo glow with pulse effect
        animateLogoGlow();

        // 4. Animate app name with slide from bottom
        animateAppName();

        // 5. Animate tagline
        animateTagline();

        // 6. Animate loading section
        animateLoadingSection();

        // 7. Animate progress pulse
        animateProgressPulse();

        // 8. Animate loading text with fade
        animateLoadingText();
    }

    private void animateDecorativeCircles() {
        // Top circle rotation
        ObjectAnimator rotateTop = ObjectAnimator.ofFloat(circleTop, "rotation", 0f, 360f);
        rotateTop.setDuration(20000);
        rotateTop.setRepeatCount(ValueAnimator.INFINITE);
        rotateTop.setInterpolator(new LinearInterpolator());
        rotateTop.start();

        // Bottom circle rotation (opposite direction)
        ObjectAnimator rotateBottom = ObjectAnimator.ofFloat(circleBottom, "rotation", 360f, 0f);
        rotateBottom.setDuration(15000);
        rotateBottom.setRepeatCount(ValueAnimator.INFINITE);
        rotateBottom.setInterpolator(new LinearInterpolator());
        rotateBottom.start();
    }

    private void animateLogo() {
        // Initial state
        imgLogo.setScaleX(0f);
        imgLogo.setScaleY(0f);
        imgLogo.setRotation(-90f);
        imgLogo.setAlpha(0f);

        // Animate with bounce effect
        imgLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .rotation(0f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();

        // Subtle continuous rotation
        ObjectAnimator logoRotate = ObjectAnimator.ofFloat(imgLogo, "rotation", 0f, 5f, 0f, -5f, 0f);
        logoRotate.setDuration(4000);
        logoRotate.setRepeatCount(ValueAnimator.INFINITE);
        logoRotate.setStartDelay(1000);
        logoRotate.start();
    }

    private void animateLogoGlow() {
        // Pulse effect for glow
        ObjectAnimator glowPulse = ObjectAnimator.ofFloat(logoGlow, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator glowPulseY = ObjectAnimator.ofFloat(logoGlow, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator glowAlpha = ObjectAnimator.ofFloat(logoGlow, "alpha", 0.3f, 0.6f, 0.3f);

        glowPulse.setDuration(2000);
        glowPulseY.setDuration(2000);
        glowAlpha.setDuration(2000);

        glowPulse.setRepeatCount(ValueAnimator.INFINITE);
        glowPulseY.setRepeatCount(ValueAnimator.INFINITE);
        glowAlpha.setRepeatCount(ValueAnimator.INFINITE);

        glowPulse.start();
        glowPulseY.start();
        glowAlpha.start();
    }

    private void animateAppName() {
        txtAppName.setAlpha(0f);
        txtAppName.setTranslationY(50f);

        txtAppName.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(600)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateTagline() {
        txtTagline.setAlpha(0f);
        txtTagline.setTranslationY(30f);

        txtTagline.animate()
                .alpha(0.8f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(900)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateLoadingSection() {
        loadingContainer.setAlpha(0f);
        loadingContainer.setTranslationY(50f);

        loadingContainer.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(1200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void animateProgressPulse() {
        ObjectAnimator pulsePulse = ObjectAnimator.ofFloat(progressPulse, "scaleX", 1f, 1.3f, 1f);
        ObjectAnimator pulsePulseY = ObjectAnimator.ofFloat(progressPulse, "scaleY", 1f, 1.3f, 1f);
        ObjectAnimator pulseAlpha = ObjectAnimator.ofFloat(progressPulse, "alpha", 0.2f, 0.5f, 0.2f);

        pulsePulse.setDuration(1500);
        pulsePulseY.setDuration(1500);
        pulseAlpha.setDuration(1500);

        pulsePulse.setRepeatCount(ValueAnimator.INFINITE);
        pulsePulseY.setRepeatCount(ValueAnimator.INFINITE);
        pulseAlpha.setRepeatCount(ValueAnimator.INFINITE);

        pulsePulse.setStartDelay(1200);
        pulsePulseY.setStartDelay(1200);
        pulseAlpha.setStartDelay(1200);

        pulsePulse.start();
        pulsePulseY.start();
        pulseAlpha.start();
    }

    private void animateLoadingText() {
        // Fade in/out animation for loading text
        ObjectAnimator textFade = ObjectAnimator.ofFloat(txtLoading, "alpha", 0.5f, 1f, 0.5f);
        textFade.setDuration(1500);
        textFade.setRepeatCount(ValueAnimator.INFINITE);
        textFade.setStartDelay(1200);
        textFade.start();
    }

    private void fadeOutAndNavigate(LocalDataStore dataStore) {
        // Fade out all elements
        centerContainer.animate()
                .alpha(0f)
                .setDuration(500)
                .start();

        loadingContainer.animate()
                .alpha(0f)
                .setDuration(500)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // 🔐 If user already logged in → MainActivity
                        Intent intent;
                        if (dataStore.getSavedUsername() != null) {
                            intent = new Intent(SplashActivity.this, MainActivity.class);
                        } else {
                            // 🔑 Otherwise → Login
                            intent = new Intent(SplashActivity.this, LoginActivity.class);
                        }

                        startActivity(intent);
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                        finish();
                    }
                })
                .start();
    }
}