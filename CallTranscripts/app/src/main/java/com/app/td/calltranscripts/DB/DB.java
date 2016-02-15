package com.app.td.calltranscripts.DB;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by user on 14/02/2016.
 */
public class DB implements Serializable{

    ArrayList<User> users;

    public DB() {
        users = new ArrayList<>();
    }

    public void addUser(User user){
        this.users.add(user);
    }

    public ArrayList<User> getUsers() {
        return users;
    }



}
