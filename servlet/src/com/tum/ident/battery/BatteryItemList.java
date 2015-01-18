package com.tum.ident.battery;

import java.io.Serializable;
import java.util.Calendar;


public class BatteryItemList implements Serializable {
	private static final long serialVersionUID = 1L;

	BatteryItem[][] items = new BatteryItem[7][48];
	
	private int currentDay;
	private int currentTimeSlot;
	boolean batteryInfoSet = false;
	int level;
    int plugged;
    boolean present;
    boolean charging;
    int temperature;

}
