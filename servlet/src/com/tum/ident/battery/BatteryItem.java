package com.tum.ident.battery;

import java.io.Serializable;


public class BatteryItem implements Serializable {
	private static final long serialVersionUID = 1L;
	double level;
	double levelChange;
	double plugged;
	double present;
	double charging;
	double temperature;
	
	int levelWeight = 0;
	int chargingWeight = 0;
	int PresentWeight = 0;

	public BatteryItem() {
	    levelWeight = 0;
		chargingWeight = 0;
		PresentWeight = 0;
	} 


}
