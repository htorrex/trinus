package com.trinus.repositori.api.gcm;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.trinus.model.MessageType;
import com.trinus.repositori.api.dto.ServiceResponseMessageDTO;
import com.trinus.repositori.api.dto.ServiceStatusMessageDTO;
import com.trinus.util.Constants;

/**
 * Class to receive the call back from GcmReceiver on Manifest and do some action.
 *
 * @author hetorres
 */
public class PushNotificationService extends GcmListenerService {
    private static final String TAG = "PushNotificationService";
    private LocalBroadcastManager broadcaster;

    @Override
    public void onCreate() {
        super.onCreate();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }

    /**
     * Called when message is received.
     *
     * @param from   SenderID of the sender.
     * @param bundle Data bundle containing message data as key/value pairs.
     *               For Set of keys use data.keySet().
     */
    @Override
    public void onMessageReceived(String from, Bundle bundle) {
        super.onMessageReceived(from, bundle);

        Log.d(TAG, "From: " + from);
        if (!bundle.isEmpty()) {
            Gson response = new Gson();
            try {
                String type = bundle.getString("type");
                String message = bundle.getString("message");

                if (!Strings.isNullOrEmpty(type)) {

                    //create intent to send with the response
                    Intent result = new Intent();
                    result.setAction(Constants.APP_PACKAGE);

                    MessageType messageTypeResponse = MessageType.valueOf(type);

                    switch (messageTypeResponse) {
                        case SERVICE_CHANGE_STATUS:
                            ServiceStatusMessageDTO serviceStatusMessage = response.fromJson(message, ServiceStatusMessageDTO.class);
                            Log.d(TAG, "IDService: " + serviceStatusMessage.getIdService());
                            Log.d(TAG, "driversName: " + serviceStatusMessage.getDriver().getName());
                            Log.d(TAG, "plates: " + serviceStatusMessage.getVehicle().getPlates());
                            Log.d(TAG, "coordinates: " + serviceStatusMessage.getVehicle().getLatitude() + " , " +
                                    serviceStatusMessage.getVehicle().getLongitude());

                            result.putExtra(Constants.MESSAGE, serviceStatusMessage);
                            broadcaster.sendBroadcast(result);
                            break;

                        case NEW_SERVICE:
                            ServiceResponseMessageDTO serviceMessage = response.fromJson(message, ServiceResponseMessageDTO.class);
                            Log.d(TAG, "IDService: " + serviceMessage.getIdService());
                            Log.d(TAG, "destinationAddress: " + serviceMessage.getDestinationAddress());

                            result.putExtra(Constants.MESSAGE, serviceMessage);
                            broadcaster.sendBroadcast(result);
                            break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                e.printStackTrace();
            }
        }

    }
}
