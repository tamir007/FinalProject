package com.app.td.calllistener;

import android.content.Context;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Date;

/**
 * This Class will handle all Tele-Phone actions.
 */
public class PhoneCallHandler extends PhonecallReceiver {

    static MediaRecorder micRecorder;
    static boolean isInstalled = false;

    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        if(isInstalled){
            recordMic();
        }
    }

    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        if(isInstalled){
            recordMic();
        }
    }

    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {

        if(isInstalled)stopRecordMic();
    }

    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        if(isInstalled)stopRecordMic();
    }

    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        // do not need
    }


    private void recordMic() {
        micRecorder = new MediaRecorder();
        micRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        micRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        micRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/MicRecording.3gp");
        micRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            micRecorder.prepare();
        } catch (IOException e) {
            Log.e("Failed to Prepare", "prepare() failed");

        }
        
        micRecorder.start();

    }

    private void stopRecordMic() {
        try{
            micRecorder.stop();
            micRecorder.release();
        }catch(RuntimeException stopExep){
            // do nothing
        }

    }

};