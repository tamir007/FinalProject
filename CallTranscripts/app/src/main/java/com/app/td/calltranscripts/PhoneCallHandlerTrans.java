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

import android.nfc.Tag;

import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.util.Log;


import android.view.animation.AccelerateInterpolator;

import android.widget.Toast;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.FileReader;
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
            recordMic();
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            recordMic();

    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }


    private void recordMic() {
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
        speech.initialize(location);
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
        String debugTag = "debug";
        SpeechRecognizer recognizer;
        Intent intent;
        boolean isNewConversation, shouldStop;
        int listenerNum;
        String theText;
        FileWriter writeFile;
        boolean isSpeaking;
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



        protected void initialize( double location) {
            try {
                FileReader readFile = new FileReader(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/call_data.txt");
                char isBitOn = (char)readFile.read();
                switch(isBitOn){
                    case '0':
                        isInstalled = false;
                        Log.d(debugTag , "isInstalled = false");
                        break;
                    case '1':
                        Log.d(debugTag , "isInstalled = true");
                        isInstalled = true;
                        break;
                }

                readFile.close();
            } catch (IOException e) {
                Log.d(debugTag , "error reading file");
            }

            Log.d(debugTag , "read installation bit");

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

            Log.d(debugTag , "FileWriter set up");
            listenerNum = 1;
            theText = "";
            isNewConversation = true;
            shouldStop = false;

            // 1 Intents
            intent = createRecognitionIntent();
            // 1 Speech Recognizer
            recognizer = SpeechRecognizer.createSpeechRecognizer(myContext);

            Log.d(debugTag, "after recognizer init");
        }

        public void run() {
            Log.i(TAG, "run");
            if(!isInstalled) return;
            // mute sounds
            muteSounds();
            // The Listeners
            Log.d(debugTag , "get new listener");
            RecognitionListener listener = createRecognitionListener();
            // Set Listeners to SpeechRecognizer
            Log.d(debugTag , "before bind - recognizer and listener");
            recognizer.setRecognitionListener(listener);
            //run first recognizer
            Log.d(debugTag, "after bind - recognizer and listener");
            runSpeech(recognizer, intent);
            Log.d(debugTag , "after run speech");
        }

        public void stop() {
            Log.i(TAG, "stop");
            shouldStop = true;
            Log.d(debugTag, "stop call");
            saveFile();
            Log.d(debugTag, "File saved");
            recognizer.cancel();
            recognizer.destroy();
            Log.d(debugTag, "destroyed recognizer");
            unMuteSounds();
            Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
            return;
        }

        private void runSpeech(SpeechRecognizer n_recognizer, Intent n_intent) {
            Log.i(TAG, "runSpeech");
            n_recognizer.startListening(n_intent);

        }

        public RecognitionListener createRecognitionListener() {
            
            return new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {
                    Log.d(debugTag, "onResults");
                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults == null) {
                        Log.i(TAG , "NULL RESULTS");
                    } else {
                        theText += voiceResults.get(0) + "\n";
                    }
                    Log.d(debugTag, "Before should stop");
                    // if should stop and not continue the listener cycles
                    if (!shouldStop) {
                        Log.d(debugTag, "called reRunListener");
                        reRunListener();
                        Log.d(debugTag, "returned reRunListener");
                    }
                }

                @Override
                public void onReadyForSpeech(Bundle params) {

                    Log.d(debugTag, "Ready for speech");
                }

                private void reRunListener() {
                    recognizer.cancel();
                    runSpeech(recognizer, intent);
                }


                @Override
                public void onError(int error) {
                    Log.d(debugTag, "onError : " + error);
                    if(!shouldStop) reRunListener();
                }

                @Override
                public void onBeginningOfSpeech() {
                    isSpeaking = true;
                    Log.d(debugTag, "onBeginingOfSpeech");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    Log.d(debugTag, "onBufferRecieved");
                }

                @Override
                public void onEndOfSpeech() {
                    Log.d(debugTag, "onEndOfSpeech");
                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    Log.d(debugTag, "onEevent");
                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    Log.d(debugTag, "onPartialResults");
                    ArrayList<String> voiceResults = partialResults
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults == null) {
                        // do nothing;
                    } else {
                        theText += voiceResults.get(0) + "\n";
                    }
                }
                @Override
                public void onRmsChanged(float rmsdB) {
                    // TODO Auto-generated method stub
                }
            };
        }

        private Intent createRecognitionIntent() {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                    "com.app.td.calltranscripts");
            intent.putExtra("android.speech.extra.DICTATION_MODE", true);
            return intent;
        }

        private void muteSounds() {
            Log.d(debugTag, "muteSound");
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, true);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            aManager.setStreamMute(AudioManager.STREAM_RING, true);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }

        private void unMuteSounds() {
            Log.d(debugTag, "unMuteSounds");
            AudioManager aManager = (AudioManager) myContext.getSystemService(Context.AUDIO_SERVICE);
            aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            aManager.setStreamMute(AudioManager.STREAM_ALARM, false);
            aManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            aManager.setStreamMute(AudioManager.STREAM_RING, false);
            aManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

        }
    }
};