package com.trinus.repositori.api.asynctask;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.trinus.ui.fragment.ClientMapFragment;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Helper class to handle the destinationAddress information based on coordinates
 *
 * @author hetorres
 */
public class AddressHelperTask extends AsyncTask<Double, Void, String> {

    private static final String TAG = AddressHelperTask.class.getSimpleName();
    private AddressDataResponse dataResponse;
    private Context context;

    public interface AddressDataResponse {
        void OnAddressAction(String address);
    }

    public AddressHelperTask(ClientMapFragment pickUpFragment) {
        dataResponse = pickUpFragment;
        context = pickUpFragment.getActivity();
    }

    @Override
    protected String doInBackground(Double[] params) {

        double lat = (double) params[0];
        double lng = (double) params[1];

        String address = getAddress(lat, lng);
        Log.d(TAG, "destinationAddress: " + address);

        if(address.isEmpty() || address.equals("")){
            return "";
        }

        String street = null, city = null;
        Scanner scanner = new Scanner(address.toString());
        scanner.useDelimiter("_");
        int param = 1;
        while (scanner.hasNext() && param < 3) {
            Log.d(TAG, "" + param);
            if (param == 1) { //street
                street = scanner.next();
            } else if (param == 2) { //city
                city = scanner.next();
            }
            param++;
        }

        return street;
    }

    @Override
    protected void onPostExecute(String result) {
        dataResponse.OnAddressAction(result);
    }

    /**
     * Method that helps to get the Address from the coordinates.
     *
     * @param lat
     * @param lng
     *
     * @return String with the destinationAddress
     */
    public String getAddress(Double lat, Double lng) {
        String result = "";
        StringBuilder addressBuilder = new StringBuilder();
        Geocoder geoCoder = new Geocoder(context, Locale.getDefault());
        try {
            List<Address> addresses = geoCoder.getFromLocation(
                    lat, // / 1E6,
                    lng /// 1E6,
                    , 1);

            // Handle case where no destinationAddress was found.
            if (addresses == null || addresses.isEmpty()) {
                result = "No destinationAddress found";
            } else {
                Address address = addresses.get(0);
                if (address.getMaxAddressLineIndex() > 0) {
                    for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                        addressBuilder.append(addresses.get(0).getAddressLine(i));
                        addressBuilder.append("_");
                    }
                    Log.d(TAG, "<<< LAST: " + addressBuilder.toString());
                    addressBuilder.deleteCharAt(addressBuilder.length() - 1);
                }

                result = addressBuilder.toString();
                Log.d(TAG, addressBuilder.toString());
            }
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
            result = "No destinationAddress found";
        }
        return result;
    }
}
