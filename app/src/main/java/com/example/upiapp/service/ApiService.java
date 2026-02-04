package com.example.upiapp.service;

import com.example.upiapp.models.LoginRequest;
import com.example.upiapp.models.LoginResponse;
import com.example.upiapp.models.SetPinRequest;
import com.example.upiapp.models.SignupRequest;
import com.example.upiapp.models.TransactionHistoryResponse;
import com.example.upiapp.models.TransferRequest;
import com.example.upiapp.models.TransferResponse;
import com.example.upiapp.models.WalletResponse;
import com.example.upiapp.models.ProfileResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/auth/signup")
    Call<Void> signup(@Body SignupRequest request);
    @POST("/auth/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("/auth/set-pin")
    Call<Void> setPin(
            @Body SetPinRequest request
    );

    @GET("/wallet/balance") //
    Call<WalletResponse> getBalance();

    @GET("/transactions/history") //
    Call<TransactionHistoryResponse> getTransactionHistory();

    @POST("/transactions/transfer") // [cite: 53]
    Call<TransferResponse> transfer(@Body TransferRequest request);

    @GET("/profile") //
    Call<ProfileResponse> getProfile();

}
