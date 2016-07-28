package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.trinus.repositori.api.DriverAPI;
import com.trinus.repositori.api.dto.DriverDTO;
import com.trinus.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hetorres on 7/23/16.
 */
public class UpdateDriversLocationRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = UpdateDriversLocationRequest.class.getSimpleName();
    private DriversLocationResponse driversLocationResponse;

    public UpdateDriversLocationRequest(SupportMapFragment driverMapFragment) {
        driversLocationResponse = (DriversLocationResponse) driverMapFragment;
    }

    public interface DriversLocationResponse {
        void onDriverLocationResponse(boolean result);
    }

    @Override
    protected Void doInBackground(String... params) {
        String userId = params[0];
        double latitude = Double.parseDouble(params[1]);
        double longitude = Double.parseDouble(params[2]);
        String driverGCMToken = params[3];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        DriverAPI driverAPI = retrofit.create(DriverAPI.class);
        DriverDTO driverDTO = new DriverDTO(userId, latitude, longitude, driverGCMToken);
        Call<Void> locationCall = driverAPI.updateLocation(driverDTO);

        locationCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.i(TAG, "on Update location Driver");
                if (response.isSuccessful()) {
                    driversLocationResponse.onDriverLocationResponse(true);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                driversLocationResponse.onDriverLocationResponse(false);
            }
        });

        return null;
    }
}
