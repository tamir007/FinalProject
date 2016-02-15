package com.app.td.calltranscripts.Predictors.PST;


public class Runner {

	public static void howToRun(String[] args) {
		
		char[] callLog = {'1' , '2' , '3' , '4', '5', '6'};
		PSTMultiClassClassifier classifer = new PSTMultiClassClassifier(0, callLog);
		
		for(int i = 0 ; i < 100; i++){
			Double[] dbl = {};
			char prediction = classifer.predict(dbl);
//			System.out.println(prediction);
//			System.out.println("Enter real next memeber");
//			Scanner scan = new Scanner(System.in);
//			String s = scan.next();
//			char c = s.charAt(0);
			char c = 'a';
			classifer.feedback(c);
			
		}

	
	}

}
