package com.tum.ident.locations;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import com.tum.ident.Util;

public class LocationItem  implements Serializable {
	private static final long serialVersionUID = 1L;
	public double latitude;
	public double longitude;
	
	public long[][] timecounter = new long[7][24];
	public long counter;
    
    public double distanceTo(LocationItem locationItem) {
    	double lat2 = locationItem.latitude;
    	double lon2 = locationItem.longitude;
    	double theta = longitude - lon2;
    	double dist = Math.sin(Util.deg2rad(latitude)) * Math.sin(Util.deg2rad(lat2)) + Math.cos(Util.deg2rad(latitude)) * Math.cos(Util.deg2rad(lat2)) * Math.cos(Util.deg2rad(theta));
    	dist = Math.acos(dist);
    	dist = Util.rad2deg(dist);
    	dist = dist * 60 * 1.1515;
    	dist = dist * 1.609344;
    	return dist;
    }
    public void updateCounter(){
    	Calendar calendar = Calendar.getInstance();
    	int time = calendar.get(Calendar.HOUR_OF_DAY);
    	int day  = calendar.get(Calendar.DAY_OF_WEEK)-1;
    	timecounter[day][time] =  timecounter[day][time]+1;
    	counter++;
    }


    
}
