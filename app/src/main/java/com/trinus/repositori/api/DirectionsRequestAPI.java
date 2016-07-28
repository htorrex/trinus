package com.trinus.repositori.api;

import com.trinus.repositori.api.dto.DirectionsDTO;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Interface to handle the directions api request
 */
public interface DirectionsRequestAPI {
    // https://developers.google.com/maps/documentation/directions/intro#RequestParameters
    // this is a Synchronous method since we are running this inside of Async Task.
    @GET("/maps/api/directions/json")
    Call<DirectionsDTO> getDirections(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String key
    );
}
