package com.example.upiapp.models;

public class SignupRequest {
    public String upiId;
    public String name;
    public String password;
    public String pin;
    public String mobile;
    public String deviceId;

    public SignupRequest(String upiId, String name, String password,
                         String pin, String mobile, String deviceId) {
        this.upiId = upiId;
        this.name = name;
        this.password = password;
        this.pin = pin;
        this.mobile = mobile;
        this.deviceId = deviceId;
    }
}

