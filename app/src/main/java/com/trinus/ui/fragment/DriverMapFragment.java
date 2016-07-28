package com.trinus.ui.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.trinus.R;
import com.trinus.repositori.api.asynctask.DirectionsHelperTask;
import com.trinus.repositori.api.asynctask.UpdateDriversLocationRequest;
import com.trinus.repositori.api.asynctask.UpdateServiceStatusRequest;
import com.trinus.repositori.api.dto.DirectionsDTO;
import com.trinus.repositori.api.dto.MessageDTO;
import com.trinus.repositori.api.dto.ServiceResponseMessageDTO;
import com.trinus.repositori.api.dto.ServiceStatusMessageDTO;
import com.trinus.ui.activity.PermissionsActivity;
import com.trinus.util.Constants;
import com.trinus.util.PermissionsHelper;
import com.trinus.util.RouteDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DriverMapFragment extends SupportMapFragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback, DirectionsHelperTask.DirectionsDataResponse, LocationListener,
        UpdateDriversLocationRequest.DriversLocationResponse, UpdateServiceStatusRequest.ServiceStatusResponse {

    private static final String TAG = DriverMapFragment.class.getSimpleName();
    private static final String[] PERMISSIONS = new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission
            .ACCESS_COARSE_LOCATION};
    private static final int REQUEST_LOCATION = 200;
    // FIXME make this dynamic
    public final String driverId = "AC847627-111D-A31D-FF83-80E617001800";

    //FIXME using object since we have Marker and Polyline classes we can use a proper model.
    private Map<String, Object> markersDriver = Maps.newHashMap();

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;


    private GoogleMap googleMap;
    private SupportMapFragment supportMap;
    private GoogleApiClient googleApiClient;

    private Location driverLocation;
    private LatLng userCoordinates;
    private PermissionsHelper permissionsHelper;

    private View view;
    private LinearLayout driverLayout;
    private Button btnStatus;
    private String tokenGCM;
    private ServiceResponseMessageDTO serviceRequestDTO;
    private ServiceStatusMessageDTO serviceStatusMessageDTO;
    private BroadcastReceiver driverReceiver;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    protected LocationRequest locationRequest;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle arg) {
        super.onCreateView(inflater, container, arg);
        view = inflater.inflate(R.layout.map_driver_fragment, container, false);

        // Components
        btnStatus = (Button) view.findViewById(R.id.btnStatus);
        driverLayout = (LinearLayout) view.findViewById(R.id.driver_layout);

        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new UpdateServiceStatusRequest(DriverMapFragment.this).execute(serviceRequestDTO.getIdService(),
                        String.valueOf(driverLocation.getLatitude()),
                        String.valueOf(driverLocation.getLongitude()),
                        btnStatus.getText().toString(),
                        driverId);

                if (btnStatus.getText().toString().equals("ARRIVING")) {
                    btnStatus.setText("DONE");
                } else if (btnStatus.getText().toString().equals("DONE")) {
                    Toast.makeText(getActivity(), "Finishing Driver's flow.", Toast.LENGTH_LONG).show();
                    getActivity().finish();
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated*");

        permissionsHelper = new PermissionsHelper(getActivity());
        loadMap();
        buildGoogleApiClient();

        // preferences to get the token
        SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
        tokenGCM = prefs.getString(Constants.PROP_REG_ID, "");

        // Listener for Push notifications messaging
        // TODO move to Rx Android or Otto.
        driverReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getExtras() != null) {
                    Bundle extras = intent.getExtras();
                    MessageDTO messageDTO = (MessageDTO) extras.getSerializable(Constants.MESSAGE);

                    SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                    String currentUser = prefs.getString(Constants.LOGGED_USER, "");

                    if (!Strings.isNullOrEmpty(currentUser) && Constants.DRIVER.equals(currentUser)) {
                        if (messageDTO instanceof ServiceResponseMessageDTO) {
                            serviceRequestDTO = (ServiceResponseMessageDTO) messageDTO;
                            Log.d(TAG, ":: PUSH RESPONSE MESSAGE NEW SERVICE TO DRIVER ::");

                            Log.d(TAG, "IDService: " + serviceRequestDTO.getIdService());
                            Log.d(TAG, "address: " + serviceRequestDTO.getDestinationAddress());
                            userCoordinates = new LatLng(serviceRequestDTO.getUserLatitude(), serviceRequestDTO.getUserLongitude());
                            showDialog(userCoordinates);

                        } else if (messageDTO instanceof ServiceStatusMessageDTO) {
                            serviceStatusMessageDTO = (ServiceStatusMessageDTO) messageDTO;
                            Log.d(TAG, ":: PUSH RESPONSE STATUS SERVICE DRIVER ::");

                            Log.d(TAG, "IDService: " + serviceStatusMessageDTO.getIdService());
                            Log.d(TAG, "status: " + serviceStatusMessageDTO.getStatus());

                            if (serviceStatusMessageDTO.getStatus().equals(Constants.CANCELED_BY_USER)) {
                                //cleaning all
                                googleMap.clear();
                                btnStatus.setEnabled(false);
                                driverLayout.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onStart() {
        Log.i(TAG, ":: onStart() ::");
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, ":: onResume() ::");
        if (googleApiClient.isConnected()) {
            startLocationUpdates();
        }

        //TODO move this to a Utility class or change for RxAndroid.
        // We have this inside onResume() instead onStart() because we launch another screen and the service stop listening
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.APP_PACKAGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((driverReceiver), filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();

        //TODO move this to a Utility class or change for RxAndroid.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(driverReceiver);
    }

    @Override
    public void onStop() {
        Log.i(TAG, ":: onStop() ::");
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * ============== Private Methods ==============
     */

    @SuppressWarnings("MissingPermission")
    private void showLocation() {
        if (driverLocation != null) {
            Log.d(TAG, "<< Lat: " + String.valueOf(driverLocation.getLatitude()));
            Log.d(TAG, "<< Lng: " + String.valueOf(driverLocation.getLongitude()));
            googleMap.setMyLocationEnabled(true);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()), 18));
            startLocationUpdates();
        }
    }

    protected void createLocationRequest() {
        locationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @SuppressWarnings("MissingPermission")
    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    /**
     * Builds a GoogleApiClient instance.
     */
    private synchronized void buildGoogleApiClient() {
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();
        }
    }

    private void callPermissionsActivity() {
        Log.i(TAG, "callPermissionsActivity()");
        Intent intent = new Intent(getActivity(), PermissionsActivity.class);
        intent.putExtra(PermissionsActivity.PERMISSIONS_KEY, PERMISSIONS);
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    @SuppressWarnings({"MissingPermission"})
    private void loadMap() {
        Log.i(TAG, "loadMap()");
        supportMap = new SupportMapFragment() {
            @Override
            public void onActivityCreated(Bundle savedInstanceState) {
                super.onActivityCreated(savedInstanceState);
                supportMap.getMapAsync(DriverMapFragment.this);
            }
        };

        getChildFragmentManager().beginTransaction().add(R.id.map, supportMap).commit();
    }

    /**
     * ============== Google Connection CallBacks ==============
     */

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    @SuppressWarnings("MissingPermission")
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        if (permissionsHelper.permissionsCheck(PERMISSIONS)) {
            callPermissionsActivity();
        } else {
            if (driverLocation == null) {
                driverLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                showLocation();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended()");
        // The connection to Google Play services was lost for some reason. We call connect() to attempt to re-establish the connection.
        googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "onConnectionFailed()" + connectionResult.getErrorCode());
    }

    /**
     * ======= MapLoading =======
     */
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setPadding(0, 0, 0, 400); // showing google logo.
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    /**
     * ======= ActivityForResult CallBack =======
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Permissions actions
        if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED && requestCode == REQUEST_LOCATION) {
            showLocation(); //first time
        } else if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            getActivity().finish();
        }
    }

    private void showDialog(final LatLng userCoordinates) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCancelable(false)
               .setMessage("New Service")
               .setPositiveButton("Take service", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {

                       new UpdateServiceStatusRequest(DriverMapFragment.this).execute(serviceRequestDTO.getIdService(),
                               String.valueOf(driverLocation.getLatitude()),
                               String.valueOf(driverLocation.getLongitude()), "ACCEPTED", driverId);

                       new DirectionsHelperTask(DriverMapFragment.this).execute(
                               new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()),
                               userCoordinates);
                   }
               })
               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       new UpdateServiceStatusRequest(DriverMapFragment.this).execute(serviceRequestDTO.getIdService(),
                               String.valueOf(serviceRequestDTO.getUserLatitude()),
                               String.valueOf(serviceRequestDTO.getUserLongitude()), Constants.CANCELED_BY_DRIVER, driverId);

                       // User cancelled the dialog
                       dialog.dismiss();
                   }
               });

        builder.create().show();
    }

    @Override
    public void onDirectionsAction(DirectionsDTO response) {
        if (response != null) {
            drawPath(response);
        }
    }

    private void drawPath(DirectionsDTO directions) {
        ArrayList<LatLng> routeList = new ArrayList<>();
        if (!directions.getRoutes().isEmpty()) {
            ArrayList<LatLng> decodeList;
            DirectionsDTO.Route routeA = directions.getRoutes().get(0);
            Log.i(TAG, "Legs length : " + routeA.getLegs().size());
            if (!routeA.getLegs().isEmpty()) {
                List<DirectionsDTO.Step> steps = routeA.getLegs().get(0).getSteps();
                Log.i(TAG, "Steps size :" + steps.size());
                DirectionsDTO.Step step;
                DirectionsDTO.StepLocation stepLocation;
                String polyline;
                for (int i = 0; i < steps.size(); i++) {
                    step = steps.get(i);
                    stepLocation = step.getStartLocation();
                    routeList.add(new LatLng(stepLocation.getLat(), stepLocation.getLng()));
                    Log.i(TAG, "Start Location :" + stepLocation.getLat() + ", " + stepLocation.getLng());
                    polyline = step.getPolyline().getPoints();
                    decodeList = RouteDecoder.getDecodePolyline(polyline);
                    routeList.addAll(decodeList);
                    stepLocation = step.getEndLocation();
                    routeList.add(new LatLng(stepLocation.getLat(), stepLocation.getLng()));
                    Log.i(TAG, "End Location :" + stepLocation.getLat() + ", " + stepLocation.getLng());
                }
            }
        }
        Log.i(TAG, "routeList size : " + routeList.size());
        if (!routeList.isEmpty()) {
            PolylineOptions rectLine = new PolylineOptions()
                    .width(15)
                    .color(R.color.primary);

            for (int i = 0; i < routeList.size(); i++) {
                rectLine.add(routeList.get(i));
            }

            // Adding route on the map
            googleMap.addPolyline(rectLine);

            // adding marker of the user location.
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(userCoordinates);
            Marker userLocationMarker = googleMap.addMarker(markerOptions);
            markersDriver.put(Constants.USER_MARKER, userLocationMarker);

            // showing button to change status
            btnStatus.setText(Constants.ARRIVING);
            btnStatus.setEnabled(true);
            driverLayout.setVisibility(View.VISIBLE);

            Constants.centerMarkersOnMap(markersDriver, googleMap);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        driverLocation = location;

        Log.d(TAG, "<<< Driver Coordinates: " + driverLocation.getLatitude() + " , " + driverLocation.getLongitude());

        new UpdateDriversLocationRequest(this).execute(driverId, String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()), tokenGCM);

        // TODO improve this probably in a utility class
        if (markersDriver.containsKey(Constants.TAXI_DRIVER)) {
            Log.d(TAG, "<<< Driver Marker PUSH");
            Marker marker = (Marker) markersDriver.get(Constants.TAXI_DRIVER);
            marker.setPosition(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()));
        } else {
            // taxi marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_icon));
            Marker marker = googleMap.addMarker(markerOptions);
            markersDriver.put(Constants.TAXI_DRIVER, marker);
        }

        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude()), 18));
    }

    @Override
    public void onDriverLocationResponse(boolean result) {
        if (!result) {
            Log.d(TAG, "We couldn't update your location.");
            Toast.makeText(getActivity(), "We couldn't update your location.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onServiceStatusResponse(boolean result) {
        if (!result) {
            Log.d(TAG, "We couldn't change the status of the service.");
            Toast.makeText(getActivity(), "We couldn't change the status of the service.", Toast.LENGTH_LONG).show();
        }
    }
}
