package com.app.td.calltranscripts;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Console;

public class SuggestActivity extends AppCompatActivity {

    private boolean MLcycleAns ;
    private boolean wasChecked ;
    public final String debugTag = "debug";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(debugTag, "on create (SUGGEST)");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest);
        wasChecked = false;
        Intent intent = getIntent();
        String[] mentionedNames = intent.getStringArrayExtra(PhoneCallHandlerTrans.MENTIONED_NAMES_EXTRA);
        MLcycleAns = false;

        // create the layout params that will be used to define how your
        // button will be displayed

        makeButtons(mentionedNames);



//        if (cycle){
//            PhoneCallHandlerTrans.speech.predictionCorrect();
//        }
//        else{
//            PhoneCallHandlerTrans.speech.predictionIncorrect();
//        }

    }

    private void makeButtons (final String[] mentionedNames) {
        TableLayout myTable = (TableLayout) findViewById(R.id.tableForButtons);

        for (int i = 0 ; i < mentionedNames.length ; i++){
            TableRow tableRow = new TableRow(this);
            myTable.addView(tableRow);
            Button myButton = new Button(this);
            myButton.setText(mentionedNames[i]);
            myButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Log.i(debugTag , "clicked");
                    MLcycleAns = true;
                    // call number of contact name
                    //PhoneCallHandlerTrans.speech.predictionCorrect();
                    Button b = (Button)v;
                    String buttonText = b.getText().toString();
                    String number = buttonText.split(" ")[1];
                    Log.d("debug", "call : " + number);
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + number));
                    startActivity(callIntent);
                    Log.d("debug", "number to call : " + number);

                }
            });
            tableRow.addView(myButton);
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
        if (MLcycleAns){
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
