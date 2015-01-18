package com.tum.ident.device;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class DeviceIDItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String gsfAndroidID;
	private String imeiString;
	private String serialNum;
	private String androidID;

	public String getGsfAndroidID() {
		return gsfAndroidID;
	}

	public String getImeiString() {
		return imeiString;
	}

	public String getSerialNum() {
		return serialNum;
	}

	public String getAndroidID() {
		return androidID;
	}

	public DeviceIDItem(String gsfAndroidID, String imeiString,
			String serialNum, String androidID) {
		this.gsfAndroidID = gsfAndroidID;
		this.imeiString = imeiString;
		this.serialNum = serialNum;
		this.androidID = androidID;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
