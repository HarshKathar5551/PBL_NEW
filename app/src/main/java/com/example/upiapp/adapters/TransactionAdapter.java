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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);
        String displayUpiId = "unknown";
        String status = transaction.status; //

        // --- 1. LEGACY TIME FORMATTING (API 24 Compatible) ---
        String formattedTime = transaction.createdAt; //
        try {
            // Parser for the backend ISO format
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));

            // Formatter for readable output
            SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());

            Date date = parser.parse(transaction.createdAt); //
            if (date != null) {
                formattedTime = formatter.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.textTimestamp.setText(formattedTime);

        // --- 2. AMOUNT AND COLOR LOGIC ---
        if (status != null && !status.equalsIgnoreCase("SUCCESS") && !status.equalsIgnoreCase("PENDING")) {
            holder.textAmount.setText("₹ " + transaction.amount); //
            holder.textAmount.setTextColor(Color.RED);
            displayUpiId = transaction.toUpi; //
            holder.textStatus.setTextColor(Color.RED);
            holder.textStatus.setBackgroundColor(Color.parseColor("#22FF0000"));
        }
        else if (loggedInUpi != null && loggedInUpi.equalsIgnoreCase(transaction.toUpi)) {
            holder.textAmount.setText("₹ " + transaction.amount); //
            holder.textAmount.setTextColor(Color.parseColor("#4CAF50"));
            displayUpiId = transaction.fromUpi; //
            holder.textStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.textStatus.setBackgroundColor(Color.parseColor("#2200FF00"));
        }
        else {
            holder.textAmount.setText("₹ " + transaction.amount); //
            holder.textAmount.setTextColor(Color.BLACK);
            displayUpiId = transaction.toUpi; //
            holder.textStatus.setTextColor(Color.parseColor("#4CAF50"));
            holder.textStatus.setBackgroundColor(Color.parseColor("#2200FF00"));
        }

        holder.textReceiver.setText(displayUpiId);
        holder.textStatus.setText(status);

        String finalFormattedTime = formattedTime;
        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, TransactionDetailsActivity.class);
            intent.putExtra("TXN_ID", transaction.transactionId); //
            intent.putExtra("SENDER", transaction.fromUpi); //
            intent.putExtra("RECEIVER", transaction.toUpi); //
            intent.putExtra("AMOUNT", String.valueOf(transaction.amount)); //
            intent.putExtra("STATUS", transaction.status); //
            intent.putExtra("DATE", finalFormattedTime);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactionList != null ? transactionList.size() : 0;
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView textReceiver, textAmount, textStatus, textTimestamp;
        TransactionViewHolder(View itemView) {
            super(itemView);
            textReceiver = itemView.findViewById(R.id.text_receiver);
            textAmount = itemView.findViewById(R.id.text_amount);
            textStatus = itemView.findViewById(R.id.text_status);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
        }
    }
}