package com.trinus.repositori.api.dto;

/**
 * Created by hetorres on 7/25/16.
 */
public class DriverDTO {
    private String idDriver;
    private double latitude;
    private double longitude;
    private String tokenGCM;

    public DriverDTO(String idDriver, double latitude, double longitude, String tokenGCM){
        this.idDriver = idDriver;
        this.latitude = latitude;
        this.longitude = longitude;
        this.tokenGCM = tokenGCM;
    }

    public String getIdDriver() {
        return idDriver;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getTokenGCM() {
        return tokenGCM;
    }
}
