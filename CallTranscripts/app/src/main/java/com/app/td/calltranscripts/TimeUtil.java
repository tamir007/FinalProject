package com.app.td.calltranscripts;

import java.util.Date;

/**
 * Created by user on 14/02/2016.
 */
public class TimeUtil {

    public static long getLongTime(){
        return (new Date()).getTime();
    }

    public static int getClockFromLong(long time) {
        Date date = new Date(time);
        // hour in 24 hour clock
        int hour = date.getHours();
        // minutes from last hour
        int minutes = date.getMinutes();
        int halfHour = 0;
        if (minutes < 30) {
            halfHour = 0;
        } else {
            halfHour = 1;
        }
        // 0 = Sunday .... 0 = Saturday
        int day = date.getDay();
        // time in 24 hour clock, in minutes
        return hour * 2 + halfHour;
    }

    public static int getDayFromLong(long time) {
        Date date = new Date(time);
        // 0 = Sunday .... 0 = Saturday
        return date.getDay();

    }

}