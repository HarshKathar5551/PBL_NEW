package com.example.upiapp;

import android.content.Context;

import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.SecurePrefManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL = "https://finance-assistant-backend-bw3u.onrender.com/";

    private static ApiService apiService = null;

    public static ApiService getClient(Context context) {
        if (apiService == null) {
            // Use Application Context to prevent memory leaks if an Activity context is passed
            Context appContext = context.getApplicationContext();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request original = chain.request();

                        // Access the token using the application context
                        SecurePrefManager prefManager = new SecurePrefManager(appContext);
                        String token = prefManager.getToken();

                        if (token != null) {
                            Request request = original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .header("Content-Type", "application/json")
                                    .method(original.method(), original.body())
                                    .build();
                            return chain.proceed(request);
                        }
                        return chain.proceed(original);
                    }).build();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}
