package com.trinus.repositori.api.dto;

import java.io.Serializable;

/**
 * Model class to manipulate the response of the service sent it from push to the Driver
 *
 * @author hetorres
 */
public class ServiceResponseMessageDTO implements MessageDTO, Serializable {
    private String idService;
    private double userLatitude;
    private double userLongitude;
    private String destinationAddress;

    public String getIdService() {
        return idService;
    }

    public double getUserLatitude() {
        return userLatitude;
    }

    public double getUserLongitude() {
        return userLongitude;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }
}
