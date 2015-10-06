package com.app.td.calltranscripts;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class BagOfWords {

    ArrayList<HashMap<String,Double>> samples;
    ArrayList<Double> tags;
    ArrayList<Double> w_vec;
    public static final String debugTag = "debug";
    String[] common;
    double learningRate;
    public BagOfWords(double learningRate){
        samples = new ArrayList<HashMap<String,Double>>();
        w_vec = new ArrayList<Double>();
        tags = new ArrayList<Double>();
        this.learningRate = learningRate;
        loadCommon();
    }

    private void loadCommon() {
        String text = getTextFromFilePath(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "data/common.txt");
        common = text.split("[,]");
    }

    public void saveData() {

        try {
            PrintWriter writer;
            writer = new PrintWriter("data/tags.txt", "UTF-8");
            for(double tag : tags){
                writer.println(tag);
            }

            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("couldnt write to file tags.txt");
        }

        try {
            PrintWriter writer;
            writer = new PrintWriter("data/wVec.txt", "UTF-8");
            for(double wCoef : w_vec){
                writer.println(wCoef);
            }

            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            System.out.println("couldnt write to file wVec.txt");
        }

        try{
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/TRANSCRIPTS");
            File[] listOfFiles = folder.listFiles();
            File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/OLD_TRANSCRIPTS");
            dir.mkdir();
            if(listOfFiles[0].renameTo(new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    "/OLD_TRANSCRIPTS/" + listOfFiles[0].getName()))){
                Log.d("debug" , "File is moved successful!");
            }else{
                Log.d("debug", "File is failed to move!");
            }
        }catch(Exception e){
            Log.d("debug", "couldnt move new call to transcript directory");
        }



    }

    private String getUserInput(){
        Scanner sc = new Scanner(System.in);
        return sc.nextLine();
    }

    // returns sample with largest loss
    private int getLargestLoss(){
        double maxLoss = 0.0;
        double currentLoss = 0.0;
        int i = 0;
        int largestIndex = 0;
        for(HashMap<String, Double> sample : samples){
            currentLoss = calcLoss(sample,tags.get(i));
            if(maxLoss < currentLoss){
                maxLoss = currentLoss;
                largestIndex = i;
            }

            i = i+1;
        }

        return largestIndex;
    }

    public void optimizeKernelCoefficients() {

        int sampleNumber = getLargestLoss();
        System.out.println("largest loss samp number : " + sampleNumber);
        HashMap<String, Double> newSamp = samples.get(sampleNumber);
        double tag = tags.get(sampleNumber);
        double loss = calcLoss(newSamp,tag);
        System.out.println("w_vec before optimizing  : " + w_vec);
        System.out.println("the loss : " + loss);
        double tempResult = 0.0;

        while(loss >= 1.0){
            // (w_t+1)_i= (w_t)_i + y_i*kernel(x_i ,x)
            // fix coefficient vector with respect to samples
            for(int i = 0 ; i < w_vec.size() ; i++){
                tempResult = w_vec.get(i)+tag*kernel(newSamp, samples.get(i));
                w_vec.set(i, tempResult);
            }


            System.out.println("newest w_vec  : " +  w_vec);

            sampleNumber = getLargestLoss();

            newSamp = samples.get(sampleNumber);
            tag = tags.get(sampleNumber);
            System.out.println("sample : " + sampleNumber + " with loss : " + calcLoss(newSamp, tag));
            System.out.println("tag : " + tag);
            loss = calcLoss(newSamp, tag);
            System.out.println("loss : " + loss);
            System.out.println("preforming loop");
        }

    }

    public void addNewVectorToKernel(HashMap<String, Double> vec){
        samples.add(vec);
    }


    private double calcLoss(HashMap<String, Double> vec, double tag){
        return Math.max(0, 1-tag*calcHypothesis(vec));
    }

    public double calcHypothesis(HashMap<String, Double> vec){
        double sum = 0;
        int i = 0;
        for(HashMap<String, Double> doc : samples){
            System.out.println(i);
            sum += w_vec.get(i++)*kernel(doc,vec);
        }
        return sum;
    }


    // calc kernel for 2 documents
    private Double kernel(HashMap<String, Double> doc,
                          HashMap<String, Double> vec) {
        double result = 0.0;
        for (HashMap.Entry<String, Double> entry : doc.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            if(vec.containsKey(key)){
                result += vec.get(key)*value;
            }
        }
        return result;
    }

    /*
     * wVec.txt format : coefficient\n
     * 					 coefficient\n
     */
    public void loadWVector(String fileName){
        File vecFile = new File(fileName);
        int i = 0;
        if(vecFile.exists()){
            System.out.println("file exists w_vec");
            String[] vector = getTextFromFilePath(fileName).split("[\n \r]+");
            for(String str : vector){
                System.out.println("in loop : " + str);
                w_vec.add(i++,new Double(str));
            }
        }
    }

    public void loadTags(String fileName){
        File vecFile = new File(fileName);
        if(vecFile.exists()){
            String[] vector = getTextFromFilePath(fileName).split("[\n \r]+");
            for(String str : vector){
                tags.add(new Double(str));
            }
        }
    }


    public HashMap<String, Double> getMappingVector(String fileName){
        Log.i(debugTag , "getMappingVector" );
        String transcript = getTextFromFilePath(fileName);
        ArrayList<String> wordArray = getWordsArray(transcript);
        HashMap<String, Double> mapping = new HashMap<String, Double>();

        for(String word : wordArray){
            if(mapping.containsKey(word)){
                mapping.put(word, mapping.get(word) + 1.0);
            }else{
                mapping.put(word,  1.0);
            }
        }

        return mapping;
    }

    /**
     * label : true - wants to call boss after ,
     * false - does not want to call boss after
     */
    public ArrayList<String> getWordsArray(String transcript){
        Log.i(debugTag , "getWordsArray");
        String[] arr = transcript.split("[\n \r]+");
        ArrayList<String> toReturn = new ArrayList<String>();
        for(int i = 0 ; i < arr.length ; i++){
            arr[i] = arr[i].replaceAll("\\n", "");
            arr[i] = arr[i].replaceAll(" ", "");
            arr[i] = arr[i].replaceAll("\\r", "");
        }

        boolean shouldBreak;
        for(String str : arr){
            shouldBreak = false;
            for(String commonWord : common){
                if(commonWord.equals(str)){
                    shouldBreak = true;
                    break;
                }
            }
            if(shouldBreak) continue;

            // if not a common word
            toReturn.add(str);
        }

        return toReturn;
    }

    public String getTextFromFilePath(String filePath){
        Log.i (debugTag , "getTextFromFilePath");
        String text = "";
        try {
            text = getStringFromFile(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Read File : " + filePath);

        return text;

    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }

    /**
     * Input folder name and all calls will be uploaded to kernel
     * @param folderName
     */
    public void addCallsFromFolder(String folderName){
        File folder = new File(folderName);
        if(!folder.exists()) return;
        File[] listOfFiles = folder.listFiles();
        for(File fl : listOfFiles){
            Log.d("debug", "in files from old transcript folder");
            addNewVectorToKernel(getMappingVector(fl.getAbsolutePath()));
        }
    }


}
