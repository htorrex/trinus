package com.trinus.ui.fragment;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.trinus.R;
import com.trinus.model.MessageType;
import com.trinus.repositori.api.asynctask.AddressHelperTask;
import com.trinus.repositori.api.asynctask.DirectionsHelperTask;
import com.trinus.repositori.api.asynctask.ServiceInfoRequest;
import com.trinus.repositori.api.asynctask.TaxiLocationRequest;
import com.trinus.repositori.api.asynctask.TaxisServiceRequest;
import com.trinus.repositori.api.asynctask.UpdateServiceStatusRequest;
import com.trinus.repositori.api.dto.DirectionsDTO;
import com.trinus.repositori.api.dto.MessageDTO;
import com.trinus.repositori.api.dto.ServiceResponseDTO;
import com.trinus.repositori.api.dto.ServiceStatusDTO;
import com.trinus.repositori.api.dto.ServiceStatusMessageDTO;
import com.trinus.ui.activity.PermissionsActivity;
import com.trinus.util.Constants;
import com.trinus.util.MapTouchableWrapper;
import com.trinus.util.PermissionsHelper;
import com.trinus.util.RouteDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class ClientMapFragment extends SupportMapFragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, MapTouchableWrapper.MapTouchable,
        AddressHelperTask.AddressDataResponse, DirectionsHelperTask.DirectionsDataResponse, TaxisServiceRequest.TaxiServiceDataResponse,
        UpdateServiceStatusRequest.ServiceStatusResponse, TaxiLocationRequest.TaxiLocationResponse, ServiceInfoRequest.ServiceInfoResponse {

    private static final String TAG = ClientMapFragment.class.getSimpleName();
    private static final String[] PERMISSIONS = new String[] {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission
            .ACCESS_COARSE_LOCATION};
    private static final int REQUEST_LOCATION = 200;
    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 300;

    private GoogleMap googleMap;
    private SupportMapFragment supportMap;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private PermissionsHelper permissionsHelper;
    private LatLng userPosition;
    private Timer timer;

    private View view;
    private TextView txtAddress;
    private Button btnPickUp;
    private ImageView btnBack;
    private Button btnRequestTaxi;
    private ImageView imageUserPosition;
    private View requestTaxiLayout;
    private View pickUpLayout;
    private View driversLayout;
    private View cancelLayout;
    private TextView txtOrigin;
    private TextView txtDestination;
    private boolean pickUpPressed;
    //FIXME using object since we have Marker and Polyline classes we can use a proper model.
    private Map<String, Object> markersClient = Maps.newHashMap();

    private LatLng centerFromPoint;
    private MapTouchableWrapper mapTouchableWrapper;
    private ProgressDialog progressDialog;
    private ProgressDialog loading;
    private BroadcastReceiver broadcastReceiver;
    private ServiceResponseDTO serviceResponseDTO;

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
        view = inflater.inflate(R.layout.map_client_fragment, container, false);

        // Components
        txtAddress = (TextView) view.findViewById(R.id.txtAddress);
        btnPickUp = (Button) view.findViewById(R.id.btnPickUp);
        btnBack = (ImageView) view.findViewById(R.id.btnBack);
        imageUserPosition = (ImageView) view.findViewById(R.id.marker);
        requestTaxiLayout = view.findViewById(R.id.request_taxi_layout);
        pickUpLayout = view.findViewById(R.id.pick_up_layout);
        driversLayout = view.findViewById(R.id.drivers_layout);
        cancelLayout = view.findViewById(R.id.cancel_layout);

        txtOrigin = (TextView) view.findViewById(R.id.txtOrigin);
        txtDestination = (TextView) view.findViewById(R.id.txtDestination);
        btnRequestTaxi = (Button) view.findViewById(R.id.btnRequestTaxi);

        txtOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPickUp();
            }
        });

        btnPickUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPickUp();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPickUp();
            }
        });

        btnRequestTaxi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.setMessage("Requesting taxi...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setIndeterminate(true);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                // preferences to get the token
                SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                String previousRegId = prefs.getString(Constants.PROP_REG_ID, "");

                new TaxisServiceRequest(ClientMapFragment.this).execute("abc", String.valueOf(userPosition.latitude),
                        String.valueOf(userPosition.longitude), txtAddress.getText().toString(),
                        previousRegId);
            }
        });

        cancelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPickUp();
                driversLayout.setVisibility(View.GONE);

                if (serviceResponseDTO != null) {
                    new ServiceInfoRequest(ClientMapFragment.this).execute(serviceResponseDTO.getObjectId());
                }
            }
        });

        txtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading = new ProgressDialog(getActivity());
                loading.setMessage("loading..");
                loading.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                loading.setIndeterminate(true);
                loading.setCanceledOnTouchOutside(false);
                loading.show();
                findPlace(view);
            }
        });

        mapTouchableWrapper = new MapTouchableWrapper(this);
        mapTouchableWrapper.addView(view);

        return mapTouchableWrapper;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated*");

        permissionsHelper = new PermissionsHelper(getActivity());
        loadMap();
        buildGoogleApiClient();

        progressDialog = new ProgressDialog(getActivity());

        // Listener for Push notifications messaging
        // TODO move to Rx Android or Otto.
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, ":: <<< onReceive() CLIENT >>>>::");
                if (intent.getExtras() != null) {
                    Bundle extras = intent.getExtras();
                    MessageDTO messageDTO = (MessageDTO) extras.getSerializable(Constants.MESSAGE);

                    SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREF_FILE, Context.MODE_PRIVATE);
                    String currentUser = prefs.getString(Constants.LOGGED_USER, "");

                    if (!Strings.isNullOrEmpty(currentUser) && Constants.CLIENT.equals(currentUser)) {
                        if (messageDTO instanceof ServiceStatusMessageDTO) {
                            final ServiceStatusMessageDTO serviceStatusMessageDTO = (ServiceStatusMessageDTO) messageDTO;
                            Log.d(TAG, ":: PUSH RESPONSE CLIENT ::");

                            Log.d(TAG, "IDService: " + serviceStatusMessageDTO.getIdService());
                            Log.d(TAG, "driversName: " + serviceStatusMessageDTO.getDriver().getName());
                            Log.d(TAG, "plates: " + serviceStatusMessageDTO.getVehicle().getPlates());
                            Log.d(TAG, "coordinates: " + serviceStatusMessageDTO.getVehicle().getLatitude() + " , " +
                                    serviceStatusMessageDTO.getVehicle().getLongitude());
                            Log.d(TAG, "Status: " + serviceStatusMessageDTO.getStatus());

                            if (serviceStatusMessageDTO.getStatus().equals(Constants.ACCEPTED)) {

                                // dismissing the dialog
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }

                                // showing the taxi information
                                driversLayout.setVisibility(View.VISIBLE);
                                pickUpLayout.setVisibility(View.GONE);
                                requestTaxiLayout.setVisibility(View.GONE);

                                //printing results on the UI.
                                TextView txtVehicle = (TextView) driversLayout.findViewById(R.id.txtVehicle);
                                txtVehicle.setText(serviceStatusMessageDTO.getVehicle().getModel());

                                TextView txtPlates = (TextView) driversLayout.findViewById(R.id.txtPlates);
                                txtPlates.setText(serviceStatusMessageDTO.getVehicle().getPlates());

                                TextView txtDriversName = (TextView) driversLayout.findViewById(R.id.txtDriversName);
                                txtDriversName.setText(serviceStatusMessageDTO.getDriver().getName());

                                if (timer == null) {
                                    timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            Log.d(TAG, "<<<<<<<<<<<<< Executing timer  >>>>>>>>>>>>");
                                            new TaxiLocationRequest(ClientMapFragment.this).execute(serviceStatusMessageDTO.getIdService());
                                        }
                                    }, 0, 10000);
                                }

                                updateMarkerAndCenterBounds(serviceStatusMessageDTO);

                            } else if (serviceStatusMessageDTO.getStatus().equals(Constants.ARRIVING)) {

                                updateMarkerAndCenterBounds(serviceStatusMessageDTO);
                                showNotification("Trinus", "Your Taxi is " + serviceStatusMessageDTO.getStatus());

                            } else if (serviceStatusMessageDTO.getStatus().equals(Constants.DONE) && timer != null) {
                                //stop making request for drivers coordinates.
                                if (timer != null) {
                                    timer.cancel();
                                    timer.purge();
                                    timer = null;
                                }
                                Log.d(TAG, ":::: CANCELING TIMER :::: ");

                                Toast.makeText(getActivity(), "Finishing Client flow.", Toast.LENGTH_LONG).show();
                                getActivity().finish();
                            } else if (serviceStatusMessageDTO.getStatus().equals(Constants.CANCELED_BY_DRIVER)) {
                                // this is the first alert when the driver get the service
                                //TODO handle this scenario
                                if (progressDialog != null) {
                                    progressDialog.dismiss();
                                }

                                Toast.makeText(getActivity(), "Driver canceled, please make request again.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getActivity(), "Client does not support this operation", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        };
    }

    private void updateMarkerAndCenterBounds(ServiceStatusMessageDTO messageDTO) {
        //TODO improve this probably in a utility class
        if (markersClient.containsKey(Constants.TAXI_DRIVER)) {
            Log.d(TAG, "<<< Driver Marker PUSH");
            Marker marker = (Marker) markersClient.get(Constants.TAXI_DRIVER);
            marker.setPosition(new LatLng(messageDTO.getVehicle().getLatitude(),
                    messageDTO.getVehicle().getLongitude()));
        } else {
            // taxi marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(new LatLng(messageDTO.getVehicle().getLatitude(),
                    messageDTO.getVehicle().getLongitude()));
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.taxi_icon));
            Marker marker = googleMap.addMarker(markerOptions);
            markersClient.put(Constants.TAXI_DRIVER, marker);
        }

        //limits on the view based on markersClient.
        Constants.centerMarkersOnMap(markersClient, googleMap);
    }

    private void showNotification(String title, String content) {
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getActivity())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(Notification.DEFAULT_ALL);

        NotificationManager mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(123, notification.build());
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

        //TODO move this to a Utility class or change for RxAndroid.
        // We have this inside onResume() instead onStart() because we launch another screen and the service stop listening
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.APP_PACKAGE);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver((broadcastReceiver), filter);
    }

    @Override
    public void onPause() {
        super.onPause();

        //TODO move this to a Utility class or change for RxAndroid.
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onStop() {
        Log.i(TAG, ":: onStop() ::");
        if (googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public View getView() {
        return view;
    }


    /**
     * ============== Private Methods ==============
     */

    private void postProcessTaxi(final ServiceResponseDTO responseDTO) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (responseDTO != null) {
                    Toast.makeText(getActivity(), "Service created", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "<< SERVICE:" + responseDTO.getObjectId());
                    progressDialog.setMessage("Looking for drivers nearby to you.");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                } else {
                    Log.d(TAG, "<< We couldn't create the service, try again later..");
                    Toast.makeText(getActivity(), "We couldn't create the service, try again later", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void findPlace(View view) {
        try {

            //            AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
            //                    .setTypeFilter(AutocompleteFilter.TYPE_FILTER_REGIONS)
            //                    .build();

            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    //                            .setFilter(typeFilter)
                    .setBoundsBias(googleMap.getProjection().getVisibleRegion().latLngBounds)
                    .build(getActivity());

            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, "GooglePlayServicesRepairableException: " + e.getMessage());
            e.printStackTrace();
            // TODO: Handle the error.
        } catch (GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
            Log.e(TAG, "GooglePlayServicesNotAvailableException: " + e.getMessage());
            e.printStackTrace();
        }
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
                supportMap.getMapAsync(ClientMapFragment.this);
            }
        };

        getChildFragmentManager().beginTransaction().add(R.id.map, supportMap).commit();
    }

    @SuppressWarnings({"MissingPermission"})
    private void showLocation(LatLng latLng) {
        txtAddress.setText("Updating Location...");
        if (latLng == null) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                Log.d(TAG, "<< Lat: " + String.valueOf(lastLocation.getLatitude()));
                Log.d(TAG, "<< Lng: " + String.valueOf(lastLocation.getLongitude()));
                googleMap.setMyLocationEnabled(true);
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 18));
                new AddressHelperTask(this).execute(lastLocation.getLatitude(), lastLocation.getLongitude());
            }
        } else {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), 18));
            new AddressHelperTask(this).execute(latLng.latitude, latLng.longitude);
        }
    }

    private void setPickUp() {
        double latitude = 0.0d;
        double longitude = 0.0d;

        if (centerFromPoint != null) {
            latitude = centerFromPoint.latitude;
            longitude = centerFromPoint.longitude;
        } else if (lastLocation != null) {
            latitude = lastLocation.getLatitude();
            longitude = lastLocation.getLongitude();
        }

        userPosition = new LatLng(latitude, longitude);

        //Add current location of the pin as a marker
        MarkerOptions markerOptions = new MarkerOptions();
        //        markerOptions.title("Title");
        //        markerOptions.snippet("Snippet");
        markerOptions.position(userPosition);
        //mkoptionsTaxista.icon(BitmapDescriptorFactory.fromResource(obtenerIconoTaxi(taxiRastreado.getEstatusServicio())));
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_pink));
        Marker marker = googleMap.addMarker(markerOptions);
        markersClient.put(Constants.ORIGIN, marker);

        imageUserPosition.setVisibility(View.GONE);

        //Switch de UI in order to show the pickup and the destination
        pickUpLayout.setVisibility(View.GONE);
        requestTaxiLayout.setVisibility(View.VISIBLE);
        btnBack.setVisibility(View.VISIBLE);
        //FIXME moving this to get Address for now
        //        txtOrigin.setText(txtAddress.getText().toString());
        pickUpPressed = true;
    }

    private void resetPickUp() {
        markersClient.clear();
        googleMap.clear();

        imageUserPosition.setVisibility(View.VISIBLE);

        //Switch de UI in order to show the pickup and the destination
        pickUpLayout.setVisibility(View.VISIBLE);

        txtDestination.setText("Add destination");
        btnRequestTaxi.setEnabled(false);

        requestTaxiLayout.setVisibility(View.GONE);
        btnBack.setVisibility(View.GONE);
        pickUpPressed = false;

        if (Strings.isNullOrEmpty(txtAddress.getText().toString())) {
            btnPickUp.setEnabled(false);
        }

        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /**
     * ============== Google Connection CallBacks ==============
     */

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "onConnected()");
        if (permissionsHelper.permissionsCheck(PERMISSIONS)) {
            callPermissionsActivity();
        } else {
            showLocation(null);
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
     * ======= ActivityForResult CallBack =======
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Permissions actions
        if (resultCode == PermissionsActivity.PERMISSIONS_GRANTED && requestCode == REQUEST_LOCATION) {
            showLocation(null); //first time
        } else if (resultCode == PermissionsActivity.PERMISSIONS_DENIED) {
            getActivity().finish();
        }

        // A place has been received; use requestCode to track the request.
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {

            if (loading != null) {
                loading.dismiss();
            }

            if (resultCode == getActivity().RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                Log.i(TAG, "** Place: " + place.getName());
                Log.i(TAG, "** Address: " + place.getAddress());
                Log.i(TAG, "** LatLng: " + place.getLatLng());

                //check if we have a previous marker and removing it.
                if (!markersClient.isEmpty()) {
                    if (markersClient.containsKey("marker2")) {
                        Marker oldMarker = (Marker) markersClient.get("marker2");
                        oldMarker.remove(); //removing from the map
                        markersClient.remove(oldMarker);
                    }

                    // removing old path if need it.
                    if (markersClient.containsKey(Constants.SUGGESTED_PATH)) {
                        Polyline currentPolyline = (Polyline) markersClient.get(Constants.SUGGESTED_PATH);
                        currentPolyline.remove(); //removing the old path.
                    }
                }

                // create a generic method to add markersClient
                //Add current location of the pin as a marker
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("Destination");
                markerOptions.snippet("Snippet");
                markerOptions.position(place.getLatLng());
                //mkoptionsTaxista.icon(BitmapDescriptorFactory.fromResource(obtenerIconoTaxi(taxiRastreado.getEstatusServicio())));
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.pin_blue));
                Marker marker2 = googleMap.addMarker(markerOptions);

                markersClient.put(Constants.DESTINATION, marker2);
                txtDestination.setText(place.getName());
                btnRequestTaxi.setEnabled(true);

                Constants.centerMarkersOnMap(markersClient, googleMap);

                new DirectionsHelperTask(this).execute(userPosition, place.getLatLng());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                // TODO: Handle the error.
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d(TAG, "<< CANCELED");
            }
        }
    }


    /**
     * ======= MapTouch =======
     */
    @Override
    public void OnMapTouched(float posX, float posY) {

        // Notice:  DELETE not using x,y for now, we are getting the location from the center of the screen

        if (!pickUpPressed) {
            VisibleRegion visibleRegion = googleMap.getProjection().getVisibleRegion();
            Point x = googleMap.getProjection().toScreenLocation(visibleRegion.farRight);
            Point y = googleMap.getProjection().toScreenLocation(visibleRegion.nearLeft);
            Point centerPoint = new Point(x.x / 2, y.y / 2);
            centerFromPoint = googleMap.getProjection().fromScreenLocation(centerPoint);
            showLocation(centerFromPoint);
        }

        //            mapLatLng = googleMap.getCameraPosition().target; //The location that the camera is pointing at.
        //            googleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
        //                @Override
        //                public void onMapLoaded() {
        //                    mapLatLng = googleMap.getCameraPosition().target; //The location that the camera is pointing at.
        //                }
        //            });
    }

    /**
     * ======= MapLoading =======
     */
    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setPadding(0, 0, 0, 400); // showing google logo.
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (!pickUpPressed) {
                    showLocation(null);
                }
                return false;
            }
        });
    }

    /**
     * ======= AddressHelperTask CallBack =======
     */
    @Override
    public void OnAddressAction(String address) {
        txtAddress.setText(address);
        txtOrigin.setText(address);
        if (!Strings.isNullOrEmpty(address)) {
            if (!pickUpPressed && !address.equals("No destinationAddress found")) {
                btnPickUp.setEnabled(true);
            } else if (pickUpPressed && !txtDestination.getText().equals("Add destination")) {
                btnRequestTaxi.setEnabled(true);
            }
        } else {
            btnPickUp.setEnabled(false);
            btnRequestTaxi.setEnabled(false);
        }
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
            Polyline polyline = googleMap.addPolyline(rectLine);
            markersClient.put(Constants.SUGGESTED_PATH, polyline);
        }
    }

    @Override
    public void onTaxiResponseAction(ServiceResponseDTO response) {
        postProcessTaxi(response);
        serviceResponseDTO = response;
    }

    @Override
    public void onServiceStatusResponse(boolean result) {
        if (!result) {
            Log.d(TAG, "We couldn't change the status of the service.");
            Toast.makeText(getActivity(), "We couldn't change the status of the service.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onTaxiLocationAction(LatLng coordinates) {
        //TODO update marker taxi with this coordinates.
        Log.d(TAG, "<<< Getting taxi Coordinates");
        Toast.makeText(getActivity(), "Getting taxi Coordinates", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "<<< coordinates: " + coordinates.latitude + " , " + coordinates.longitude);

        if (markersClient.containsKey(Constants.TAXI_DRIVER)) {
            Log.d(TAG, "<<< Refreshing Driver Marker");
            Marker marker = (Marker) markersClient.get(Constants.TAXI_DRIVER);
            marker.setPosition(new LatLng(coordinates.latitude, coordinates.longitude));
        }
    }

    @Override
    public void onServiceInfoAction(ServiceStatusMessageDTO messageDTO) {
        new UpdateServiceStatusRequest(ClientMapFragment.this).execute(messageDTO.getIdService(),
                String.valueOf(userPosition.latitude),
                String.valueOf(userPosition.longitude),
                Constants.CANCELED_BY_USER,
                messageDTO.getDriver().getObjectId());
    }
}
