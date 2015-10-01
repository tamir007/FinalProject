package com.app.td.calltranscripts;

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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

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
    // private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    HashMap<String, Double> newCall;
    double signedResult;
    private Location mLastLocation;
    private boolean isRelevant;
    static double latitude;
    static double longitude;
    static BagOfWords bag = null;
    private boolean flag1 = false;
    private boolean flag0 = false;
    private int turn = 0;
    // Google client to interact with Google API
    static GoogleApiClient mGoogleApiClient;


    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        recordMic();
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {

        recordMic();
        flag0 = true;
        turn = 1;
        while(flag1 && turn == 1){
            // busy wait
        }
        if(isRelevant){
            speech.predictionIncorrect();
        }

        flag0 = false;

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


        private void startBag(){


            // if bag is not initialized, load previous calls
            if(bag == null){
                Log.d("debug", "Load previous knowledge");
                bag = new BagOfWords(1.0);
                bag.loadWVector(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "data/wVec.txt");
                bag.loadTags(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "data/tags.txt");
            }
            signedResult = 0.0;
            bag.addCallsFromFolder(Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/OLD_TRANSCRIPTS");

            Log.d("debug","Analyzing new call");
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                    +"/TRANSCRIPTS");
            File[] listOfFiles = folder.listFiles();
            if(listOfFiles.length == 0){
                Log.d("debug","no new calls");
            }


           newCall = bag.getMappingVector((listOfFiles[0]).getAbsolutePath());
            if (bag.samples.size() == 0){
                bag.samples.add(newCall);
                bag.w_vec.add(1.0);
                bag.tags.add(1.0);
                bag.saveData();
            }

            double result = bag.calcHypothesis(newCall);
            Log.d("debug" , "Result : " + result);
            signedResult = Math.signum(result);

            if(signedResult == 1.0){
                // wants call suggestions
                // call intent activity
//                Intent intent = new Intent(myContext, SuggestsActivity.class);
//                intent.putExtra()
                
            }else{
                // don't want call suggestions
                startRelevantTimer();
            }
        }

        private void startRelevantTimer() {
            isRelevant = true;
            new CountDownTimer(15000, 15000) {

                public void onTick(long millisUntilFinished) {
                    // do nothing
                }

                public void onFinish() {
                    flag1 = true;
                    turn = 0;
                    while(flag0 && turn == 0){
                        // busy wait
                    }

                    if(isRelevant){
                        speech.predictionCorrect();
                    }
                    isRelevant = false;

                    flag1 = false;
                }
            }.start();
        }

        private void predictionCorrect(){
            bag.addNewVectorToKernel(newCall);
            bag.w_vec.add(signedResult);
            bag.tags.add(signedResult);

            bag.saveData();
        }



        private void predictionIncorrect(){
            bag.w_vec.add(0.0);
            bag.tags.add(-signedResult);
            bag.samples.add(newCall);
            bag.optimizeKernelCoefficients();

            bag.saveData();
        }


        private void saveFile() {
            if(wasWritten) return;
            Log.i(debugTag , "save file");
            try {
                writeFile.write(theText);
                writeFile.write("\n" + "Location: \n"  + "latitude : " + latitude + "\n" +
                        "longitude : " + longitude + "\n" + "ADDRESS : " + callAddress);
                writeFile.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

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

            //Log.d(debugTag , "read installation bit");

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            String dateAndTime = dateFormat.format(date);

            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TRANSCRIPTS");
            dir.mkdir();
            // Log.i(debugTag, dateAndTime);
            String fileName =  dateAndTime + ".txt";
            fileName = fileName.replaceAll("\\s","");
            fileName = fileName.replaceAll(":","");
            fileName = fileName.replaceAll("/","");
            fileName = "/" + fileName;

            Log.i(debugTag, fileName);

            try {
                writeFile = new FileWriter(dir.getAbsolutePath() +
                        fileName);

            } catch (IOException e) {
                // do nothing
                Log.i(debugTag , "no writer");
            }
            Log.d(debugTag , "FileWriter set up");
            listenerNum = 1;
            theText = "";
            lastText = "";
            isNewConversation = true;
            shouldStop = false;
            wasWritten = false;

            // add time and date to the file.



            //write phone number
            try {
                writeFile.write("Date and Time : " + dateAndTime + "\n");
                writeFile.write("Phone Number : " + PhoneCallHandlerTrans.savedNumber + "\n");
            } catch (IOException e) {
                Log.i(debugTag , "exception writing");
                e.printStackTrace();
            }

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
            Log.d(debugTag, "after bind - recognizer and listener");
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
                        theText += voiceResults.get(0) + "\n";
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
                        startBag();
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
                        startBag();
                        Toast.makeText(myContext, "Transcript stopped", Toast.LENGTH_SHORT).show();
                    }
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