package com.tum.ident.locations;

import java.io.Serializable;
import java.util.Calendar;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.tum.ident.util.Util;

public class LocationItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private long[][] timecounter = new long[7][24];
	
	private long counter;
	private double latitude;
	private double longitude;
	
	
	
	
	public long getCounter() {
		return counter;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public LocationItem() {
		counter = 1;
	}

	public double distanceTo(LocationItem locationItem) {
		double lat2 = locationItem.latitude;
		double lon2 = locationItem.longitude;
		double theta = longitude - lon2;
		double dist = Math.sin(Util.deg2rad(latitude))
				* Math.sin(Util.deg2rad(lat2))
				+ Math.cos(Util.deg2rad(latitude))
				* Math.cos(Util.deg2rad(lat2)) * Math.cos(Util.deg2rad(theta));
		dist = Math.acos(dist);
		dist = Util.rad2deg(dist);
		dist = dist * 60 * 1.1515;
		dist = dist * 1.609344;
		return dist;
	}

	public void updateCounter() {
		Calendar calendar = Calendar.getInstance();
		int time = calendar.get(Calendar.HOUR_OF_DAY);
		int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		timecounter[day][time] = timecounter[day][time] + 1;
		counter++;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
