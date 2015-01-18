package com.tum.ident.identification;

import java.io.Serializable;

import com.tum.ident.userdevice.UserDevice;

public class IdentificationItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private  String method = "";
	private  UserDevice userDevice = null;
	private  String userID = "";
	private  String deviceID = "";
	
	public IdentificationItem(){
		userDevice = new UserDevice();
	}
	public IdentificationItem(String deviceID,String userID){
		this.method = "compare";
		this.deviceID = deviceID;
		this.userID = userID;
		this.userDevice = null;
	}
	
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public UserDevice getUserDevice() {
		return userDevice;
	}
	public void setUserDevice(UserDevice userDevice) {
		this.userDevice = userDevice;
	}
	public String getUserID() {
		return userID;
	}
	public void setUserID(String userID) {
		this.userID = userID;
	}
	public String getDeviceID() {
		return deviceID;
	}
	public void setDeviceID(String deviceID) {
		this.deviceID = deviceID;
	}
	
	
}
