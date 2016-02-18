//package com.app.td.calltranscripts;
//
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.util.Log;
//
///**
// * Created by daniel zateikin on 06/10/2015.
// */
//public class SuggestReceiver extends BroadcastReceiver {
//
//    public static final  String MENTIONED_NAMES_EXTRA = "Relevant names";
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        String debugTag = "debug";
//        //String[] names = {"avi" , "tali" , "boris"};
//        Log.i(debugTag , "received");
//        Intent myIntent = new Intent(context , SuggestActivity.class);
//        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        //myIntent.putExtra(MENTIONED_NAMES_EXTRA,names);
//        context.startActivity(myIntent);
//    }
//}
