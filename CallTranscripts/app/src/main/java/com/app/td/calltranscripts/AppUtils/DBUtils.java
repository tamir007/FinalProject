package com.app.td.calltranscripts.AppUtils;

import android.util.Log;

import com.app.td.calltranscripts.DB.DB;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by user on 14/02/2016.
 */
public class DBUtils {

    private static String DBPath = "DB PATH";

    public static void saveDB(DB theDB){
        SerializationUtil.serialize(theDB,DBPath);
        Log.i("debug", "after DB save");

    }

    public static DB loadDB(){
        DB newDB = (DB)SerializationUtil.deserialize(DBPath);
        Log.i("debug", "after DB load");

        return newDB;
    }
}
