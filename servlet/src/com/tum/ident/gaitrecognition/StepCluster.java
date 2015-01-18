package com.tum.ident.gaitrecognition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.tum.servlets.identification.IdentificationConfiguration;



public class StepCluster implements Serializable {
	private static final long serialVersionUID = 1L;
	final int radius = 8;
	private static final String TAG = "StepData";
	public ArrayList<StepItem> steps = new ArrayList<StepItem>(); //todo transient;
	public byte[] stepData = null;
	public StepItem avgSteps = null;
	public  long     duration;    
	transient Iterator<StepItem> iterator = steps.iterator();
	public boolean merge = false;
	public boolean compareSteps = false;
	public long numSteps=0;
	double minAcceleration = -1;
	double maxAcceleration = 1;
	transient boolean changed = true;
	public double dtwSumMax = 0;
	public double dtwSumMin = IdentificationConfiguration.dwtThreshold*10;
	public long maxStepListSize = 20;
	public long lastUpdateTime =0;

	transient private double distancePuffer = 0;
	transient private StepItem distancePufferItem = null;
	transient boolean newCluster = false;
	transient boolean clusterFound = false;
	public long index;

	transient double timePuffer[] = null;

	

	
}
