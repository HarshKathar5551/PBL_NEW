package com.example.upiapp.models;

public class Transaction {
    public String transactionId; // [cite: 81]
    public String fromUpi;
    public String toUpi;         // [cite: 82]
    public int amount;           // [cite: 83]
    public String status;         // [cite: 84]
    public String createdAt;     // [cite: 85]


}



//package com.example.upiapp.models;
//public class Transaction {
//    private String receiverId;
//    private double amount;
//    private String message;
//    private String timestamp;
//    private String status; // APPROVED, FLAGGED, BLOCKED
//    private int riskScore;
//    private String reason;
//    private String transactionId; // Unique ID
//
//    // Constructor
//    public Transaction(String receiverId, double amount, String message, String status, int riskScore, String reason) {
//        this.receiverId = receiverId;
//        this.amount = amount;
//        this.message = message;
//        this.status = status;
//        this.riskScore = riskScore;
//        this.reason = reason;
//        this.timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date());
//        this.transactionId = "TXN_" + System.currentTimeMillis();
//    }
//
//    // --- Getters ---
//    public String getReceiverId() { return receiverId; }
//    public double getAmount() { return amount; }
//    public String getMessage() { return message; }
//    public String getTimestamp() { return timestamp; }
//    public String getStatus() { return status; }
//    public int getRiskScore() { return riskScore; }
//    public String getReason() { return reason; }
//    public String getTransactionId() { return transactionId; }
//}

