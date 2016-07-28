package com.trinus.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.common.base.Strings;
import com.trinus.R;
import com.trinus.repositori.api.gcm.RegistrationIntentService;
import com.trinus.util.Constants;

/**
 * Created by hetorres on 7/20/16.
 */
public class LoginActivity extends FragmentActivity {
    private final String TAG = LoginActivity.class.getSimpleName();
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private BroadcastReceiver registrationReceiver;
    private boolean receiverRegistered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        if (checkPlayServices()) {

            // Registering BroadcastReceiver
            registerReceiver();

            SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = prefs.edit();

            if (Strings.isNullOrEmpty(getRegistrationId())) {
                // new GCMHelperTask(this).execute(getApplicationContext());

                // Start IntentService to register this application with GCM.
                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            }

            findViewById(R.id.btnDriver).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putString(Constants.LOGGED_USER, Constants.DRIVER);
                    editor.commit();

                    Intent driver = new Intent(LoginActivity.this, DriverMapActivity.class);
                    startActivity(driver);
                }
            });

            findViewById(R.id.btnClient).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    editor.putString(Constants.LOGGED_USER, Constants.CLIENT);
                    editor.commit();

                    Intent client = new Intent(LoginActivity.this, ClientMapActivity.class);
                    startActivity(client);
                }
            });

            // Listener for Registration on Push notifications
            // TODO move to Rx Android or Otto.
            registrationReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    boolean response = intent.getExtras().getBoolean(Constants.REGISTRATION_COMPLETE);
                    Log.d(TAG, "Device registered");
                    if (!response) {
                        Toast.makeText(LoginActivity.this, "We couldn't register the device.", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "We couldn't register the device.");
                    }
                }
            };
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(registrationReceiver);
        receiverRegistered = false;
        super.onPause();
    }

    private void registerReceiver() {
        if (!receiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(registrationReceiver,
                    new IntentFilter(Constants.REGISTRATION_COMPLETE));
            receiverRegistered = true;
        }
    }

    private String getRegistrationId() {
        Log.d(TAG, "<< getRegistrationId");
        SharedPreferences prefs = getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        String previousRegId = prefs.getString(Constants.PROP_REG_ID, "");

        if (previousRegId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        } else {
            Log.d(TAG, "<< GCM (ID=" + previousRegId);
        }

        return previousRegId;
    }

    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                               .show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

}
