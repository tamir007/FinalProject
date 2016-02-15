package com.app.td.calltranscripts.DB;

import java.io.Serializable;

/**
 * Created by user on 14/02/2016.
 */
public class Location implements Serializable{

    public double getLat() {
        return lat;
    }

    public double getLonge() {
        return longe;
    }

    private double lat;
    private double longe;

    public Location(double lat, double longe) {
        this.lat = lat;
        this.longe = longe;
    }
}
