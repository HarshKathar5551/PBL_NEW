package com.example.upiapp.models;

public class TransferRequest {
    public String toUpi;         //
    public int amount;           //
    public String pin;            //
    public String transactionType; //
    public Device device;         //
    public Location location;     //

    public static class Device {
        public String deviceId;   //
        public String deviceType; //
    }

    public static class Location {
        public String city;       //
        public String country;    //
    }
}