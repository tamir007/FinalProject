package com.app.td.calltranscripts;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.widget.Toast;

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
        if (isInstalled) {
            recordMic(ctx);
        }
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {

        if (isInstalled) {
            //Toast.makeText(ctx, "Here", Toast.LENGTH_SHORT).show();
            recordMic(ctx);
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

        if (isInstalled) stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        if (isInstalled) stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }


    private void recordMic(Context ctx) {
        speech = new SpeechToTextNoPop();
        speech.initialize(ctx);
        speech.run();

    }

    private void stopRecordMic() {
        if (speech != null) speech.stop();
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


        protected void initialize(Context cxt) {
            try {
                writeFile = new FileWriter(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/newCall.txt");
            } catch (IOException e) {
                // do nothing
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
            n_recognizer.startListening(n_intent);
        }


        public RecognitionListener newRecognitionListener() {
            return new RecognitionListener() {

                @Override
                public void onResults(Bundle results) {

                    ArrayList<String> voiceResults = results
                            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                    if (voiceResults == null) {
                        // do nothing;
                    } else {
                        theText += voiceResults.get(0) + "\n";
                    }

                    // if should stop and not continue the listener cycles
                    if (!shouldStop) nextListener();

                }

                @Override
                public void onReadyForSpeech(Bundle params) {
                    //Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();
                    //  Log.d(TAG, "Ready for speech");

                }


                private void nextListener() {
                    switch (listenerNum) {
                        case 1:
                            runSpeech(recognizer_two, intent_two);
                            listenerNum = 2;
                            break;
                        case 2:
                            runSpeech(recognizer_three, intent_three);
                            listenerNum = 3;
                            break;
                        case 3:
                            runSpeech(recognizer, intent);
                            listenerNum = 1;
                            break;

                    }

                }

                @Override
                public void onError(int error) {
                    nextListener();
                    Toast.makeText(myContext, "Error : " + error, Toast.LENGTH_SHORT).show();
                    //   Log.d(TAG, "Error listening for speech: " + error);
                    //Toast.makeText(getApplicationContext(), "Error listening for speech:" + error, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onBeginningOfSpeech() {
                    //Toast.makeText(getApplicationContext(), "Speech Starting", Toast.LENGTH_SHORT).show();
                    //    Log.d(TAG, "Speech starting");
                }

                @Override
                public void onBufferReceived(byte[] buffer) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onEndOfSpeech() {

                    // TODO Auto-generated method stub

                }

                @Override
                public void onEvent(int eventType, Bundle params) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onPartialResults(Bundle partialResults) {
                    // TODO Auto-generated method stub

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