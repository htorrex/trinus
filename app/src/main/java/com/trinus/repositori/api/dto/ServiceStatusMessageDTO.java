package com.trinus.repositori.api.dto;

import com.google.common.base.Strings;
import com.trinus.model.Driver;
import com.trinus.model.Vehicle;

import java.io.Serializable;

/**
 * Model class to manipulate the status response of service sent it from driver / client
 *
 * @author hetorres
 */
public class ServiceStatusMessageDTO implements MessageDTO, Serializable {
    private String objectId;
    private String idService;
    private Driver driver;
    private Vehicle vehicle;
    private String status;

    public String getIdService() {
        return Strings.isNullOrEmpty(idService)?objectId:idService; //FIXME unified with idService
    }

    public Driver getDriver() {
        return driver;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getStatus() {
        return status;
    }
}
