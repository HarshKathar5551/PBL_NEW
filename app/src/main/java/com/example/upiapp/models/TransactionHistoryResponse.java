package com.example.upiapp.models;
import com.example.upiapp.adapters.TransactionAdapter;

import java.util.List;

public class TransactionHistoryResponse {
    // The key "transactions" must match the JSON key in the contract
    public List<Transaction> transactions;
}