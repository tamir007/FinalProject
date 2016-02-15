package com.app.td.calltranscripts.AppUtils;

import android.content.Context;
import android.content.res.Resources;

import com.app.td.calltranscripts.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import static com.app.td.calltranscripts.AppUtils.SerializationUtil.deserialize;

/**
 * Created by user on 15/02/2016.
 */
public class RepresentationUtils {
    // ==================== REAL DATA ========================

    private final static int numOfLocations = 4;

    // jerusalem location
    private final static double jerusLat = 31.768319;
    private final static double jerusLong = 35.21370999999999;

    // tel aviv location
    private final static double telAvivLat = 32.0852999;
    private final static double telAvivLong = 32.0852999;

    // Haifa location
    private final static double haifaLat = 32.7940463;
    private final static double haifaLong = 34.98957100000007;

    // beersheva location
    private final static double beershevaLat = 31.252973;
    private final static double beershevaLong = 34.791462000000024;

    /**
     *
     * @param call - String of call transcript
     * @param myLat - latitude of user
     * @param myLong - longitude of user
     * @param clock - 0-47 int where 0 =  00:00 , 47 = 23:30, representing time arround clock
     * @param day - 0-6 int representing day , 0 = sunday , 6 = saturday
     * @return double[] representing one call
     */
        public static double[] mapData(Context context, String call, double myLat, double myLong, int clock, int day){

            double[] clockVec = getClockVec(clock);
            double[] dayVec = getDayVec(day);
            double[] locVec = getLocationVec(myLat, myLong);
            double[] wordRep = parseDoc(call, context);
            // append all array to create final vector
            double[] finalVec = appendArrays(wordRep,clockVec);
            finalVec = appendArrays(finalVec,dayVec);
            finalVec = appendArrays(finalVec,locVec);

            return finalVec;
    }

    private static double[] appendArrays(double[] aArray, double[] bArray){
        int aLen = aArray.length;
        int bLen = bArray.length;
        double[] newList = new double[aLen + bLen];

        for(int i = 0 ; i < aLen ; i++){
            newList[i] = aArray[i];
        }

        for(int i = 0 ; i < bLen ; i++){
            newList[i + aLen] = bArray[i];
        }

        return newList;

    }


    private static double[] parseDoc(String theCall, Context context){

        String[] words = theCall.split(" ");
        // get dictionary
        InputStream myStream = context.getResources().openRawResource(R.raw.dict);
        HashMap<String,Integer> myDict = (HashMap<String, Integer>)deserialize(myStream);

        // build vector
        double[] vec = new double[myDict.size()];
        Integer slot = null;
        HashMap<Integer,String> sanity = new HashMap<>();
        for(String str : words){
            // if word exists raise occurences by 1
            if( (slot = myDict.get(str)) != null){
                sanity.put(slot,str);
                vec[slot] += 1.0;
            }
        }

        return vec;
    }

    private static double calcEucleadenDist(double aLat, double aLong, double bLat , double bLong){

        return Math.sqrt(Math.pow(aLat-bLat,2) + Math.pow(aLong-bLong, 2));
    }

    private static String[] readList(String path){
        File file = new File(path);
        byte[] data;
        FileInputStream fis;
        String wordList;
        try {
            fis = new FileInputStream(file);
            data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();
            wordList = new String(data, "UTF-8");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

        return wordList.split("\n");
    }


    private static double[] getLocationVec(double myLat, double myLong){

        // convert coordinations to distance from locations

        double[] locVec = new double[numOfLocations];
        locVec[0] = calcEucleadenDist(myLat,myLong,jerusLat,jerusLong);
        locVec[1] = calcEucleadenDist(myLat,myLong,telAvivLat,telAvivLong);
        locVec[2] = calcEucleadenDist(myLat,myLong,haifaLat,haifaLong);
        locVec[3] = calcEucleadenDist(myLat,myLong,beershevaLat,beershevaLong);

        return locVec;
    }

    private static double[] getDayVec(int day){
        // convert day to triangle around time area ( cyclic solution)

        int weekSize = 7;
        double[] dayVec = new double[weekSize];
        dayVec[(day-1)%weekSize] = 0.5;
        dayVec[day] = 1.0;
        dayVec[(day+1)%weekSize] = 0.5;

        return dayVec;
    }

    private static double[] getClockVec(int clock){
        // convert clock to triangle around time area ( cyclic solution)

        int clockSize = 48;
        double[] clockVec = new double[clockSize];
        clockVec[(clock-1)%clockSize] = 0.5;
        clockVec[(clock-1)%clockSize] = 1.0;
        clockVec[clock] = 2.0;
        clockVec[(clock+1)%clockSize] = 1.0;
        clockVec[(clock-1)%clockSize] = 0.5;

        return clockVec;
    }

}