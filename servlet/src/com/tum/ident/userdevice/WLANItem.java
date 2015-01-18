package com.tum.ident.userdevice;

import java.io.Serializable;

public class WLANItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private long id = 0;

	public enum WLANType {
	    Device,
	    Configured,
	    Connected,
	    Scanned
	}
	
	private String SSID;
	private String BSSID;
	private WLANType type = null;
	
	
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
	public WLANType getType() {
		return type;
	}
	public String getBSSID() {
		return BSSID;
	}

	public void setBSSID(String bSSID) {
		BSSID = bSSID;
	}

	public String getSSID() {
		return SSID;
	}
	

	public WLANItem(String SSID, String BSSID,WLANType type) {
		this.SSID = SSID;
		this.BSSID = BSSID;
		this.type = type;

		
	}
	
	
}
