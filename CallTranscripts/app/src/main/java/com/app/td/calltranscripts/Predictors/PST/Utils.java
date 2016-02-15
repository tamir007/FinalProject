package com.app.td.calltranscripts.Predictors.PST;

import android.database.Cursor;
import android.provider.CallLog;

public class Utils{
    
	// Private constructor
    private Utils(){
    }
    
    /*
     * Calculates the dot product between two vectors
     */
    public static double dotProduct(Double[] v1, Double[] v2){
    	
    	Double res = 0.0;
 
    	for (int i = 0; i < Math.min(v1.length, v2.length); ++i)
    		res += v1[i] * v2[i];
    	
    	return res;
    }
    
    /*
     * Calculates the norm of the specified vector
     */
    public static double norm(Double[] v){
    	
    	double res = 0.0d;
    	
    	for (int i = 0; i < v.length; ++i)
    		res += Math.pow(v[i], 2);
    	
    	return Math.sqrt(res);
    }
    
    /*
     * Multiplies the specified vector with the specified scalar (element wise)
     */
    public static Double[] multScalar(Double[] v, double scalar){
    	
    	for (int i = 0; i < v.length; ++i)
    		v[i] *= scalar;
    	
    	return v;
    }
    
    /*
     * Adds two vectors
     */
    public static Double[] addVectors(Double[] v1, Double[] v2){
    	
    	Double[] res = new Double[v1.length];
    	
    	for (int i = 0; i < res.length; ++i)
    		res[i] = v1[i] + v2[i];
    	
    	return res;
    }
}
