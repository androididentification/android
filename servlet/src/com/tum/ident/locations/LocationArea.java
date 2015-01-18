package com.tum.ident.locations;

import java.io.Serializable;
import java.util.ArrayList;


public class LocationArea implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private static final String TAG = "SensorData";
	LocationItem center;
	long radius;
	ArrayList<LocationItem> locations;
	
	public LocationArea() {
		center = new LocationItem();
		locations = new ArrayList<LocationItem>();
	}

	public void updateArea(){
		if(center==null){
			center = new LocationItem();
		}
		if(locations==null){
			 locations = new ArrayList<LocationItem>();
		}
		if(locations.size() > 0){
			double avgLat = 0;
			double avgLong = 0;
			for (LocationItem l : locations) {
				if(l!=null){
					avgLat = avgLat+l.latitude;
					avgLong= avgLong+l.longitude;
				}
			}
			avgLat = avgLat/locations.size();
			avgLong= avgLong/locations.size();
			center.latitude = avgLat;
			center.longitude = avgLong;
			radius = 80; //todo
		}

	}

	public static double compareAreas( ArrayList<LocationArea> al1, ArrayList<LocationArea> al2){
		double avgDistances = 0;

		for (LocationArea a1 : al1) {
			for (LocationArea a2 : al2) {
				double distance = a1.center.distanceTo(a2.center);

				double avgDistance = 0;

				if(distance <= a1.radius+a2.radius)
				{
					if(a1.locations.size()>0 || a2.locations.size()>0 ){
						long num = a1.locations.size()*a2.locations.size();
						for (LocationItem l1 : a1.locations) {
							for (LocationItem l2 : a2.locations) {
								distance = l1.distanceTo(l2);
								avgDistance = avgDistance+distance;
							}
							avgDistance = avgDistance/num;
						}

					}
					avgDistances = avgDistances+avgDistance;
				}
			}
		}
		return avgDistances;
	}

}
