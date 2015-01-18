package com.tum.ident.gaitrecognition;

import java.io.Serializable;

public class StepStatistics implements Serializable{
	private static final long serialVersionUID = 1L;
	public StepClusterList clusters = new StepClusterList();
	public StepCounter counter = new StepCounter();
}