package com.tum.ident.userdevice;


import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import com.tum.ident.device.Device;
import com.tum.ident.user.User;

public class UserDevice implements Serializable {
	private static final long serialVersionUID = 1L;
	public  User user = new User();
	public  Device device = new Device();

	public String getUserGid() {
		return user.getGid();
	}
	public String getDeviceGid() {
		return device.getGid();
	}
	public long getUserId() {
		return user.getId();
	}
	public long getDeviceId() {
		return device.getId();
	}
	
	public void prepareData(boolean generateGID){
		user.prepareData(generateGID);
		device.prepareData(generateGID);
	}
	
	
}
