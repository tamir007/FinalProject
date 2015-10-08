package com.app.td.calltranscripts;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by daniel zateikin on 26/09/2015.
 */
public class GetContactsFromText {

    public static final String debugTag = "debug";

    public static String readContacts(Activity activity) {
        String contacts = "";
        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Log.d("debug" , "has number" );
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{ id }, null);
                    while (pCur.moveToNext())
                    {
                        String contactNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts = contacts + name + " " + contactNumber +  "\n";
                        break;
                    }
                    pCur.close();
                    Log.d("debug", "got number");

                    // get the phone number

                }
            }
        }
        Log.d("debug" , "readContacts okay" );
        return contacts;
    }


    public static String[] getMentionedContacts(String text, String contacts){

        Log.i(debugTag , "get Mentioned Contacts");
        //String contacts = readContacts(activity);
        String[] listOfContacts = contacts.split("\n");
        String[] listOfContactNames = new String[listOfContacts.length];

        for(int i = 0 ; i < listOfContacts.length ; i++){
            listOfContactNames[i] = (listOfContacts[i]).split(" ")[0];
        }
        //String[] dic = message.split("\\W+");
        String[] listOfWords = text.split("\\W+");
//        for (int i = 0 ; i < listOfWords.length ; i++){
//            Log.i(debugTag , listOfWords[i]);
//        }
        String mentioned = "";
        for (int i = 0 ; i < listOfWords.length ; i++){
            for (int j = 0 ; j < listOfContactNames.length ; j++){
                if (listOfContactNames[j].toLowerCase().equals(listOfWords[i].toLowerCase())){
                    mentioned += listOfWords[i] + " " + listOfContacts[j].split(" ")[1]  + "\n";
                    break;
                }
            }
        }
        return mentioned.split("\n");
    }
}

















