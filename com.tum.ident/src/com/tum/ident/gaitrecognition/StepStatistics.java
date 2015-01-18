package com.tum.ident.gaitrecognition;

import java.io.Serializable;


public class StepStatistics implements Serializable {
	private static final long serialVersionUID = 1L;
	private StepClusterList clusters = new StepClusterList();
	private StepCounter counter = new StepCounter();
	
	public StepClusterList getClusters(){
		return clusters;
	}
	
	public StepCounter getCounter(){
		return counter;
	}

	public void setClusters(StepClusterList c) {
		clusters = c;
		if (clusters == null) {
			clusters = new StepClusterList();
		}
		else if (clusters.getList() == null) {
			clusters = new StepClusterList();
		}
	}

	public void setCounter(StepCounter c) {
		counter = c;
		if (counter == null) {
			counter = new StepCounter();
		}
	}
	
	
	
}
