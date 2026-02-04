package com.example.upiapp;

import android.content.Context;

import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.SecurePrefManager;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://web-production-7ae5c.up.railway.app";

    private static Retrofit retrofit = null;

    public static ApiService getClient(Context context) {
        // We use an Interceptor to automatically add the Token to every request
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();

                    // Access the token directly using your utility class
                    SecurePrefManager prefManager = new SecurePrefManager(context);
                    String token = prefManager.getToken();

                    if (token != null) {
                        // Build the request with the required Authorization header [cite: 5, 32]
                        Request request = original.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .header("Content-Type", "application/json")  // [cite: 4]
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                    return chain.proceed(original);
                }).build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client) // Attach the Interceptor here
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}

