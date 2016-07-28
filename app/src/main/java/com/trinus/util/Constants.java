package com.trinus.util;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.Map;

/**
 * Created by hetorres on 7/20/16.
 */
public final class Constants {
    public static final String HOST = "http://207.210.66.71";
    public static final String PREF_FILE = "TRINUS";
    public static final String LOGGED_USER = "loggedUser";
    public static final String CLIENT = "client";
    public static final String DRIVER = "driver";
    public static final String PROP_REG_ID = "registration_id";
    public static final String SENDER_ID = "344437097854";
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String MESSAGE = "message";
    public static final String APP_PACKAGE = "com.trinus";
    public static final String ORIGIN = "marker1";
    public static final String DESTINATION = "marker2";
    public static final String TAXI_DRIVER = "marker3";
    public static final String SUGGESTED_PATH = "marker4";
    public static final String USER_MARKER = "marker5";
    public static final String CANCELED_BY_USER = "CANCELED_BY_USER";
    public static final String CANCELED_BY_DRIVER = "CANCELED_BY_DRIVER";
    public static final String ACCEPTED = "ACCEPTED";
    public static final String ARRIVING = "ARRIVING";
    public static final String DONE = "DONE";


    //  FIXME move this to an utility class
    public static void centerMarkersOnMap(Map<String, Object> markers, GoogleMap googleMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Object object : markers.values()) { //building the bounds for the including markersClient(should be origin - destination)
            // and taxi once we got it.
            if (object instanceof Marker) {
                Marker marker = (Marker) object;
                builder.include(marker.getPosition());
            }
        }
        LatLngBounds bounds = builder.build();
        int padding = 120; // offset from edges of the map in pixels.
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        // Set the camera to the greatest possible zoom level that includes the bounds
        googleMap.moveCamera(cu);
        // Zoom in, animating the camera.
        googleMap.animateCamera(cu);
    }
}
