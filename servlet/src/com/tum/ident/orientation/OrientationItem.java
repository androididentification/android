package com.tum.ident.orientation;

import java.io.Serializable;
import java.util.ArrayList;

public class OrientationItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final String TAG = "OrientationData";
	
	
	private int rx = 40;
	private int ry = 20;
	private int rz = 20;
	
	public long[][] timecounter = new long[7][24];
	
	private long[][][][] orientationCounter;
	private long counter = 0;
	
	OrientationItem(){
		this.counter = 0;
		this.orientationCounter = new long[rx][ry][rz][2];
	}
	

}
