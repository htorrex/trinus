package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.trinus.repositori.api.DirectionsRequestAPI;
import com.trinus.repositori.api.dto.DirectionsDTO;
import com.trinus.ui.fragment.ClientMapFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Helper class to handle directions request between 2 points.
 *
 * @author hetorres
 */
public class DirectionsHelperTask extends AsyncTask<LatLng, Void, Void> {
    public static final String KEY = "AIzaSyA5b7usl3Y9hICD76nL91oxDuDR-FBf3Ts";
    private static final String TAG = AddressHelperTask.class.getSimpleName();

    private DirectionsDataResponse directionsDataResponse;

    public interface DirectionsDataResponse {
        void onDirectionsAction(DirectionsDTO response);
    }

    public DirectionsHelperTask(SupportMapFragment fragment) {
        directionsDataResponse = (DirectionsDataResponse) fragment;
    }

    @Override
    protected Void doInBackground(LatLng... params) {
        String origin = convertCoordinatesToString(params[0]);
        String destination = convertCoordinatesToString(params[1]);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        final DirectionsRequestAPI requestAPI = retrofit.create(DirectionsRequestAPI.class);
        Call<DirectionsDTO> directions = requestAPI.getDirections(origin, destination, KEY);
        directions.enqueue(new Callback<DirectionsDTO>() {
            @Override
            public void onResponse(Call<DirectionsDTO> call, Response<DirectionsDTO> response) {
                DirectionsDTO payload = response.body();
                if (response != null && payload != null) {
                    Log.i(TAG, "on success" + payload.getRoutes().size());
                    directionsDataResponse.onDirectionsAction(payload);
                }
            }

            @Override
            public void onFailure(Call<DirectionsDTO> call, Throwable t) {
                directionsDataResponse.onDirectionsAction(null);
            }
        });

        return null;
    }

    private String convertCoordinatesToString(LatLng coordinates){
        StringBuilder builder = new StringBuilder();
        builder.append(coordinates.latitude)
                .append(",")
                .append(coordinates.longitude);
        return builder.toString();
    }
}
