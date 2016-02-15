package com.app.td.calltranscripts.AppUtils;

import java.util.Date;

/**
 * Created by user on 14/02/2016.
 */
public class TimeUtil {

    /*
        Converts 1970 long time to day of the week and minute of day.
        1st digit represents day from 1-7 , and next 4 digit represent
        time of day in minutes from 00:00 to 24:00 (0-1440)
        ex: 61439 is friday 23:59
     */
    public static int normalizeTime(long time){
        Date date = new Date(time);
        // hour in 24 hour clock
        int hour = date.getHours();
        // minutes from last hour
        int minutes = date.getMinutes();
        int halfHour = 0;
        if(minutes < 15 || minutes > 45 ){
            halfHour = 0;
        }else{
            halfHour = 1;
        }
        // 0 = Sunday .... 0 = Saturday
        int day = date.getDay();
        // time in 24 hour clock, in minutes
        int dayTime = hour*2 + halfHour;

        return day*10000 + dayTime;
    }

    public static int getCurrentNormTime(){
        Date date = new Date();
        return normalizeTime(date.getTime());
    }

    public static int getDay(int normTime){
        int numOfDay = (int)Math.floor(normTime/10000);
        return numOfDay;
    }

    public static int getMinuteOfDay(int normTime){
        int day = getDay(normTime)*10000;
        return normTime - day;
    }
}