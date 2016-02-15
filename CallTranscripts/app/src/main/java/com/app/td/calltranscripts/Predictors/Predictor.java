package com.app.td.calltranscripts.Predictors;

/**
 * Created by user on 14/02/2016.
 */
public interface Predictor {

    int predict();
    void feedback(int tag);

}
