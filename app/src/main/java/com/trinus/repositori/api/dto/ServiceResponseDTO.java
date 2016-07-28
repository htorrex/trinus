package com.trinus.repositori.api.dto;

/**
 * Created by hetorres on 7/23/16.
 */
public class ServiceResponseDTO {
    String objectId;
    String status;
    String user;

    public String getStatus() {
        return status;
    }

    public String getUser() {
        return user;
    }

    public String getObjectId() {
        return objectId;
    }
}
