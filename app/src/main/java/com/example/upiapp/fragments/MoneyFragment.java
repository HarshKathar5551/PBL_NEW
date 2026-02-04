package com.example.upiapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.example.upiapp.ApiClient;
import com.example.upiapp.R;
import com.example.upiapp.SendMoneyActivity;
import com.example.upiapp.adapters.TransactionAdapter;
import com.example.upiapp.models.Transaction;
import com.example.upiapp.models.WalletResponse;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.models.TransactionHistoryResponse;
import com.example.upiapp.models.ProfileResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MoneyFragment extends Fragment {

    private RecyclerView recyclerView;
    private TextView textAccountBalance;
    private TextView textEmptyHistory;
    private TextView textTransactionCount;
    private ImageButton  btnFilter, btnRefreshBalance;
    private MaterialButton btnSendMoneyEmpty;
    private LinearLayout layoutEmptyState;
    private MaterialCardView cardBalance;
    private String myUpiId = ""; // To store the logged-in user's ID

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // ESSENTIAL: This must be present to inflate the layout
        return inflater.inflate(R.layout.fragment_money, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        recyclerView = view.findViewById(R.id.recycler_view_history);
        textAccountBalance = view.findViewById(R.id.text_account_balance);
        textEmptyHistory = view.findViewById(R.id.text_empty_history);
        textTransactionCount = view.findViewById(R.id.text_transaction_count);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);

        btnFilter = view.findViewById(R.id.btn_filter);
        btnRefreshBalance = view.findViewById(R.id.btn_refresh_balance);
        btnSendMoneyEmpty = view.findViewById(R.id.btn_send_money_empty);
        cardBalance = view.findViewById(R.id.card_logo); // Assuming this is the balance card



        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));



        // Set up button listeners
        setupButtonListeners();

        // Animate entrance
        animateEntrance();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchProfileAndHistory();
        fetchWalletBalance();   // GET /wallet/balance
//        fetchTransactionHistory(); // GET /transactions/history
    }

    private void setupButtonListeners() {
        // Back button


        // Filter button
        if (btnFilter != null) {
            btnFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButtonPress(v);
                    Toast.makeText(getActivity(), "Filter coming soon", Toast.LENGTH_SHORT).show();
                    // TODO: Implement filter functionality
                }
            });
        }

        // Refresh balance button
        if (btnRefreshBalance != null) {
            btnRefreshBalance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateRefreshButton(v);
                    fetchWalletBalance();
                    Toast.makeText(getActivity(), "Refreshing balance...", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Send Money button (empty state)
        if (btnSendMoneyEmpty != null) {
            btnSendMoneyEmpty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateButtonPress(v);
                    // Navigate to Send Money activity
                    Intent intent = new Intent(getActivity(), SendMoneyActivity.class);
                    startActivity(intent);
                }
            });
        }
    }

    private void fetchProfileAndHistory() {
        ApiService apiService = ApiClient.getClient(getContext());

        // Step 1: Get Profile to find out "Who am I?"
        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    myUpiId = response.body().upiId; // Store our UPI ID

                    // Step 2: Now that we know our ID, fetch history
                    fetchTransactionHistory();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e("PROFILE", "Failed to fetch profile info");
            }
        });
    }

    private void fetchTransactionHistory() {
        ApiService apiService = ApiClient.getClient(getContext());
        apiService.getTransactionHistory().enqueue(new Callback<TransactionHistoryResponse>() {
            @Override
            public void onResponse(Call<TransactionHistoryResponse> call, Response<TransactionHistoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Transaction> transactions = response.body().transactions;

                    // Update transaction count
                    if (textTransactionCount != null && transactions != null) {
                        textTransactionCount.setText(transactions.size() + " txns");
                    }

                    // Pass our retrieved UPI ID to the adapter for coloring logic
                    TransactionAdapter adapter = new TransactionAdapter(transactions, myUpiId);
                    recyclerView.setAdapter(adapter);
                    showEmptyState(transactions.isEmpty());
                }
            }
            @Override
            public void onFailure(Call<TransactionHistoryResponse> call, Throwable t) { /* handle error */ }
        });
    }

    private void fetchWalletBalance() {
        if (getContext() == null) return;

        ApiService apiService = ApiClient.getClient(getContext());
        apiService.getBalance().enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(@NonNull Call<WalletResponse> call, @NonNull Response<WalletResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int balance = response.body().balance;
                    textAccountBalance.setText("₹ " + balance);
                    animateBalanceUpdate();
                } else if (response.code() == 401) {
                    textAccountBalance.setText("₹ --");
                    Toast.makeText(getActivity(), "Session expired", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalletResponse> call, @NonNull Throwable t) {
                textAccountBalance.setText("₹ Error");
            }
        });
    }

//    private void fetchTransactionHistory() {
//        if (getContext() == null) return;
//
//        ApiService apiService = ApiClient.getClient(getContext());
//        apiService.getTransactionHistory().enqueue(new Callback<TransactionHistoryResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<TransactionHistoryResponse> call, @NonNull Response<TransactionHistoryResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    List<Transaction> transactions = response.body().transactions;
//
//                    if (transactions == null || transactions.isEmpty()) {
//                        showEmptyState(true);
//                    } else {
//                        showEmptyState(false);
//                        TransactionAdapter adapter = new TransactionAdapter(transactions, getContext());
//                        recyclerView.setAdapter(adapter);
//                    }
//                } else {
//                    showEmptyState(true);
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<TransactionHistoryResponse> call, @NonNull Throwable t) {
//                showEmptyState(true);
//            }
//        });
//    }

    private void showEmptyState(boolean isEmpty) {
        if (isEmpty) {
            recyclerView.setVisibility(View.GONE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.VISIBLE);
                animateEmptyState();
            }
            // Fallback to old text view if layout not found
            if (textEmptyHistory != null && layoutEmptyState == null) {
                textEmptyHistory.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            if (layoutEmptyState != null) {
                layoutEmptyState.setVisibility(View.GONE);
            }
            if (textEmptyHistory != null) {
                textEmptyHistory.setVisibility(View.GONE);
            }
        }
    }

    private void animateEntrance() {
        // Animate balance card
        if (cardBalance != null) {
            cardBalance.setAlpha(0f);
            cardBalance.setTranslationY(-30f);
            cardBalance.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(600)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        // Animate RecyclerView
        if (recyclerView != null) {
            recyclerView.setAlpha(0f);
            recyclerView.setTranslationY(30f);
            recyclerView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(200)
                    .setDuration(600)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private void animateEmptyState() {
        if (layoutEmptyState != null) {
            layoutEmptyState.setAlpha(0f);
            layoutEmptyState.setScaleX(0.8f);
            layoutEmptyState.setScaleY(0.8f);
            layoutEmptyState.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(500)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void animateBalanceUpdate() {
        if (textAccountBalance != null) {
            textAccountBalance.setScaleX(1.2f);
            textAccountBalance.setScaleY(1.2f);
            textAccountBalance.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setInterpolator(new OvershootInterpolator())
                    .start();
        }
    }

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
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

    private void animateRefreshButton(View view) {
        view.animate()
                .rotation(360f)
                .setDuration(500)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        view.setRotation(0f);
                    }
                })
                .start();
    }
}