package com.tum.ident.gaitrecognition;

import java.io.Serializable;
import java.util.ArrayList;

public class StepItem implements Serializable{
	private static final long serialVersionUID = 1L;
	final int radius = 8;
	transient private static final String TAG = "StepData";


	long duration;
	transient long startTime;
	public double[] stepData = null;

	public double dtwValue = 0;
	
	public double weight = 0;
	
	public StepItem(long duration, double[] stepData){
		this.duration = duration;
		this.stepData =  stepData;
	}
	
}
