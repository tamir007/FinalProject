package com.app.td.calltranscripts.AppUtils;

import android.util.Log;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializationUtil {

    // deserialize to Object from given file
    public static Object deserialize(String fileName) {

        FileInputStream fis;
        try {
            fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.i("debug", "deserialize failed");
            e.printStackTrace();
        }

        return null;
    }
    /**
     * This is for files from /resources
     * @param myInput - inputstream of file
     * @return
     */
    public static Object deserialize(InputStream myInput) {

        FileInputStream fis;

        try {

            fis = (FileInputStream)myInput;
            ObjectInputStream ois = new ObjectInputStream(fis);
            Object obj = ois.readObject();
            ois.close();
            return obj;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("ERROR1");
            e.printStackTrace();
        }

        return null;
    }

    // serialize the given object and save it to file
    public static void serialize(Object obj, String fileName){
        try{
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(obj);
            fos.close();
        }catch(Exception e){
            Log.i("debug", "serialization failed");
        }
    }
}