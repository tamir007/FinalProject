
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*; 

public class BagOfWords {

	ArrayList<HashMap<String,Double>> samples;
	ArrayList<Double> tags;
	ArrayList<Double> w_vec;
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
		String text = getTextFromFilePath("data/common.txt");
		common = text.split("[,]");
		
	}

	public static void main(String[] args){

		System.out.println("Load previous knowledge");

		BagOfWords bag = new BagOfWords(1.0);
		
		bag.addCallsFromFolder("transcripts");
		bag.loadWVector("data/wVec.txt");
		bag.loadTags("data/tags.txt");
		
		System.out.println("Analyzing new call");
		File folder = new File("newCalls");
		File[] listOfFiles = folder.listFiles();
		if(listOfFiles.length == 0){
			System.out.println("no new calls");
		}
		
		for(int i = 0 ; i < listOfFiles.length ; i++){
			HashMap<String, Double> newCall = bag.getMappingVector((listOfFiles[i]).getAbsolutePath());
			if (bag.samples.size() == 0){
				bag.samples.add(newCall);
				bag.w_vec.add(1.0);
				bag.tags.add(1.0);
				bag.saveData();
				continue;
			}
			
			System.out.println(newCall);
			double result = bag.calcHypothesis(newCall);
			double signedResult = Math.signum(result);
			if(signedResult == 1.0){
				System.out.println("wants to call boss with :  " + result);
			}else if(signedResult == -1.0){
				System.out.println("Dont want to call boss with : " + result);
			}else{
				System.out.println("Dont want to call boss with : " + result);
			}
			System.out.println("If the result was correct enter true, otherwise enter false");
			
			boolean hasAnswered = false;
			while(hasAnswered == false){
				String answer = bag.getUserInput();
				
				if(answer.equals("true")){
					bag.addNewVectorToKernel(newCall);
					hasAnswered = true;
					bag.w_vec.add(signedResult);
					bag.tags.add(signedResult);
				}else if(answer.equals("false")){
					bag.w_vec.add(0.0);
					bag.tags.add(-signedResult);
					bag.samples.add(newCall);
					bag.optimizeKernelCoefficients();
					hasAnswered = true;
				}else{
					System.out.println("wrong input! true or false only!");
					System.out.println("For correct result enter true, for incorrect result enter false");
				}
			}
			
			
			bag.saveData();
			System.out.println("The new coefficent vector is : " + bag.w_vec);
			System.out.println("Bag of words has been updated in respect to new call");
		}
		


	}

	private void saveData() {
		
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
			File folder = new File("newCalls");
			File[] listOfFiles = folder.listFiles();
			if(listOfFiles[0].renameTo(new File("transcripts/" + listOfFiles[0].getName()))){
	    		System.out.println("File is moved successful!");
	    	   }else{
	    		System.out.println("File is failed to move!");
	    	   }
		}catch(Exception e){
			System.out.println("couldnt move new call to transcript directory");
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

	private void optimizeKernelCoefficients() {
		
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

	private void addNewVectorToKernel(HashMap<String, Double> vec){
		samples.add(vec);
	}


	private double calcLoss(HashMap<String, Double> vec, double tag){
		return Math.max(0, 1-tag*calcHypothesis(vec));
	}

	private double calcHypothesis(HashMap<String, Double> vec){
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
	private void loadWVector(String fileName){
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

	private void loadTags(String fileName){
		File vecFile = new File(fileName);
		if(vecFile.exists()){
			String[] vector = getTextFromFilePath(fileName).split("[\n \r]+");
			for(String str : vector){
				tags.add(new Double(str));
			}
		}
	}


	public HashMap<String, Double> getMappingVector(String fileName){
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
	 * @param fileName
	 * @param label
	 */
	public ArrayList<String> getWordsArray(String transcript){
		String[] arr = transcript.split("[\n \r]+");
		ArrayList<String> toReturn = new ArrayList<String>();
		for(int i = 0 ; i < arr.length ; i++){
			arr[i] = arr[i].replaceAll("\\n", "");
			arr[i] = arr[i].replaceAll(" ", "");
			arr[i] = arr[i].replaceAll("\\r", "");
		}
		
		boolean shouldBreak = false;
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
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
			text = text.toLowerCase();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("get Text from file path " + filePath + " has failed");

		}

		System.out.println("Read File : " + filePath);

		return text;

	}

	/**
	 * Input folder name and all calls will be uploaded to kernel
	 * @param folderName
	 */
	public void addCallsFromFolder(String folderName){
		File folder = new File(folderName);
		File[] listOfFiles = folder.listFiles();
		for(File fl : listOfFiles){
			addNewVectorToKernel(getMappingVector(fl.getAbsolutePath()));
		}
	}


}
