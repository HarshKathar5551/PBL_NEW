package com.example.upiapp.models;

public class SetPinRequest {

    private String password;
    private String newPin;

    public SetPinRequest(String password, String newPin) {
        this.password = password;
        this.newPin = newPin;
    }

    public String getPassword() {
        return password;
    }

    public String getNewPin() {
        return newPin;
    }
}
