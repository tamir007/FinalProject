package com.app.td.calltranscripts.Predictors.PST;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PSTMultiClassClassifier implements Serializable
{	
	private static final long serialVersionUID = 1L;
	
	//=============
	// Data Members
	//=============
	private Map<String, Map<String, Double>> 	_g;
	private Map<String, Double[]> 				_w;
	private char[]								_lableSet;
	private String 								_context;
	private Double[]		 					_xt;
	private double								_ht;
	private int									_t;
	private char								_lastPredictedLabel;
	public String[]								callerNumbers;
	/*
	 * Constructor
	 */
	public PSTMultiClassClassifier(int featureLength, char[] labelSet){
		
		_context = new String();
		_t = 0;
		_lableSet = labelSet;
		_g = new HashMap<String, Map<String,Double>>();
		_w = new HashMap<String, Double[]>();
		
		String currLabel;
		for (int i = 0; i < labelSet.length; ++i){
			
			currLabel = ""+labelSet[i];
			_g.put(currLabel, new HashMap<String, Double>());
			_w.put(currLabel, new Double[featureLength]);
			
			for (int j = 0; j < featureLength; ++j)
				_w.get(currLabel)[j] = 0.0d;

			_g.get(currLabel).put("", 0.0d);
		}
	}
	
	/*
	 * Predicts the next label according to the labels history and to the specified feature vector
	 */
	public char predict(Double[] x){
		
		++_t;
		_xt = x;
		
		// Calculate j
		int j = 0;
		if (_context.length() > 0){
			while (_g.get(""+_lableSet[0]).containsKey(_context.substring(_context.length() - 1 - j)))
				++j;
		}
		// Calculate ht for each classifier and take the one with the maximal value //
		//==========================================================================//
		double max_ht = -999999999.0;
		String arg_max = "";
		double curr_g = 0.0d;
		double curr_ht = 0.0d;
		String currLabel;
		
		for (int i = 0; i < _lableSet.length; ++i){
			currLabel = ""+_lableSet[i];
			curr_g = 0.0d;
			
			for (int k = 0; k < j; ++k)
				curr_g += Math.pow(2, (-1)*k/2) * _g.get(currLabel).get(_context.substring(_context.length() - 1 - k));
			
			curr_ht = Utils.dotProduct(_w.get(currLabel), _xt) + curr_g;
			
			if (curr_ht > max_ht){
				max_ht = curr_ht;
				arg_max = currLabel;
			}
		}
		_ht = max_ht;
		
		if (_ht == 0){
			_lastPredictedLabel = '?';
			return '?';
		} else {
			_lastPredictedLabel = arg_max.charAt(0);
			return _lastPredictedLabel;
		}
	}

	/*
	 * Updates the tree and weights vector according to the correct label specified
	 */
	public void feedback(char labelSign){
		
		double isCorrect = (labelSign == _lastPredictedLabel ? Math.signum(_ht) : -1.0 * Math.signum(_ht));
		
		double loss = Math.max(0, 1 - isCorrect * _ht);
		
		double tau = loss / (Math.pow((Utils.norm(_xt)), 2) + 3);
		
		int dt = _t - 1;
		
		if (isCorrect < 0 || _ht == 0.0d){
			_w.put(""+labelSign, Utils.addVectors(_w.get(""+labelSign), Utils.multScalar(_xt, 1.0d * tau)));
			
			if (_ht != 0.0d){
				_w.put(""+_lastPredictedLabel, Utils.addVectors(_w.get(""+_lastPredictedLabel), Utils.multScalar(_xt, -1.0d * tau)));
			}
				
		}
		// Adding nodes to the tree
		//=========================
		String s = "";
		for (int i = 1; i <= dt; ++i){
			
			s = _context.substring(_context.length() - i);
			
			for (int k = 0; k < _lableSet.length; ++k){
				
				if (!_g.get(""+_lableSet[k]).containsKey(s))
					_g.get(""+_lableSet[k]).put(s, 0.0d);
				
				if ((_lableSet[k] == _lastPredictedLabel && isCorrect < 0.0d) || _ht == 0.0d){
					_g.get(""+_lableSet[k]).put(s, 
							_g.get(""+_lableSet[k]).get(s) + Math.pow(2, -1*s.length() / 2.0d) * tau * -1.0d);
				} else if (isCorrect > 0) {
					_g.get(""+_lableSet[k]).put(s, 
							_g.get(""+_lableSet[k]).get(s) + Math.pow(2, -1*s.length() / 2.0d) * tau);
				}
			}
		}
		_context += labelSign;
	}
	
	public int getTimeStep()
	{
		return _t;
	}
}