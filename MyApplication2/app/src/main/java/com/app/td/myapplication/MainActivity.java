package com.app.td.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.media.MediaRecorder;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private boolean isRecording;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        isRecording = false;
        Button btn = (Button) findViewById(R.id.button);
        Button recBtn = (Button) findViewById(R.id.recordBtn);



        // Record button Click Listener
        recBtn.setOnClickListener(new View.OnClickListener() {
            MediaRecorder micRecorder;
            Button innerRecButton = (Button) findViewById(R.id.recordBtn);
            @Override
            public void onClick(View view) {
                // start Recording
                if(!isRecording){
                    // Media recorder initialization
                    Boolean isSDPresent = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                    if(!isSDPresent){
                        innerRecButton.setText("Error ! : NO SD");
                        return;
                    }

                    // initialize Media Recorder Settings
                    micRecorder = new MediaRecorder();
                    micRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    micRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                    micRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/MicRecording.3gp");
                    micRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

                    innerRecButton.setText("Stop Recording");
                    try {
                        micRecorder.prepare();
                    } catch (IOException e) {
                        Log.e("Failed to Prepare", "prepare() failed");
                    }
                    isRecording = true;
                    micRecorder.start();

                // Stop Recording
                }else{
                    micRecorder.stop();
                    micRecorder.release();
                    innerRecButton.setText("Start Recording");
                    isRecording = false;


                }
            }
        });

        // Call button Click Listener
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);

                String number = ((EditText)findViewById(R.id.numberToCall)).getText().toString();
                callIntent.setData(Uri.parse("tel:"+ number));
                startActivity(callIntent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
