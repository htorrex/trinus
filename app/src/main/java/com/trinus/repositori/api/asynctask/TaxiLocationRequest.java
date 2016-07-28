package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.trinus.repositori.api.TaxiServices;
import com.trinus.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hetorres on 7/23/16.
 */
public class TaxiLocationRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = TaxiLocationRequest.class.getSimpleName();
    private TaxiLocationResponse driversLocationResponse;

    public TaxiLocationRequest(SupportMapFragment fragment) {
        driversLocationResponse = (TaxiLocationResponse) fragment;
    }

    public interface TaxiLocationResponse {
        void onTaxiLocationAction(LatLng coordinates);
    }

    @Override
    protected Void doInBackground(String... params) {
        String idService = params[0];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TaxiServices requestAPI = retrofit.create(TaxiServices.class);

        Call<LatLng> driversLocationByService = requestAPI.getDriversLocationByService(idService);
        driversLocationByService.enqueue(new Callback<LatLng>() {
            @Override
            public void onResponse(Call<LatLng> call, Response<LatLng> response) {
                if (response.isSuccessful()) {
                    LatLng payload = response.body();
                    if (payload != null) {
                        Log.d(TAG, "<<<<< latitude:" + payload.latitude);
                        Log.d(TAG, "<<<<< longitude:" + payload.longitude);
                        driversLocationResponse.onTaxiLocationAction(payload);
                    }
                }
            }

            @Override
            public void onFailure(Call<LatLng> call, Throwable t) {
                Log.d(TAG, "<<<<<<<<<<<<< onFailure  >>>>>>>>>>>>");
                driversLocationResponse.onTaxiLocationAction(null);
            }
        });

        return null;
    }
}
