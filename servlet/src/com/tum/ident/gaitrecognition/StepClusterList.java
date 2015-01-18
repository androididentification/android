package com.tum.ident.gaitrecognition;

import java.io.Serializable;
import java.util.ArrayList;


public class StepClusterList implements Serializable{
	private static final long serialVersionUID = 1L;
	
	transient private static final String TAG = "StepData";
	
	public ArrayList<StepCluster> list = new ArrayList<StepCluster>();
	
	public long clusterIndex = 0;

	transient private StepCluster lastCluster = null;
	

	transient private int currentClusterCounter = 0;
}
