package com.example.upiapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.upiapp.R;
import com.example.upiapp.TransactionDetailsActivity;
import com.example.upiapp.models.Transaction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private final List<Transaction> transactionList;
    private final String loggedInUpi;

    public TransactionAdapter(List<Transaction> transactionList, String loggedInUpi) {
        this.transactionList = transactionList;
        this.loggedInUpi = loggedInUpi;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {

        Transaction transaction = transactionList.get(position);

        // --- TIME FORMAT (UNCHANGED) ---
        String formattedTime = transaction.createdAt;
        try {
            SimpleDateFormat parser =
                    new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));

            SimpleDateFormat formatter =
                    new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

            Date date = parser.parse(transaction.createdAt);
            if (date != null) formattedTime = formatter.format(date);
        } catch (Exception ignored) {}

        holder.textTimestamp.setText(formattedTime);
        holder.textStatus.setText(transaction.status);

        // Update status colors based on backend response
        String status = transaction.status != null ? transaction.status : "UNKNOWN";
        int statusColor;
        int statusBgColor;

        if (status.equalsIgnoreCase("SUCCESS") || status.equalsIgnoreCase("APPROVED")) {
            statusColor = Color.parseColor("#10B981"); // Green
            statusBgColor = Color.parseColor("#2210B981"); // Light Green
        } else if (status.equalsIgnoreCase("PENDING") || status.equalsIgnoreCase("FLAGGED")) {
            statusColor = Color.parseColor("#FF9800"); // Orange
            statusBgColor = Color.parseColor("#22FF9800"); // Light Orange
        } else { // FAILURE, BLOCKED or UNKNOWN
            statusColor = Color.parseColor("#DC2626"); // Red
            statusBgColor = Color.parseColor("#22DC2626"); // Light Red
        }

        holder.textStatus.setTextColor(statusColor);
        holder.textStatus.setBackgroundColor(statusBgColor);

        // ✅ CATEGORY FROM BACKEND
        holder.textCategory.setText(transaction.category);

        holder.textAmount.setText("₹ " + transaction.amount);

        // Amount Color Logic
        boolean isCredit = loggedInUpi != null && loggedInUpi.equalsIgnoreCase(transaction.toUpi);
        int amountColor;

        if (status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("FLAGGED") || status.equalsIgnoreCase("FAILURE")) {
            amountColor = Color.parseColor("#DC2626"); // Red for blocked/flagged/failure
        } else if (isCredit) {
            amountColor = Color.parseColor("#10B981"); // Green for credited
        } else {
            amountColor = Color.parseColor("#6B7280"); // Gray for sent
        }
        holder.textAmount.setTextColor(amountColor);

        if (isCredit) {
            holder.textReceiver.setText(transaction.fromUpi);
        } else {
            holder.textReceiver.setText(transaction.toUpi);
        }

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, TransactionDetailsActivity.class);
            intent.putExtra("TXN_ID", transaction.transactionId);
            intent.putExtra("SENDER", transaction.fromUpi);
            intent.putExtra("RECEIVER", transaction.toUpi);
            intent.putExtra("AMOUNT", String.valueOf(transaction.amount));
            intent.putExtra("STATUS", transaction.status);
            intent.putExtra("CATEGORY", transaction.category);
            intent.putExtra("IS_CREDIT", isCredit);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView textReceiver, textAmount, textStatus,
                textTimestamp, textCategory;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            textReceiver = itemView.findViewById(R.id.text_receiver);
            textAmount = itemView.findViewById(R.id.text_amount);
            textStatus = itemView.findViewById(R.id.text_status);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
            textCategory = itemView.findViewById(R.id.text_category);
        }
    }
}
