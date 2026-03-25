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

        // ✅ CATEGORY FROM BACKEND
        holder.textCategory.setText(transaction.category);

        // --- AMOUNT + COLOR LOGIC (UNCHANGED) ---
        holder.textAmount.setText("₹ " + transaction.amount);

        if (loggedInUpi != null && loggedInUpi.equalsIgnoreCase(transaction.toUpi)) {
            holder.textAmount.setTextColor(Color.parseColor("#4CAF50"));
            holder.textReceiver.setText(transaction.fromUpi);
        } else {
            holder.textAmount.setTextColor(Color.BLACK);
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
            intent.putExtra("CATEGORY", transaction.category); // ✅ PASS CATEGORY
//            intent.putExtra("DATE", formattedTime);
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
            textCategory = itemView.findViewById(R.id.text_category); // ✅ NEW
        }
    }
}
