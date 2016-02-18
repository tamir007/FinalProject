package com.app.td.calltranscripts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

//google shit
import com.google.android.gms.common.api.GoogleApiClient;


import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import jxl.read.biff.BiffException;
import jxl.write.WriteException;

/**
 * This Class will handle all Tele-Phone actions.
 */
public class PhoneCallHandlerTrans extends PhonecallReceiver{

    static boolean isInstalled = false;
    String debugTag = "debug";
    static boolean running = false;
    static SpeechToTextNoPop speech;
    static Context myContext;
    static String callAddress;
    static String myPhoneContacts;
    // private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    HashMap<String, Double> newCall;
    double signedResult;
    String[] newRow = new String[7];

    public static final  String MENTIONED_NAMES_EXTRA = "Relevant names";
    public static final String PHONE_NUMBERS_EXTRA = "Relevant numbers";
    private Location mLastLocation;
    private boolean isRelevant;
    static double latitude;
    static double longitude;




    // Google client to interact with Google API
    static GoogleApiClient mGoogleApiClient;

    public static final String BROADCAST = "PACKAGE_NAME.android.action.broadcast";
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



    public static void setLocation(String address){
        callAddress = address;
    }


    private void recordMic() {
        Log.i(debugTag, "record mic");


        mGoogleApiClient.connect();

        speech = new SpeechToTextNoPop();
        // mGoogleApiClient.connect();
        speech.initialize();
        speech.run();

    }

    private void stopRecordMic() {
        if (speech != null) speech.stop();
    }

    public class SpeechToTextNoPop {
        RecognitionListener listener;
        SpeechRecognizer recognizer;
        Intent intent;
        boolean isNewConversation, shouldStop;
        int listenerNum;
        String theText;
        FileWriter writeFile;

        boolean isSpeaking;
        String lastText;
        boolean wasWritten;
        AppData myAppData;


        private void saveFile() {
            if(wasWritten) return;
            Log.i(debugTag, "save file");
            newRow[2] = "lat - " + latitude;
            newRow[3] = "lon - " + longitude;
            Log.i("debug", theText);
            newRow[6] = theText;
            try {
                ExcelUtils.write_excel(newRow);
            } catch (IOException e) {
                Log.i("debug", "excel IOException");
                e.printStackTrace();
            } catch (BiffException e) {
                Log.i("debug", "excel BiffException");
                e.printStackTrace();
            } catch (WriteException e) {
                Log.i("debug", "excel WriteException");
                e.printStackTrace();
            }


            Log.i("debug", "finish saveFile");
            wasWritten =true;
        }

        public String findContact(String phoneNumber) {
            Log.i(debugTag, "finding contact");
            ContentResolver cr = myContext.getContentResolver();
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Cursor cur = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            // add checks to the results of the cursor
            if (cur == null) {
                Log.i(debugTag, "cursor null");
                return null;
            }
            String contactName = null;
            if (cur.moveToFirst()) {
                contactName = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.i(debugTag, "name is : " + contactName);
            }
            if (cur != null && !cur.isClosed()) {
                cur.close();
            }
            return contactName;
        }


        /**
         * Initialize SpeechToTextNoPop
         */
        protected void initialize() {
            Log.d(debugTag , "initialize");
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
            myAppData = (AppData) SerializationUtil.deserialize(Environment.getExternalStorageDirectory().getAbsolutePath() + "/App_Data.txt");

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            String dateAndTime = dateFormat.format(date);


            newRow[0] = myAppData.getId(PhoneCallHandlerTrans.savedNumber);
            newRow[1] = PhoneCallHandlerTrans.savedNumber;
            long time = TimeUtil.getLongTime();
            newRow[4] = Integer.toString(TimeUtil.getClockFromLong(time));
            newRow[5] = Integer.toString(TimeUtil.getDayFromLong(time));


            listenerNum = 1;
            theText = "";
            lastText = "";
            isNewConversation = true;
            shouldStop = false;
            wasWritten = false;


            //find and write the contact
//            try {
//                String contact = findContact(PhoneCallHandlerTrans.savedNumber);
//                Log.i(debugTag, "found contact : " + contact);
//                writeFile.write("Contact : " + contact + "\n");
//            } catch (IOException e) {
//                Log.i(debugTag, "ERROR writing contact");
//                e.printStackTrace();
//            }
//
// 1 Intents

            intent = createRecognitionIntent();
            // 1 Speech Recognizer
            recognizer = SpeechRecognizer.createSpeechRecognizer(myContext);

            Log.d(debugTag, "after recognizer init");
        }

        public void run() {
            if(!isInstalled) return;
            // mute sounds
            muteSounds();
            // The Listeners

            Log.d(debugTag, "get new listener");
            listener = createRecognitionListener();
            // Set Listeners to SpeechRecognizer
            Log.d(debugTag, "before bind - recognizer and listener");

            recognizer.setRecognitionListener(listener);
            //run first recognizer
            runSpeech(recognizer, intent);
            Log.d(debugTag, "after run speech");
        }

        public void stop() {

            shouldStop = true;
            Log.d(debugTag, "stop call");
            recognizer.stopListening();
//            Log.d(debugTag, "File saved");
//            recognizer.cancel();
//            recognizer.destroy();
//            Log.d(debugTag, "destroyed recognizer");

            Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
            return;
        }

        private void runSpeech(SpeechRecognizer n_recognizer, Intent n_intent) {

            n_recognizer.startListening(n_intent);
        }

        public RecognitionListener createRecognitionListener() {

            return new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {
                    Log.d(debugTag, "onResults");
                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    String temp = "";
                    if (voiceResults == null) {
                        // do nothing;
                    } else {
                        theText += voiceResults.get(0) + " ";
                    }

                    Log.d(debugTag, "Before should stop");
                    // if should stop and not continue the listener cycles
                    if (!shouldStop) {
                        Log.d(debugTag, "called reRunListener");
                        reRunListener(0);
                        Log.d(debugTag, "returned reRunListener");
                    }else{
                        saveFile();
                        Log.d(debugTag, "File Saved");
                        recognizer.cancel();
                        recognizer.destroy();
                        Log.d(debugTag, "destroyed recognizer");
                        unMuteSounds();
                        // BAG OF WORDS
                        Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                    Log.d(debugTag, "Ready for speech");
                }

                private void reRunListener(int error) {

                    recognizer.cancel();

                    if(recognizer != null){
                        recognizer.destroy();
                    }
                    recognizer = SpeechRecognizer.createSpeechRecognizer(myContext);
                    recognizer.setRecognitionListener(listener);

                    runSpeech(recognizer, intent);
                }

                @Override
                public void onError(int error) {
                    Log.d(debugTag, "onError : " + error);
                    if(!shouldStop){
                        Log.d(debugTag, "continue");
                        reRunListener(error);
                    }else{
                        Log.d(debugTag, "Stopping in error");
                        saveFile();
                        Log.d(debugTag, "File Saved");
                        recognizer.cancel();
                        recognizer.destroy();
                        Log.d(debugTag, "destroyed recognizer");
                        unMuteSounds();
                        // BAG OF WORDS
                        //startBag();
                        Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onBeginningOfSpeech() {

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
                    // TODO Auto-generated method stub
                    Log.d(debugTag, "onPartialResults");
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
            //intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            //intent.putExtra("android.speech.extra.DICTATION_MODE", true);
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