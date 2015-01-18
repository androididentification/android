package com.tum.servlets.identification;

public class IdentificationConfiguration {
	
	//StepDetector
	public static double     dwtThreshold = 10000;
	public static double     dwtAvgThreshold =  15000;
	public static int  accelerationArrayLength = 150;
	public static long cleanClusterTime = 3000000000L;
	
	public static long maxStepDuration = 1200000000L;  //average: 530973451
	public static long minStepDuration = 100000000L;  
	public static double maxLastStepDurationDiff = 200000000L;
}

