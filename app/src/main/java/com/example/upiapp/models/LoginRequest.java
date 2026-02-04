package com.example.upiapp.models;

public class LoginRequest {

    private String upiId;
    private String password;

    public LoginRequest(String upiId, String password) {
        this.upiId = upiId;
        this.password = password;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getPassword() {
        return password;
    }
}
