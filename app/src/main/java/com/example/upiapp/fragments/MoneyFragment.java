package com.example.upiapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

import com.example.upiapp.ApiClient;
import com.example.upiapp.R;
import com.example.upiapp.SendMoneyActivity;
import com.example.upiapp.adapters.TransactionAdapter;
import com.example.upiapp.models.Transaction;
import com.example.upiapp.models.TransactionHistoryResponse;
import com.example.upiapp.models.WalletResponse;
import com.example.upiapp.models.ProfileResponse;
import com.example.upiapp.service.ApiService;
import com.example.upiapp.utils.CsvExportUtil;
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
    private ImageButton btnFilter, btnRefreshBalance;
    private MaterialButton btnSendMoneyEmpty;
    private LinearLayout layoutEmptyState;
    private MaterialCardView cardBalance;

    private String myUpiId = "";

    // ✅ GLOBAL TRANSACTION LIST (IMPORTANT FOR CSV)
    private List<Transaction> transactionList = Collections.emptyList();

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_money, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // UI binding
        recyclerView = view.findViewById(R.id.recycler_view_history);
        textAccountBalance = view.findViewById(R.id.text_account_balance);
        textEmptyHistory = view.findViewById(R.id.text_empty_history);
        textTransactionCount = view.findViewById(R.id.text_transaction_count);
        layoutEmptyState = view.findViewById(R.id.layout_empty_state);
        btnFilter = view.findViewById(R.id.btn_filter);
        btnRefreshBalance = view.findViewById(R.id.btn_refresh_balance);
        btnSendMoneyEmpty = view.findViewById(R.id.btn_send_money_empty);
        cardBalance = view.findViewById(R.id.card_logo);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // ✅ CSV DOWNLOAD BUTTON
        Button btnDownloadCsv = view.findViewById(R.id.btn_download_csv);
        btnDownloadCsv.setOnClickListener(v -> {
            if (transactionList == null || transactionList.isEmpty()) {
                Toast.makeText(getContext(),
                        "No transactions to export",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            CsvExportUtil.exportTransactions(requireContext(), transactionList);
        });

        setupButtonListeners();
        animateEntrance();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchProfileAndHistory();
        fetchWalletBalance();
    }

    // 🔹 BUTTON LISTENERS
    private void setupButtonListeners() {

        if (btnFilter != null) {
            btnFilter.setOnClickListener(v -> {
                animateButtonPress(v);
                Toast.makeText(getActivity(),
                        "Filter coming soon",
                        Toast.LENGTH_SHORT).show();
            });
        }

        if (btnRefreshBalance != null) {
            btnRefreshBalance.setOnClickListener(v -> {
                animateRefreshButton(v);
                fetchWalletBalance();
            });
        }

        if (btnSendMoneyEmpty != null) {
            btnSendMoneyEmpty.setOnClickListener(v -> {
                animateButtonPress(v);
                startActivity(new Intent(getActivity(), SendMoneyActivity.class));
            });
        }
    }

    // 🔹 FETCH PROFILE → TRANSACTION HISTORY
    private void fetchProfileAndHistory() {
        ApiService apiService = ApiClient.getClient(getContext());

        apiService.getProfile().enqueue(new Callback<ProfileResponse>() {
            @Override
            public void onResponse(
                    Call<ProfileResponse> call,
                    Response<ProfileResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    myUpiId = response.body().upiId;
                    fetchTransactionHistory();
                }
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                Log.e("PROFILE", "Profile fetch failed", t);
            }
        });
    }

    // 🔹 TRANSACTION HISTORY
    private void fetchTransactionHistory() {
        ApiService apiService = ApiClient.getClient(getContext());

        apiService.getTransactionHistory().enqueue(new Callback<TransactionHistoryResponse>() {
            @Override
            public void onResponse(
                    Call<TransactionHistoryResponse> call,
                    Response<TransactionHistoryResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {

                    // ✅ STORE TRANSACTIONS GLOBALLY
                    transactionList = response.body().transactions;

                    if (transactionList == null) {
                        transactionList = Collections.emptyList();
                    }

                    textTransactionCount.setText(transactionList.size() + " txns");

                    TransactionAdapter adapter =
                            new TransactionAdapter(transactionList, myUpiId);
                    recyclerView.setAdapter(adapter);

                    showEmptyState(transactionList.isEmpty());
                }
            }

            @Override
            public void onFailure(Call<TransactionHistoryResponse> call, Throwable t) {
                showEmptyState(true);
            }
        });
    }

    // 🔹 WALLET BALANCE
    private void fetchWalletBalance() {
        if (getContext() == null) return;

        ApiService apiService = ApiClient.getClient(getContext());
        apiService.getBalance().enqueue(new Callback<WalletResponse>() {
            @Override
            public void onResponse(
                    @NonNull Call<WalletResponse> call,
                    @NonNull Response<WalletResponse> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    textAccountBalance.setText("₹ " + response.body().balance);
                    animateBalanceUpdate();
                } else {
                    textAccountBalance.setText("₹ --");
                }
            }

            @Override
            public void onFailure(@NonNull Call<WalletResponse> call, @NonNull Throwable t) {
                textAccountBalance.setText("₹ Error");
            }
        });
    }

    // 🔹 UI HELPERS
    private void showEmptyState(boolean isEmpty) {
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    private void animateEntrance() {
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
    }

    private void animateBalanceUpdate() {
        textAccountBalance.setScaleX(1.2f);
        textAccountBalance.setScaleY(1.2f);
        textAccountBalance.animate()
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void animateButtonPress(View view) {
        view.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100)
                                .start()
                ).start();
    }

    private void animateRefreshButton(View view) {
        view.animate()
                .rotation(360f)
                .setDuration(500)
                .withEndAction(() -> view.setRotation(0f))
                .start();
    }
}
