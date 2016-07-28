package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.trinus.repositori.api.TaxiServices;
import com.trinus.repositori.api.dto.ServiceStatusDTO;
import com.trinus.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hetorres on 7/23/16.
 */
public class UpdateServiceStatusRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = UpdateServiceStatusRequest.class.getSimpleName();
    private ServiceStatusResponse serviceStatusResponse;

    public UpdateServiceStatusRequest(SupportMapFragment driverMapFragment) {
        serviceStatusResponse = (ServiceStatusResponse) driverMapFragment;
    }

    public interface ServiceStatusResponse {
        void onServiceStatusResponse(boolean result);
    }

    @Override
    protected Void doInBackground(String... params) {
        String idService = params[0];
        double latitude = Double.parseDouble(params[1]);
        double longitude = Double.parseDouble(params[2]);
        String status = params[3];
        String idDriver = params[4];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ServiceStatusDTO serviceStatusDTO = new ServiceStatusDTO(idService, latitude, longitude, status, idDriver);

        TaxiServices taxiServices = retrofit.create(TaxiServices.class);
        Call<Void> statusCall = taxiServices.updateServiceStatus(serviceStatusDTO);

        statusCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.i(TAG, "Service Status Response");
                if (response.isSuccessful()) {
                    serviceStatusResponse.onServiceStatusResponse(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                serviceStatusResponse.onServiceStatusResponse(false);
            }
        });

        return null;
    }
}
