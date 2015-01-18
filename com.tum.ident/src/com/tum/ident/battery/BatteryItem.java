package com.tum.ident.battery;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import android.os.BatteryManager;


public class BatteryItem implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private double level;
	private double levelChange;
	private double pluggedAC;
	private double pluggedUSB;
	private double pluggedWireless;
	private double present;
	private double charging;
	private double temperature;
	
	private int levelWeight = 0;
	private int chargingWeight = 0;
	private int presentWeight = 0;
	private int pluggedWeight = 0;

	public BatteryItem() {
		levelWeight = 0;
		chargingWeight = 0;
		presentWeight = 0;
	}

	public void update(int level, int levelChange, int temperature,
			int plugged, boolean present, boolean charging) {
		
		if(plugged != 0){
			if(plugged ==  BatteryManager.BATTERY_PLUGGED_AC ){
				this.pluggedAC = (this.pluggedAC * pluggedWeight +1.0)
						/ (pluggedWeight + 1.0);
			}
			else{
				this.pluggedAC = (this.pluggedAC * pluggedWeight)
						/ (pluggedWeight + 1.0);
			}
			if(plugged ==  BatteryManager.BATTERY_PLUGGED_USB ){
				this.pluggedUSB = (this.pluggedUSB * pluggedWeight +1.0)
						/ (pluggedWeight + 1.0);
			}
			else{
				this.pluggedUSB = (this.pluggedUSB * pluggedWeight)
						/ (pluggedWeight + 1.0);
			}
			if(plugged ==  BatteryManager.BATTERY_PLUGGED_WIRELESS ){
				this.pluggedWireless = (this.pluggedWireless * pluggedWeight +1.0)
						/ (pluggedWeight + 1.0);
			}
			else {
				this.pluggedWireless = (this.pluggedWireless * pluggedWeight)
						/ (pluggedWeight + 1.0);
			}
			pluggedWeight++;
		}
		
		if (present == false) {
			this.present = (this.present * presentWeight)
					/ (presentWeight + 1.0);
		} else {
			this.present = (this.present * presentWeight + 1.0)
					/ (presentWeight + 1.0);
			this.temperature = (this.temperature * chargingWeight + temperature)
					/ (chargingWeight + 1.0);
			if (charging) {
				this.charging = (this.charging * chargingWeight + 1.0)
						/ (chargingWeight + 1.0);
			} else {
				this.charging = (this.charging * chargingWeight)
						/ (chargingWeight + 1.0);
				this.level = (this.level * levelWeight + levelWeight)
						/ (levelWeight + 1.0);
				this.levelChange = (this.levelChange * levelWeight + levelChange)
						/ (levelWeight + 1.0);
				levelWeight++;
			}
			chargingWeight++;
		}
		presentWeight++;
	}
	
	
	

	public double getLevel() {
		return level;
	}

	public double getLevelChange() {
		return levelChange;
	}

	public double getPluggedAC() {
		return pluggedAC;
	}

	public double getPluggedUSB() {
		return pluggedUSB;
	}

	public double getPluggedWireless() {
		return pluggedWireless;
	}

	public double getPresent() {
		return present;
	}

	public double getCharging() {
		return charging;
	}

	public double getTemperature() {
		return temperature;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
