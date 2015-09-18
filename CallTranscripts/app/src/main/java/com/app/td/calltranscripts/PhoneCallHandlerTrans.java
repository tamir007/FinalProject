package com.app.td.calltranscripts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.widget.Toast;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * This Class will handle all Tele-Phone actions.
 */
public class PhoneCallHandlerTrans extends PhonecallReceiver {

    static boolean isInstalled = false;
    static boolean running = false;
    static SpeechToTextNoPop speech;
    static Context myContext;

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
        speech = new SpeechToTextNoPop();
        speech.initialize();
        speech.run();

    }

    private void stopRecordMic() {
        if (speech != null) speech.stop();
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

        /**
         * Initialize SpeechToTextNoPop
         */
        protected void initialize() {
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
                // do nothing
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
                        // do nothing;
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
                    // TODO Auto-generated method stub
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