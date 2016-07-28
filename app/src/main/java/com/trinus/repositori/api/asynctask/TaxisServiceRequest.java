package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.trinus.repositori.api.TaxiServices;
import com.trinus.repositori.api.dto.RequestTaxiDTO;
import com.trinus.repositori.api.dto.ServiceResponseDTO;
import com.trinus.ui.fragment.ClientMapFragment;
import com.trinus.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hetorres on 7/23/16.
 */
public class TaxisServiceRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = TaxisServiceRequest.class.getSimpleName();
    private TaxiServiceDataResponse directionsDataResponse;

    public TaxisServiceRequest(ClientMapFragment clientMapFragment) {
        directionsDataResponse = clientMapFragment;
    }

    public interface TaxiServiceDataResponse {
        void onTaxiResponseAction(ServiceResponseDTO response);
    }

    @Override
    protected Void doInBackground(String... params) {
        String userId = params[0];
        double latitude = Double.parseDouble(params[1]);
        double longitude = Double.parseDouble(params[2]);
        String address = params[3];
        String userGCMToken = params[4];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TaxiServices requestAPI = retrofit.create(TaxiServices.class);

        RequestTaxiDTO requestTaxi = new RequestTaxiDTO(userId, latitude, longitude, address, userGCMToken);

        Call<ServiceResponseDTO> requestTaxiDTOCall = requestAPI.requestTaxi(requestTaxi);
        requestTaxiDTOCall.enqueue(new Callback<ServiceResponseDTO>() {
            @Override
            public void onResponse(Call<ServiceResponseDTO> call, Response<ServiceResponseDTO> response) {
                if (response.isSuccessful()) {
                    ServiceResponseDTO payload = response.body();
                    if (payload != null && payload.getStatus().equals("created")) {
                        Log.i(TAG, "on taxi request" + payload.getStatus());
                        directionsDataResponse.onTaxiResponseAction(payload);
                    }
                }
            }

            @Override
            public void onFailure(Call<ServiceResponseDTO> call, Throwable t) {
                directionsDataResponse.onTaxiResponseAction(null);
            }
        });

        return null;
    }
}
