package com.trinus.repositori.api;

import com.google.android.gms.maps.model.LatLng;
import com.trinus.repositori.api.dto.RequestTaxiDTO;
import com.trinus.repositori.api.dto.ServiceResponseDTO;
import com.trinus.repositori.api.dto.ServiceStatusDTO;
import com.trinus.repositori.api.dto.ServiceStatusMessageDTO;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Interface to handle the Taxi Services request
 * TODO change variables  for objects
 */
public interface TaxiServices {
    @POST("/trinus/api/v1/services")
    Call<ServiceResponseDTO> requestTaxi(@Body RequestTaxiDTO requestTaxiDTO);

    @GET("/trinus/api/v1/services/location")
    Call<LatLng> getDriversLocationByService(
            @Query("idService") String idService
    );

    @PUT("/trinus/api/v1/services")
    Call<Void> updateServiceStatus(@Body ServiceStatusDTO idService);

    @GET("/trinus/api/v1/services")
    Call<ServiceStatusMessageDTO> getServiceById(
            @Query("serviceId") String idService
    );
}
