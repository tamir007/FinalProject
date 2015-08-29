package com.app.td.calllistener;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        if (PhoneCallHandler.isInstalled) {
            checkBox.setChecked(true);
        }
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                File myFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/recData.txt");
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

    private void activateRecord() {

        PhoneCallHandler.isInstalled = true;
        Toast.makeText(getApplicationContext(), "Call Recorder Acativated", Toast.LENGTH_SHORT).show();


    }


    private void deActivateRecord() {
        PhoneCallHandler.isInstalled = false;
        Toast.makeText(getApplicationContext(), "Call Recorder Decativated", Toast.LENGTH_LONG).show();

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
