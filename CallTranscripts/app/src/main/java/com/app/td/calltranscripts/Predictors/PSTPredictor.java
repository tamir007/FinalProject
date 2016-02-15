package com.app.td.calltranscripts.Predictors;

import com.app.td.calltranscripts.Predictors.PST.PSTMultiClassClassifier;

/**
 * Created by user on 14/02/2016.
 */
public class PSTPredictor implements Predictor {

    private PSTMultiClassClassifier classifier;
    char[] members = {'1' , '2' , '3' , '4', '5', '6'};

    public PSTPredictor(){
        classifier = new PSTMultiClassClassifier(0,members);
    }

    @Override
    public int predict() {
        Double[] dbl = {};
        char prediction = classifier.predict(dbl);
        return Character.getNumericValue(prediction);
    }

    @Override
    public void feedback(int tag) {
        char numTag = (char)(tag - 48);
        classifier.feedback(numTag);
    }
}
