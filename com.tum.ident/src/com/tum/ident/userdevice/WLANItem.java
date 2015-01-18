package com.tum.ident.userdevice;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class WLANItem implements Serializable {
	
	
	public enum WLANType {
		Device, Configured, Connected, Scanned
	}
	private static final long serialVersionUID = 1L;
	private long id = 0;
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

	public String getBSSIDValue() {
		return "x'" + BSSID.toUpperCase(Locale.ENGLISH).replaceAll(":", "")
				+ "'";
	}

	public WLANItem(String SSID, String BSSID, WLANType type) {
		this.SSID = SSID;
		this.BSSID = BSSID;
		this.type = type;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE).replaceAll("BSSID=,", "");
	}

}
