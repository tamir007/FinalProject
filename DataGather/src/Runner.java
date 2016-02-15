import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;


public class Runner {

//	public static void main(String[] args) {
//		
//		char[] callLog = { '1' , '2' , '3' , '4'};
//		PSTMultiClassClassifier classifer = null;
//		String filename = "classifier.ser";
//		
//		// read the object from file
//	    // save the object to file
//	    FileInputStream fis = null;
//	    ObjectInputStream in = null;
//	    try {
//	      fis = new FileInputStream(filename);
//	      in = new ObjectInputStream(fis);
//	      classifer = (PSTMultiClassClassifier) in.readObject();
//	      in.close();
//	    } catch (Exception ex) {
//	      ex.printStackTrace();
//	    }
//	    
//	    if(classifer == null){
//	    	classifer = new PSTMultiClassClassifier(0, callLog);
//	    }
//	    
//		for(int i = 0 ; i < 30; i++){
//			Double[] dbl = {};
//			char prediction = classifer.predict(dbl);
//			System.out.println(prediction);
//			System.out.println("Enter real next memeber");
//			Scanner scan = new Scanner(System.in);
//			String s = scan.next();		
//			char c = s.charAt(0);
//			classifer.feedback(c);
//
//		}
//
//
//	    // save the object to file
//	    FileOutputStream fos = null;
//	    ObjectOutputStream out = null;
//	    try {
//	      fos = new FileOutputStream(filename);
//	      out = new ObjectOutputStream(fos);
//	      out.writeObject(classifer);
//
//	      out.close();
//	    } catch (Exception ex) {
//	      ex.printStackTrace();
//	    }
//	    
//
//
//	}
	
	public static void main(String[] args) {
		
				
				
	}
	
	
	private static double[] analyzeDB(DB theDB){
		
		char[] callLog = { '1' , '2' , '3' , '4','5','6'};
		PSTMultiClassClassifier classifer = null;
		String filename = "classifier.ser";
		
		// read the object from file
	    // save the object to file
	    FileInputStream fis = null;
	    ObjectInputStream in = null;
	    try {
	      fis = new FileInputStream(filename);
	      in = new ObjectInputStream(fis);
	      classifer = (PSTMultiClassClassifier) in.readObject();
	      in.close();
	    } catch (Exception ex) {
	      ex.printStackTrace();
	    }
	    
	    if(classifer == null){
	    	classifer = new PSTMultiClassClassifier(0, callLog);
	    }
	    
		for(int i = 0 ; i < 30; i++){
			Double[] dbl = {};
			char prediction = classifer.predict(dbl);
			System.out.println(prediction);
			System.out.println("Enter real next memeber");
			Scanner scan = new Scanner(System.in);
			String s = scan.next();		
			char c = s.charAt(0);
			classifer.feedback(c);

		}
		
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

	
	private static double[] parseDoc(String theCall){
		
		String[] words = theCall.split(" ");
		HashMap<String,Integer> myDict = (HashMap<String, Integer>) SerializationUtil.deserialize("C:/Users/user/workspace/PST/src/dict.ser");
		double[] vec = new double[myDict.size()];
		Integer slot = null;
		HashMap<Integer,String> sanity = new HashMap<>();
		for(String str : words){
			
			if( (slot = myDict.get(str)) != null){
				sanity.put(slot,str);
				vec[slot] += 1.0;			
			}
		}
		
		for(int i = 0 ; i < vec.length ; i++){
			if (vec[i] != 0.0){
				System.out.println(sanity.get(i) + " : #" + vec[i]);
			}
		}
		
		return vec;
		
	}
	
	
	private static double calcEucleadenDist(double aLat, double aLong, double bLat , double bLong){
		
		return Math.sqrt(Math.pow(aLat-bLat,2) + Math.pow(aLong-bLong, 2));
	}
	
	
	private static void createDict(String path){

			String[] dict = readList("C:/Users/user/workspace/PST/src/10000.txt");
			for(int i = 0 ; i < dict.length ; i++){
				dict[i] = dict[i].toLowerCase().replace("\r", "");
			}

			String[] stopWords = readList("C:/Users/user/workspace/PST/src/stopWords.txt");
			for(int i = 0 ; i < stopWords.length ; i++){
				stopWords[i] = stopWords[i].toLowerCase().replace("\r", "");
			}
			
			for(int i = 0 ; i < dict.length ; i++){
				String str = dict[i];
				for(String stop : stopWords){
					if(str.equals(stop)){						
						dict[i] = "**";
						break;
					}
				}			
				
			}
			String cleanFile = "";
			HashMap<String,Integer> finalDict = new HashMap<String,Integer>();
			int slot = 0;
			// build clean dictionary
			for(String str : dict){
				if( str.equals("**")) continue;
				finalDict.put(str , slot++);
				if(slot != 1){
					cleanFile += "\n";
				}
				cleanFile += str;
			}
			
			SerializationUtil.serialize(finalDict, "C:/Users/user/workspace/PST/src/dict.ser");
			try {
				PrintWriter out = new PrintWriter("C:/Users/user/workspace/PST/src/cleanWords.txt");
				out.println(cleanFile);
				out.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				System.out.println("error");
				e.printStackTrace();
			}	
		
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

}
