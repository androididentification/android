package com.tum.ident.locations;

import java.io.Serializable;
import java.util.ArrayList;

import android.location.Location;
import android.util.Log;

public class LocationAreaList implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final String TAG = "LocationData";
	private double distanceThresholdMax = 8; // in km
	private double distanceThresholdMin = 0.5; // in km

	private ArrayList<LocationArea> list = new ArrayList<LocationArea>();

	public String getLocationString() {
		String result = "";
		long counter = 1;
		for (LocationArea a : list) {
			result = result + "Area (" + counter + ")\n";
			result = result + "Center: (" + a.getCenter().getLatitude() + ","
					+ a.getCenter().getLongitude() + ") radius: " + a.getRadius() + "\n";

			for (LocationItem l : a.getLocations()) {

				result = result + l.toString() + "\n"; // "Location: ("+l.getLatitude()+","+l.longitude+") weight: "+l.counter+"\n";
			}
			result = result + "\n";
			counter++;
		}

		return result;

	}

	public long size(){
		if(list!=null){
			return list.size();
		}
		else{
			return 0;
		}
	}
	
	
	public boolean addLocation(Location location) {

		Log.v(TAG,
				"add location: " + location.getLatitude() + " "
						+ location.getLongitude());

		LocationItem locationItem = new LocationItem();
		locationItem.setLatitude(location.getLatitude());
		locationItem.setLongitude(location.getLongitude());
		locationItem.updateCounter();

		boolean areaFound = false;
		for (LocationArea a : list) {
			double distance = locationItem.distanceTo(a.getCenter());
			if (distance <= (distanceThresholdMax + a.getRadius()) * 1000) {
				for (LocationItem l : a.getLocations()) {
					distance = locationItem.distanceTo(l);
					if (distance <= distanceThresholdMax * 1000) {
						if (distance > distanceThresholdMin) {
							a.getLocations().add(locationItem);
							a.getCenter().updateCounter();
							a.updateArea();
						} else {
							l.updateCounter();
						}
						areaFound = true;
						break;
					}
				}
			}
		}
		if (areaFound == false) {
			LocationArea newArea = new LocationArea();
			newArea.getCenter().updateCounter();
			newArea.getLocations().add(locationItem);
			newArea.updateArea();
			list.add(newArea);

		}
		return areaFound;

	}
}
