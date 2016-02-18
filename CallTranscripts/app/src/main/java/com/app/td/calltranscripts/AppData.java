package com.app.td.calltranscripts;

import java.io.Serializable;

/**
 * Created by daniel zateikin on 17/02/2016.
 */
public class AppData implements Serializable{

    private static final long serialVersionUID = -7239060215922241597L;
    private boolean checkBox;
    private String[] fiveMostCalled;

    public AppData(boolean check, String[] fiveMostCalled){
        this.checkBox = check;
        this.fiveMostCalled = fiveMostCalled;
    }

    public AppData() {
        this.checkBox = false;
        this.fiveMostCalled = null;
    }

    public void setCheckBox(boolean check){
        this.checkBox = check;
    }
    public boolean getCheckBox(){
        return  this.checkBox;
    }
    public String[] getFiveMostCalled(){
        return fiveMostCalled;
    }
    public void setFiveMostCalled(String[] fiveMostCalled){
        this.fiveMostCalled = fiveMostCalled;
    }

    public String getId(String phoneNumber){
        if (fiveMostCalled[0].equals(phoneNumber)){
            return "1";
        }
        if (fiveMostCalled[1].equals(phoneNumber)){
            return "2";
        }
        if (fiveMostCalled[2].equals(phoneNumber)){
            return "3";
        }
        if (fiveMostCalled[3].equals(phoneNumber)){
            return "4";
        }
        if (fiveMostCalled[4].equals(phoneNumber)){
            return "5";
        }
        return "6";

    }
}