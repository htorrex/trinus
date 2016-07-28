package com.trinus.repositori.api.gcm;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.trinus.util.Constants;

/**
 * Created by hetorres on 7/25/16.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private Context context;

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // [START get_token]
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(Constants.SENDER_ID,
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            // [END get_token]
            Log.i(TAG, "<< NEW GCM Registration Token: " + token);

            // TODO send the new ID to our Backend

            storeRegistrationId(token);

        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
        }

        // Notify UI that registration has completed
        Intent registrationComplete = new Intent();
        registrationComplete.putExtra(Constants.REGISTRATION_COMPLETE, true);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegistrationId(final String regId) {
        Log.d(TAG, "<< setRegistrationId");
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.PREF_FILE,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(Constants.PROP_REG_ID, regId);

        editor.apply();
    }

}
