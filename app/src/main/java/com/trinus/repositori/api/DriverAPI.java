package com.trinus.repositori.api;

import com.trinus.repositori.api.dto.DriverDTO;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Interface to handle the Drivers Requests
 */
public interface DriverAPI {
    @POST("/trinus/api/v1/drivers/location")
    Call<Void> updateLocation(@Body DriverDTO driverDTO);
}
