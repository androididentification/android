package com.tum.ident.result;

import java.io.Serializable;
import java.util.ArrayList;

import com.tum.ident.device.DeviceMatch;
import com.tum.ident.user.UserMatch;

public class ResultMatchItem implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		OK, Error
	}

	private Type type;
	private ArrayList<UserMatch> userMatchList;
	private ArrayList<DeviceMatch> deviceMatchList;

	public ResultMatchItem(Type type, ArrayList<DeviceMatch> deviceMatchList, ArrayList<UserMatch> userMatchList) {
		this.type = type;
		this.userMatchList = userMatchList;
		this.deviceMatchList = deviceMatchList;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public ArrayList<UserMatch> getUserMatchList() {
		return userMatchList;
	}

	public void setUserMatchList(ArrayList<UserMatch> userMatchList) {
		this.userMatchList = userMatchList;
	}

	public ArrayList<DeviceMatch> getDeviceMatchList() {
		return deviceMatchList;
	}

	public void setDeviceMatchList(ArrayList<DeviceMatch> deviceMatchList) {
		this.deviceMatchList = deviceMatchList;
	}
	
	

}
