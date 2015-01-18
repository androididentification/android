package com.tum.ident.result;

import java.io.Serializable;

public class ResultIDItem implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		Insert, Update, Error
	}

	private  Type type;
	private  String deviceID;
	private  String userID;

	public ResultIDItem(Type type, String deviceID, String userID) {
		this.type = type;
		this.deviceID = deviceID;
		this.userID = userID;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getDeviceID() {
		return deviceID;
	}

	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	
	
}