package com.trinus.repositori.api.dto;

/**
 * Created by hetorres on 7/25/16.
 */
public class ServiceStatusDTO {
    private String idService;
    private double latitude;
    private double longitude;
    private String status;
    private String idDriver;

    public ServiceStatusDTO(String idService, double latitude, double longitude, String status, String idDriver) {
        this.idService = idService;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
        this.idDriver = idDriver;
    }

    public String getIdService() {
        return idService;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getStatus() {
        return status;
    }

    public String getIdDriver() {
        return idDriver;
    }
}
