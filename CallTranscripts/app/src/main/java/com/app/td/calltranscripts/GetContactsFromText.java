package com.app.td.calltranscripts;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;

import java.util.ArrayList;

/**
 * Created by daniel zateikin on 26/09/2015.
 */
public class GetContactsFromText {

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
                    contacts = contacts + name + "\n";

                    // get the phone number

                }
            }

        }
        return contacts;
    }


    public static String mentionedContacts(String text , String contacts){
        String[] listOfContacts = contacts.split("\n");
        String[] listOfWords = text.split(" ");
        String mentioned = "";
        for (int i = 0 ; i < listOfWords.length ; i++){
            for (int j = 0 ; j < listOfContacts.length ; j++){
                if (listOfContacts[j].toLowerCase().equals(listOfWords[i].toLowerCase())){
                    mentioned += listOfWords[i] + " ";
                    break;
                }
            }
        }
        return mentioned;
    }
}

















