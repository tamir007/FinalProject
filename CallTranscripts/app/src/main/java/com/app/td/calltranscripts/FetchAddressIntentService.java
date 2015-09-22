package com.app.td.calltranscripts;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by daniel zateikin on 21/09/2015.
 */
public class FetchAddressIntentService extends IntentService {

        String debugTag = "debug";

        /**
         * Creates an IntentService.  Invoked by your subclass's constructor.
         *
         * @param name Used to name the worker thread, important only for debugging.
         */

        protected ResultReceiver mReceiver ;


        public FetchAddressIntentService() {
            super("FetchAddressIntentService");
           Log.i(debugTag, " intent service constructor");
        }

        private void deliverResultToReceiver(int resultCode, String message) {
            Log.i(debugTag , "deliver results to receiver");
            Bundle bundle = new Bundle();
            bundle.putString(Constants.RESULT_DATA_KEY, message);
            mReceiver.send(resultCode, bundle);
        }

        protected void onHandleIntent(Intent intent) {
            Log.i(debugTag , "on handle intent");
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());


            String errorMessage = "";

            // Get the location passed to this service through an extra.
            Location location = intent.getParcelableExtra(
                    Constants.LOCATION_DATA_EXTRA);

            mReceiver = intent.getParcelableExtra(CallTranscript.Constants.RECEIVER);

            List<Address> addresses = null;

            try {

                if (geocoder.isPresent()) {
                    Log.i(debugTag , " GEOCODER present");
                    addresses = geocoder.getFromLocation(
                            location.getLatitude(),
                            location.getLongitude(),
                            // In this sample, get just a single address.
                            1);
                }
                else{
                    Log.i(debugTag , "no GEOCODER");
                }
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.service_not_available);
                Log.e(debugTag, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(debugTag, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size()  == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = getString(R.string.no_address_found);
                    Log.e(debugTag, errorMessage);
                }
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i(debugTag, getString(R.string.address_found));
                Log.i(debugTag, " ADDRESS : " + TextUtils.join(System.getProperty("line.separator"), addressFragments));
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"),
                                addressFragments));
            }
        }


     public final class Constants {
        public static final int SUCCESS_RESULT = 0;
        public static final int FAILURE_RESULT = 1;
        public static final String PACKAGE_NAME =
                "com.google.android.gms.location.sample.locationaddress";
        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME +
                ".LOCATION_DATA_EXTRA";
    }


}
