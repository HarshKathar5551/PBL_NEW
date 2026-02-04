package com.example.upiapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.upiapp.models.LoginRequest;
import com.example.upiapp.models.LoginResponse;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.LocalDataStore;
import com.example.upiapp.utils.SecurePrefManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editUsername;
    private EditText editPassword;
    private Button btnLogin;
    private TextView textSignupInstead;
    private LocalDataStore dataStore;
    private final String DUMMY_OTP = "1236";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        dataStore = new LocalDataStore(this);
//
//        // CHECK LOGIN STATUS FIRST (Crucial for the overall flow)
//        if (dataStore.isLoggedIn()) {
//            // NOTE: If you decide to add PIN validation on startup, this needs to check for a PIN too.
//            startActivity(new Intent(LoginActivity.this, MainActivity.class));
//            finish();
//            return;
//        }

        // Initialize UI components
        editUsername = findViewById(R.id.edit_username);
        editPassword = findViewById(R.id.edit_password);
        btnLogin = findViewById(R.id.btn_login);
        textSignupInstead = findViewById(R.id.text_signup_instead);

        // Dummy Logic: Enable login button only if fields are not empty
        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                boolean enableLogin = !editUsername.getText().toString().trim().isEmpty() &&
                        !editPassword.getText().toString().trim().isEmpty();
                btnLogin.setEnabled(enableLogin);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        editUsername.addTextChangedListener(loginTextWatcher);
        editPassword.addTextChangedListener(loginTextWatcher);

        // Initially disable the login button
        btnLogin.setEnabled(false);

        // Handle Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performLogin();
            }
        });

        // Handle the "Sign Up instead" link click
        textSignupInstead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirects to the new SignupActivity
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });
    }

    private void performLogin() {
        String inputUsername = editUsername.getText().toString().trim();
        String inputPassword = editPassword.getText().toString().trim();
//        // 1. Retrieve the saved credentials from LocalDataStore
//        final String savedUsername = dataStore.getSavedUsername();
//        final String savedPassword = dataStore.getSavedPassword();
//
//        // Check if input fields are empty
//        if (inputUsername.isEmpty() || inputPassword.isEmpty()) {
//            Toast.makeText(LoginActivity.this, "Please enter both Username and Password.", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        // 2. Check if a user has signed up yet (i.e., if credentials exist)
//        if (savedUsername == null || savedPassword == null) {
//            Toast.makeText(LoginActivity.this, "No user registered yet. Please Sign Up.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        // 3. Validate input against saved credentials
//        if (inputUsername.equals(savedUsername) && inputPassword.equals(savedPassword)) {

        // 1. Initialize the Utility Class
        SecurePrefManager prefManager = new SecurePrefManager(LoginActivity.this);

        ApiService apiService = ApiClient.getClient(this);
        // Prepare Login Data using upiId
        LoginRequest loginRequest = new LoginRequest(inputUsername, inputPassword);  // [cite: 19, 20]

        apiService.login(loginRequest).enqueue(new retrofit2.Callback<LoginResponse>() {
            @Override
            public void onResponse(retrofit2.Call<LoginResponse> call,
                                   retrofit2.Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginData = response.body();

                    // 3. Extract JWT Token from response body [cite: 24]
                    String jwtToken = response.body().getToken();

                    // 4. USE UTILITY METHOD to store the token securely
                    prefManager.saveToken(jwtToken);

                    // Accessing corrected field
                    String userIdentifier = loginData.getUser().getUpiId();
                    Log.d("LOGIN", "Logged in as: " + userIdentifier + " JWT Token: " + loginData.getToken());

                    Toast.makeText(getApplicationContext(), "Login Successful", Toast.LENGTH_SHORT).show();

                    // Login Success
//                    dataStore.setLoggedIn(true);

                    // Notify user and prompt for the next required step (Set PIN)
                    Toast.makeText(LoginActivity.this, "Login Successful! Please set your UPI PIN.", Toast.LENGTH_SHORT).show();

                    // *** REDIRECT TO THE NEW SET PIN ACTIVITY ***
                    Intent intent = new Intent(LoginActivity.this, SetPinActivity.class);
                    startActivity(intent);
                    finish(); // Close the login screen

                } else {
                    Log.e("LOGIN", "Error Code: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<LoginResponse> call, Throwable t) {
                Log.e("LOGIN", "Network Error", t);
            }
        });



//        } else {
//            // Login Failed
//            Toast.makeText(LoginActivity.this, "Invalid Username or Password.", Toast.LENGTH_SHORT).show();
//        }
    }

}