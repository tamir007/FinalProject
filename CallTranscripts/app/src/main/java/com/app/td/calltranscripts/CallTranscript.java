package com.app.td.calltranscripts;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CallTranscript extends AppCompatActivity  implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    String debugTag = "debug";
    //      private LocationRequest mLocationRequest;
//
    private Location mLastLocation;
//
//  //   Google client to interact with Google API
//    private GoogleApiClient mGoogleApiClient;

    public AddressResultReceiver mResultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(debugTag, "on create");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_transcript);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        // checks if receiver is installed and init checkBox
        checkIfInstalled(checkBox);

        buildGoogleApiClient();
//        PhoneCallHandlerTrans.mGoogleApiClient.connect();

        // set up checkbox on click
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/call_data.txt");
                FileOutputStream fOut = null;
                OutputStreamWriter myOutWriter = null;
                try {
                    myFile.createNewFile();
                    fOut = new FileOutputStream(myFile);
                    myOutWriter = new OutputStreamWriter(fOut);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isChecked) {
                    try {
                        myOutWriter.write("1");
                        myOutWriter.close();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    activateRecord();
                } else {
                    try {
                        myOutWriter.write("0");
                        myOutWriter.close();
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    deActivateRecord();
                }

            }
        });

    }

    /**
     * Method to display the location on UI
     * */
    private void displayLocation() {
        Log.i(debugTag, "display location - call transcript");
        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(PhoneCallHandlerTrans.mGoogleApiClient);

        if (mLastLocation != null) {
            // PhoneCallHandlerTrans.latitude = mLastLocation.getLatitude();
            //PhoneCallHandlerTrans.longitude = mLastLocation.getLongitude();
            //Log.i(debugTag, "Location : latitude = " + PhoneCallHandlerTrans.latitude);
            //Log.i(debugTag, "Location : longitude = " + PhoneCallHandlerTrans.longitude);
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            PhoneCallHandlerTrans.latitude = latitude;
            PhoneCallHandlerTrans.longitude = longitude;

            Log.i(debugTag, "Location : latitude = " + latitude);
            Log.i(debugTag, "Location : longitude = " + longitude);

            startIntentService();


        } else {
            Log.i(debugTag, "last location is null");
        }
    }

    protected synchronized void buildGoogleApiClient() {
        Log.i(debugTag, "building google client");
        PhoneCallHandlerTrans.mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void checkIfInstalled(CheckBox box){
        try {
            FileReader readFile = new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/call_data.txt");
            char isBitOn = (char)readFile.read();
            switch(isBitOn){
                case '0':
                    Log.d("Tamir", "isInstalled = false");
                    box.setChecked(false);
                    deActivateRecord();
                    break;
                case '1':
                    Log.d("Tamir" , "isInstalled = true");
                    box.setChecked(true);
                    activateRecord();
                    break;
            }

            readFile.close();
        } catch (IOException e) {
            // do nothing
        }
    }

    private void activateRecord() {
        Log.i(debugTag, "activate record");
        PhoneCallHandlerTrans.myContext = getApplicationContext();
        PhoneCallHandlerTrans.isInstalled = true;
        Toast.makeText(getApplicationContext(), "Call Recorder Acativated", Toast.LENGTH_SHORT).show();


    }


    private void deActivateRecord() {
        Log.i(debugTag, "deactivate record");
        PhoneCallHandlerTrans.isInstalled = false;
        Toast.makeText(getApplicationContext(), "Call Recorder Decativated", Toast.LENGTH_LONG).show();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_call_transcript, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(debugTag, "google: on connected");
        displayLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(debugTag, "google: on connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(debugTag, "google: on connection failed");
    }

    protected void startIntentService() {
        Log.i(debugTag , "start intent service");
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        mResultReceiver = new AddressResultReceiver(new Handler());
        intent.putExtra(Constants.RECEIVER, mResultReceiver);
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mLastLocation);
        startService(intent);
    }

    public class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            Log.i(debugTag , "on receive results");
            String mAddressOutput;
            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);
            Log.i(debugTag , "ADDRESS : " + mAddressOutput);
            PhoneCallHandlerTrans.setLocation(mAddressOutput);

            // Show a toast message if an address was found.
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.i(debugTag , getString(R.string.address_found));
            }

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
