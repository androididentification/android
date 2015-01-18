package com.tum.ident.locations;

import java.io.Serializable;
import java.util.ArrayList;

public class LocationAreaList implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "LocationData";
	public double distanceThresholdMax = 8; //in km
	public double distanceThresholdMin = 0.5; //in km
	
	public ArrayList<LocationArea> list = new ArrayList<LocationArea>();
	
	
}
