package com.app.td.calltranscripts.DB;

import java.io.Serializable;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by user on 14/02/2016.
 */
public class User implements Serializable{

    private String contactName;
    private String contactNumber;
    private ArrayList<Integer> timestamps;
    private ArrayList<Location> locations;


    public User(String name,String number){
        contactName = name;
        contactNumber = number;
        timestamps = new ArrayList<>();
        locations = new ArrayList<>();
    }


    public void addTimestamp(int time) {
        this.timestamps.add(time);
    }

    public void addLocation(double x, double y) {
        Location loc = new Location(x,y);
        this.locations.add(loc);
    }

    public ArrayList<Integer> getTimestamps() {
        return timestamps;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }


    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }


}
