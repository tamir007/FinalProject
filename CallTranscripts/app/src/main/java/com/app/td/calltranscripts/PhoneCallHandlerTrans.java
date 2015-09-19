package com.app.td.calltranscripts;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.widget.Toast;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.location.LocationManager.*;

/**
 * This Class will handle all Tele-Phone actions.
 */
public class PhoneCallHandlerTrans extends PhonecallReceiver implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static boolean isInstalled = false;
    static boolean running = false;
    static SpeechToTextNoPop speech;
    static Context myContext;
    static final String TAG = "ZUTA";
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private Location mLastLocation;

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        if (isInstalled) {
            recordMic(ctx);
        }
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.i(TAG, "outgoing call started");
        if (isInstalled) {
            Toast.makeText(ctx, "Here", Toast.LENGTH_SHORT).show();
            recordMic(ctx);
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "on incoming call ended");
        if (isInstalled) stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.i(TAG, "on outgoing call ended");
        if (isInstalled) stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }


    private void recordMic(Context ctx) {
        Log.i(TAG, "record mic");
        if (checkPlayServices()) {

            // Building the GoogleApi client
            buildGoogleApiClient();
        }
        else{
            Log.i(TAG, "no google play services");
        }
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        else{
            Log.i(TAG, "no google play CLIENT");
        }
        double location = getLocation();
        speech = new SpeechToTextNoPop();
        speech.initialize(ctx, location);
        speech.run();

    }

    private void stopRecordMic() {
        Log.i(TAG, "stop record mic");
        if (speech != null) speech.stop();
    }

    @Override
    public void onConnected(Bundle bundle) {
        double loc = getLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = "
                + connectionResult.getErrorCode());
    }

    /**
     * Method to display the location on UI
     * */
    private double getLocation() {

        mLastLocation = LocationServices.FusedLocationApi
                .getLastLocation(mGoogleApiClient);

        if (mLastLocation != null) {
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();
            return latitude;

        } else {
            return -1;
        }
    }

    /**
     * Creating google api client object
     * */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(myContext)
                .addConnectionCallbacks(this) // maybe better to have context here
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(myContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, (Activity) myContext,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(myContext,
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
            }
            return false;
        }
        return true;
    }



    public class SpeechToTextNoPop {

        SpeechRecognizer recognizer, recognizer_two, recognizer_three;
        Intent intent, intent_two, intent_three;
        boolean isNewConversation, shouldStop;
        int listenerNum;
        String theText;
        FileWriter writeFile;

        private void saveFile() {
            try {
                writeFile.write(theText);
                writeFile.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String findContact(String phoneNumber) {
            Log.i(TAG, "finding contact");
            ContentResolver cr = myContext.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cur = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            // add checks to the results of the cursor
            if (cur == null) {
                Log.i(TAG, "cursor null");
                return null;
            }
            String contactName = null;
            if (cur.moveToFirst()) {
                contactName = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.i(TAG, "name is : " + contactName);
            }
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            return contactName;
        }


//        /*----Method to Check GPS is enable or disable ----- */
//        private Boolean displayGpsStatus() {
//            ContentResolver contentResolver = myContext.getContentResolver();
//            boolean gpsStatus = Settings.Secure
//                    .isLocationProviderEnabled(contentResolver,
//                            GPS_PROVIDER);
//            if (gpsStatus) {
//                return true;
//
//            } else {
//                return false;
//            }
//        }
//
//
//        /*----------Listener class to get coordinates ------------- */
//        private class MyLocationListener implements LocationListener {
//            @Override
//            public void onLocationChanged(Location loc) {
//
//
//                Toast.makeText(myContext, "Location changed : Lat: " +
//                                loc.getLatitude() + " Lng: " + loc.getLongitude(),
//                        Toast.LENGTH_SHORT).show();
//                String longitude = "Longitude: " + loc.getLongitude();
//                Log.i(TAG, longitude);
//                String latitude = "Latitude: " + loc.getLatitude();
//                Log.i(TAG, latitude);
//
//    /*----------to get City-Name from coordinates ------------- */
//                String cityName = null;
//                Geocoder gcd = new Geocoder(myContext,
//                        Locale.getDefault());
//                List<Address> addresses;
//                try {
//                    addresses = gcd.getFromLocation(loc.getLatitude(), loc
//                            .getLongitude(), 1);
//                    if (addresses.size() > 0)
//                        System.out.println(addresses.get(0).getLocality());
//                    cityName = addresses.get(0).getLocality();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                String s = longitude + "\n" + latitude +
//                        "\n\nMy Currrent City is: " + cityName;
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//                // TODO Auto-generated method stub
//            }
//
//            @Override
//            public void onStatusChanged(String provider,
//                                        int status, Bundle extras) {
//                // TODO Auto-generated method stub
//            }
//        }


        protected void initialize(Context cxt, double location) {
            try {
                writeFile = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/newCall.txt");
            } catch (IOException e) {
                Log.i(TAG, "ERROR creating file");
            }
            // add time and date to the file.

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            //write phone number
            try {
                writeFile.write("Date and Time : " + dateFormat.format(date) + "\n");
                writeFile.write("Phone Number : " + PhoneCallHandlerTrans.savedNumber + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }

            //find and write the contact
            try {
                writeFile.write("Contact : " + findContact(PhoneCallHandlerTrans.savedNumber) + "\n");
            } catch (IOException e) {
                Log.i(TAG, "ERROR writing contact");
                e.printStackTrace();
            }


            //add Location:

            try {
                writeFile.write("Location : " + location + "\n");
            } catch (IOException e) {
                Log.i(TAG, "ERROR writing location");
                e.printStackTrace();
            }



            listenerNum = 1;
            theText = "";
            //myContext = cxt;
            isNewConversation = true;
            shouldStop = false;
            // 3 Intents
            intent = getNewRecognitionIntet();
            intent_two = getNewRecognitionIntet();
            intent_three = getNewRecognitionIntet();

            // 3 Speech Recognizer
            recognizer = SpeechRecognizer
                    .createSpeechRecognizer(myContext);
            recognizer_two = SpeechRecognizer
                    .createSpeechRecognizer(myContext);
            recognizer_three = SpeechRecognizer
                    .createSpeechRecognizer(myContext);

        }


        public void run() {
            Log.i(TAG, "run");
            // mute sounds
            muteSounds();
            // The Listeners
            RecognitionListener listener = newRecognitionListener();
            RecognitionListener listener_two = newRecognitionListener();
            RecognitionListener listener_three = newRecognitionListener();

            // Set Listeners to SpeechRecognizer
            recognizer.setRecognitionListener(listener);
            recognizer_two.setRecognitionListener(listener_two);
            recognizer_three.setRecognitionListener(listener_three);

            //run first recognizer
            runSpeech(recognizer, intent);


        }

        public void stop() {
            Log.i(TAG, "stop");
            shouldStop = true;

            saveFile();
            try {
                Thread.sleep(6000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            recognizer.destroy();
            recognizer_two.destroy();
            recognizer_three.destroy();
            unMuteSounds();
            Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
            return;
        }

        private void runSpeech(SpeechRecognizer n_recognizer, Intent n_intent) {
            Log.i(TAG, "runSpeech");
            n_recognizer.startListening(n_intent);

        }


        public RecognitionListener newRecognitionListener() {
            return new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {
                    Log.i(TAG , "RESULTS");
                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults == null) {
                        Log.i(TAG , "NULL RESULTS");
                    } else {
                        theText += voiceResults.get(0) + "\n";
                    }

                    // if should stop and not continue the listener cycles
                    if (!shouldStop) nextListener();

                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.i(TAG, "on ready for speech");
                    //Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();
                    //  Log.d(TAG, "Ready for speech");

                }


                private void nextListener() {
                    switch (listenerNum) {
                        case 1:
                            runSpeech(recognizer_two, intent_two);
                            Log.i(TAG, "started listening: second listener");
                            listenerNum = 2;
                            break;
                        case 2:
                            runSpeech(recognizer_three, intent_three);
                            Log.i(TAG, "started listening: third listener");
                            listenerNum = 3;
                            break;
                        case 3:
                            runSpeech(recognizer, intent);
                            Log.i(TAG, "started listening: first listener");
                            listenerNum = 1;
                            break;

                    }

                }
                private void stopListener() {
                    switch (listenerNum) {
                        case 1:
                            recognizer.stopListening();
                            Log.i(TAG, "first listener stopped manually");
                            break;
                        case 2:
                            recognizer_two.stopListening();
                            Log.i(TAG, "second listener stopped manually");
                            break;
                        case 3:
                            recognizer_three.stopListening();
                            Log.i(TAG, "third listener stopped manually");
                            break;

                    }

                }

                @Override
                public void onError(int error) {
                    Log.i(TAG, "on error number : " + error);
                    if (error == 6) stopListener();
                    nextListener();
                    Toast.makeText(myContext, "Error : " + error, Toast.LENGTH_SHORT).show();
                    //   Log.d(TAG, "Error listening for speech: " + error);
                    //Toast.makeText(getApplicationContext(), "Error listening for speech:" + error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBeginningOfSpeech() {
                    Log.i(TAG, "on Begining of speech");
                    //Toast.makeText(getApplicationContext(), "Speech Starting", Toast.LENGTH_SHORT).show();
                    //    Log.d(TAG, "Speech starting");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "on buffer received");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.i(TAG, "on end of speech");
                    // TODO Auto-generated method stub

                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.i(TAG, "on Event");
                    // TODO Auto-generated method stub

                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // TODO Auto-generated method stub
                    Log.i(TAG, "Partial results");
                }

                @Override
                public void onRmsChanged(float rmsdB) {
                    // TODO Auto-generated method stub
                }
            };
        }

        private Intent getNewRecognitionIntet() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "com.app.td.calltranscripts");
            return intent;
        }

        private void muteSounds() {
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            aManager.setStreamMute(AudioManager.STREAM_RING, true);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }

        private void unMuteSounds() {
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            aManager.setStreamMute(AudioManager.STREAM_RING, false);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

        }
    }


};