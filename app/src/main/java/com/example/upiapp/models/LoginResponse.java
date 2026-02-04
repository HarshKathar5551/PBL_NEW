package com.example.upiapp.models;

public class LoginResponse {

    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public User getUser() {
        return user;
    }

    public static class User {
        private String upiId;
        private String name;

        public String getUpiId() {
            return upiId;
        }

        public String getName() {
            return name;
        }
    }
}
