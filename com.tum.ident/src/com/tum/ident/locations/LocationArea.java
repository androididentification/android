package com.tum.ident.locations;

import java.io.Serializable;
import java.util.ArrayList;

public class LocationArea implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	private static final String TAG = "SensorData";
	
	private long radius;
	private LocationItem center;
	private ArrayList<LocationItem> locations;
	
	

	public long getRadius() {
		return radius;
	}

	public void setRadius(long radius) {
		this.radius = radius;
	}

	public LocationItem getCenter() {
		return center;
	}

	public void setCenter(LocationItem center) {
		this.center = center;
	}

	public ArrayList<LocationItem> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<LocationItem> locations) {
		this.locations = locations;
	}

	public LocationArea() {
		center = new LocationItem();
		locations = new ArrayList<LocationItem>();
	}

	public void updateArea() {
		if (center == null) {
			center = new LocationItem();
		}
		if (locations == null) {
			locations = new ArrayList<LocationItem>();
		}
		if (locations.size() > 0) {
			double avgLat = 0;
			double avgLong = 0;
			for (LocationItem l : locations) {
				avgLat = avgLat + l.getLatitude();
				avgLong = avgLong + l.getLongitude();
			}
			avgLat = avgLat / locations.size();
			avgLong = avgLong / locations.size();
			center.setLatitude( avgLat);
			center.setLongitude(avgLong);
			radius = 80; // todo
		}

	}

	public static double compareAreas(ArrayList<LocationArea> al1,
			ArrayList<LocationArea> al2) {
		double avgDistances = 0;

		for (LocationArea a1 : al1) {
			for (LocationArea a2 : al2) {
				double distance = a1.center.distanceTo(a2.center);

				double avgDistance = 0;

				if (distance <= a1.radius + a2.radius) {
					if (a1.locations.size() > 0 || a2.locations.size() > 0) {
						long num = a1.locations.size() * a2.locations.size();
						for (LocationItem l1 : a1.locations) {
							for (LocationItem l2 : a2.locations) {
								distance = l1.distanceTo(l2);
								avgDistance = avgDistance + distance;
							}
							avgDistance = avgDistance / num;
						}

					}
					avgDistances = avgDistances + avgDistance;
				}
			}
		}
		return avgDistances;
	}

}
