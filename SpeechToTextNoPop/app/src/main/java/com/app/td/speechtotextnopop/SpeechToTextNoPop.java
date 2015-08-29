package com.app.td.speechtotextnopop;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SpeechToTextNoPop extends AppCompatActivity {
    private boolean clicked;
    private boolean isSpeaking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speech_to_text_no_pop);

        final Button speakBtn = (Button) findViewById(R.id.spk_btn);
        final TextView myText = (TextView) findViewById(R.id.TextView1);
        isSpeaking = false;
        // 3 Intents
        final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.app.td.speechtotextnopop");

        final Intent intent_two = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.app.td.speechtotextnopop");

        final Intent intent_three = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                "com.app.td.speechtotextnopop");

        // 3 Speech Recognizer
        final SpeechRecognizer recognizer = SpeechRecognizer
                .createSpeechRecognizer(getApplicationContext());

        final SpeechRecognizer recognizer_two = SpeechRecognizer
                .createSpeechRecognizer(getApplicationContext());

        final SpeechRecognizer recognizer_three = SpeechRecognizer
                .createSpeechRecognizer(getApplicationContext());

        speakBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clicked = false;
                if (isSpeaking) {
                    speakBtn.setText("Speak");
                    recognizer.destroy();
                    recognizer_two.destroy();
                    recognizer_three.destroy();
                    isSpeaking = false;

                    // unmute sounds
                    AudioManager aManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
                    aManager.setStreamMute(AudioManager.STREAM_ALARM, false);
                    aManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
                    aManager.setStreamMute(AudioManager.STREAM_RING, false);
                    aManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);

                    return;
                } else {
                    // mute sounds
                    AudioManager aManager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
                    aManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                    aManager.setStreamMute(AudioManager.STREAM_ALARM, true);
                    aManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                    aManager.setStreamMute(AudioManager.STREAM_RING, true);
                    aManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    speakBtn.setText("Stop");
                    isSpeaking = true;

                }


                // The Listeners
                RecognitionListener listener = new RecognitionListener() {
                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> voiceResults = results
                                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (voiceResults == null) {
                            // Toast.makeText(getApplicationContext(), "No Voice Results", Toast.LENGTH_SHORT).show();
                            // Log.e(TAG, "No voice results");
                        } else {
                            //  Log.d(TAG, "Printing matches: ");
                            if (!clicked) {
                                myText.setText(voiceResults.get(0));
                                clicked = true;
                            } else {
                                myText.setText(myText.getText() + " " + voiceResults.get(0));
                            }

                        }
                        nextListener();
                    }

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        //  Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();
                        //  Log.d(TAG, "Ready for speech");
                    }

                    private void nextListener() {
                        runSpeech(recognizer_two, intent_two);
                    }

                    @Override
                    public void onError(int error) {
                        nextListener();
                        //   Log.d(TAG, "Error listening for speech: " + error);
                       // Toast.makeText(getApplicationContext(), "Error listening for speech:" + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        //  Toast.makeText(getApplicationContext(), "Speech Starting", Toast.LENGTH_SHORT).show();
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

                RecognitionListener listener_two = new RecognitionListener() {
                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> voiceResults = results
                                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (voiceResults == null) {
                            //   Toast.makeText(getApplicationContext(), "No Voice Results", Toast.LENGTH_SHORT).show();
                            // Log.e(TAG, "No voice results");
                        } else {
                            //    Toast.makeText(getApplicationContext(), "Printing matches:", Toast.LENGTH_SHORT).show();
                            //  Log.d(TAG, "Printing matches: ");
                            myText.setText(myText.getText() + " " + voiceResults.get(0));
                        }
                        nextListener();

                    }

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        //       Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();
                        //  Log.d(TAG, "Ready for speech");
                    }

                    private void nextListener() {
                        runSpeech(recognizer_three, intent_three);
                    }

                    @Override
                    public void onError(int error) {
                        nextListener();
                        //   Log.d(TAG, "Error listening for speech: " + error);
                      //  Toast.makeText(getApplicationContext(), "Error listening for speech:" + error, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        //        Toast.makeText(getApplicationContext(), "Speech Starting", Toast.LENGTH_SHORT).show();
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

                RecognitionListener listener_three = new RecognitionListener() {
                    @Override
                    public void onResults(Bundle results) {

                        ArrayList<String> voiceResults = results
                                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (voiceResults == null) {
                            //  Toast.makeText(getApplicationContext(), "No Voice Results", Toast.LENGTH_SHORT).show();
                            // Log.e(TAG, "No voice results");
                        } else {
                            //  Log.d(TAG, "Printing matches: ");
                            if (!clicked) {
                                myText.setText(voiceResults.get(0));
                                clicked = true;
                            } else {
                                myText.setText(myText.getText() + " " + voiceResults.get(0));
                            }

                        }
                        nextListener();

                    }

                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        //Toast.makeText(getApplicationContext(), "Ready For Speech", Toast.LENGTH_SHORT).show();
                        //  Log.d(TAG, "Ready for speech");
                    }

                    private void nextListener() {
                        runSpeech(recognizer, intent);
                    }

                    @Override
                    public void onError(int error) {
                        nextListener();
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


                recognizer.setRecognitionListener(listener);
                recognizer_two.setRecognitionListener(listener_two);
                recognizer_three.setRecognitionListener(listener_three);
                runSpeech(recognizer, intent);
            }
        });
    }

    ;


    private void runSpeech(SpeechRecognizer n_recognizer, Intent n_intent) {
        n_recognizer.startListening(n_intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_speech_to_text_no_pop, menu);
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
}
