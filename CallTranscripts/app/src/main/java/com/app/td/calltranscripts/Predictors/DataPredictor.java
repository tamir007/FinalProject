package com.app.td.calltranscripts.Predictors;

import com.app.td.calltranscripts.DB.Location;
import com.app.td.calltranscripts.DB.User;

import java.util.ArrayList;

/**
 * Created by user on 14/02/2016.
 */
public class DataPredictor{

    ArrayList<User> users;
    int numOfUsers;
    public DataPredictor(ArrayList<User> users) {
        this.users = users;
        this.numOfUsers = users.size();
    }

    public double[] predict(int aTime, Location aLoc) {
        double[] scores = new double[numOfUsers];
        for(User user : users){
            ArrayList<Integer> timestamps = user.getTimestamps();
            ArrayList<Location> locations = user.getLocations();
            for(int i = 0 ; i < timestamps.size() ; i++){
                double timePower = getGaussTimeDiff(aTime,timestamps.get(i));
                double locPower = getGaussLocFigg(aLoc,locations.get(i));
            }
        }
        return scores;
    }

    public double getGaussTimeDiff(int aTime , int bTime){
        return -Math.pow((double)(aTime - bTime),2)/2;
    }
    public double getGaussLocFigg(Location aLoc , Location bLoc){
        double euclidianDistance = Math.sqrt(Math.pow(aLoc.getLat() - bLoc.getLat(),2)
        + Math.pow(aLoc.getLonge() - bLoc.getLonge(),2));

        return -Math.pow(euclidianDistance,2)/2;
    }

}