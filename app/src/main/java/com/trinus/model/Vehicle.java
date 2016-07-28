package com.trinus.model;

/**
 * Model class to handle the Vehicle information
 *
 * @author hetorres
 */
public class Vehicle {
    String objectId;
    String model;
    String plates;
    double latitude;
    double longitude;

    public String getObjectId() {
        return objectId;
    }

    public String getModel() {
        return model;
    }

    public String getPlates() {
        return plates;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
