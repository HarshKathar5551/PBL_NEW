package com.example.upiapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.upiapp.models.ProfileResponse;
import com.example.upiapp.service.ApiService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiveMoneyActivity extends AppCompatActivity {

    private TextView textReceiveUpiId;
    private ImageView imageQrCode;
    private Button btnShareUpiId;

    private String userUpiId;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_money);

        textReceiveUpiId = findViewById(R.id.text_receive_upi_id);
        imageQrCode = findViewById(R.id.image_qr_code);
        btnShareUpiId = findViewById(R.id.btn_share_upi_id);

        // Fetch user data from backend as per API contract
        fetchProfileAndGenerateQR();

        btnShareUpiId.setOnClickListener(v -> shareUpiIdAndQr());


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

    private void fetchProfileAndGenerateQR() {
        // ApiClient handles the "Authorization: Bearer <JWT>" header automatically
        ApiService apiService = ApiClient.getClient(this);

        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProfileResponse> call, @NonNull Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Mapping data from Profile API
                    userUpiId = response.body().upiId; // [cite: 42]
                    userName = response.body().name;   // [cite: 43]

                    textReceiveUpiId.setText(userUpiId);
                    generateQrCode();
                } else {
                    Log.e("RECEIVE_MONEY", "Failed to fetch profile: " + response.code());
                    Toast.makeText(ReceiveMoneyActivity.this, "Could not fetch user identity", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProfileResponse> call, @NonNull Throwable t) {
                Log.e("RECEIVE_MONEY", "Network error", t);
                Toast.makeText(ReceiveMoneyActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void generateQrCode() {
        if (userUpiId == null || userName == null) return;

        try {
            // Structured QR content for the UPI simulator
            String qrContent = "UPI:\nID=" + userUpiId + ";\nNAME=" + userName + ";";

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix bitMatrix = writer.encode(qrContent, BarcodeFormat.QR_CODE, 600, 600);

            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.createBitmap(bitMatrix);
            imageQrCode.setImageBitmap(bitmap);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to generate QR Code", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareUpiIdAndQr() {
        if (userUpiId == null) {
            Toast.makeText(this, "UPI ID not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri imageUri = getQrImageUri();
        if (imageUri != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("image/png");
            intent.putExtra(Intent.EXTRA_STREAM, imageUri);
            intent.putExtra(Intent.EXTRA_TEXT, "Pay me using my UPI ID: " + userUpiId);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(intent, "Share UPI via"));
        }
    }

    private Uri getQrImageUri() {
        try {
            BitmapDrawable drawable = (BitmapDrawable) imageQrCode.getDrawable();
            if (drawable == null) return null;

            Bitmap bitmap = drawable.getBitmap();
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "upi_qr.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            return FileProvider.getUriForFile(this, "com.example.upiapp.fileprovider", file);
        } catch (IOException e) {
            return null;
        }
    }
}