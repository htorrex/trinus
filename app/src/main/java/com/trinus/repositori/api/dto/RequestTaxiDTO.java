package com.trinus.repositori.api.dto;

/**
 * Created by hetorres on 7/25/16.
 */
public class RequestTaxiDTO {
    private String userId;
    private double latitude;
    private double longitude;
    private String address;
    private String userGCMToken;

    public RequestTaxiDTO(String userId, double latitude, double longitude, String address, String userGCMToken) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.userGCMToken = userGCMToken;
    }

    public String getUserId() {
        return userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public String getUserGCMToken() {
        return userGCMToken;
    }
}
