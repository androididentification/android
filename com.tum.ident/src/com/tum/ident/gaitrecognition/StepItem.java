package com.tum.ident.gaitrecognition;

import java.io.Serializable;

import com.tum.ident.util.Util;

public class StepItem implements Serializable {
	private static final long serialVersionUID = 1L;	
	
	@SuppressWarnings("unused")
	transient private static final String TAG = "StepData";
	
	private final int radius = 8;

	private long duration;
	private transient long startTime;
	private double[] data = null;
	private double dtwValue = 0;
	private double weight = 0;
	
	
	public int getRadius() {
		return radius;
	}

	public long getDuration() {
		return duration;
	}

	public long getStartTime() {
		return startTime;
	}

	public double[] getStepData() {
		return data;
	}

	public double getDtwValue() {
		return dtwValue;
	}

	public double getWeight() {
		return weight;
	}

	public void setDistanceValue(double distance) {
		dtwValue = distance;
	}

	public int length(){
		return data.length;
	}
	
	public double[] getData(){
		return data;
	}
	
	public void setWeight(double weight) {
		this.weight = weight;
	}

	public StepItem(long duration, double[] data) {
		this.duration = duration;
		this.data = data;
		this.startTime = 0;
	}

	public StepItem(long duration, long startTime, double[] data) {
		this.duration = duration;
		this.data = data;
		this.startTime = startTime;
	}

	public double dwtDistance(StepItem stepItem) {
		double result = -1;
		if (data != null) {
			result = Util.dwtDistance(this.data, stepItem.data, radius);
		}
		return result;
	}
}
