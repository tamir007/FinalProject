package com.app.td.calltranscripts;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SuggestActivity extends AppCompatActivity {

    private boolean cycle ;
    private boolean wasChecked ;
    public final String debugTag = "debug";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(debugTag, "on create (SUGGEST)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        wasChecked = false;
        Intent intent = getIntent();
        String[] mentoinedNames = intent.getStringArrayExtra(PhoneCallHandlerTrans.MENTIONED_NAMES_EXTRA);
        cycle = false;

        // create the layout params that will be used to define how your
        // button will be displayed
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        for (int i = 0 ; i < mentoinedNames.length ; i++){

            // Create LinearLayout
            LinearLayout ll = new LinearLayout(this);
            ll.setOrientation(LinearLayout.HORIZONTAL);

            // Create Button
            final Button btn = new Button(this);
            // Give button an ID
            btn.setId(i + 1);
            btn.setText(mentoinedNames[i]);
            // set the layoutParams on the button
            btn.setLayoutParams(params);

            final int index = i;
            // Set click listener for button
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {

                    cycle = true;

                    Toast.makeText(getApplicationContext(),
                            "Clicked Button Index :" + index,
                            Toast.LENGTH_LONG).show();

                }
            });

            //Add button to LinearLayout
            ll.addView(btn);
            //Add button to LinearLayout defined in XML

        }
        if (cycle){
            PhoneCallHandlerTrans.speech.predictionCorrect();
        }
        else{
            PhoneCallHandlerTrans.speech.predictionIncorrect();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_suggest, menu);
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

    public void checkCycle(){
        Log.i(debugTag , "checkCycle");

        if (wasChecked){
            Log.i(debugTag , "was checked already");
            return;
        }
        if (cycle){
            PhoneCallHandlerTrans.speech.predictionCorrect();
        }
        else{
            PhoneCallHandlerTrans.speech.predictionIncorrect();
        }
        wasChecked = true;
    }

    @Override
    protected void onStop() {
        Log.i(debugTag , "onStop (SUGGEST)");
        super.onStop();
        checkCycle();
    }

    @Override
    protected void onDestroy() {
        Log.i(debugTag , "onDestroy (SUGGEST)");
        super.onDestroy();
        checkCycle();

    }

    @Override
    protected void onPause() {
        Log.i(debugTag , "onPause (SUGGEST)");
        super.onPause();
        checkCycle();
    }
}
