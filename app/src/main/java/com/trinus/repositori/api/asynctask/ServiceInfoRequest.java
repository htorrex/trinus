package com.trinus.repositori.api.asynctask;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.trinus.repositori.api.TaxiServices;
import com.trinus.repositori.api.dto.ServiceStatusMessageDTO;
import com.trinus.util.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hetorres on 7/23/16.
 */
public class ServiceInfoRequest extends AsyncTask<String, Void, Void> {
    private static final String TAG = ServiceInfoRequest.class.getSimpleName();
    private ServiceInfoResponse serviceInfoResponse;

    public ServiceInfoRequest(SupportMapFragment fragment) {
        serviceInfoResponse = (ServiceInfoResponse) fragment;
    }

    public interface ServiceInfoResponse {
        void onServiceInfoAction(ServiceStatusMessageDTO messageDTO);
    }

    @Override
    protected Void doInBackground(String... params) {
        String idService = params[0];

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.HOST)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        TaxiServices requestAPI = retrofit.create(TaxiServices.class);

        Call<ServiceStatusMessageDTO> serviceById = requestAPI.getServiceById(idService);
        serviceById.enqueue(new Callback<ServiceStatusMessageDTO>() {
            @Override
            public void onResponse(Call<ServiceStatusMessageDTO> call, Response<ServiceStatusMessageDTO> response) {
                if (response.isSuccessful()) {
                    ServiceStatusMessageDTO payload = response.body();
                    if (payload != null) {
                        Log.d(TAG, "<<<<< idService:" + payload.getIdService());
                        serviceInfoResponse.onServiceInfoAction(payload);
                    }
                }
            }

            @Override
            public void onFailure(Call<ServiceStatusMessageDTO> call, Throwable t) {
                Log.d(TAG, "<<<<<<<<<<<<< onFailure  ServiceInfoRequest >>>>>>>>>>>>");
                serviceInfoResponse.onServiceInfoAction(null);
            }
        });

        return null;
    }
}
