/**
 * Created by user on 14/02/2016.
 */
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    		System.out.println("ERROR2");
    	}

    }

}
