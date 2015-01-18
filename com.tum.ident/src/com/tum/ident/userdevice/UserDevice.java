package com.tum.ident.userdevice;

import java.io.Serializable;

import android.content.Context;

import com.tum.ident.bluetooth.BluetoothData;
import com.tum.ident.device.Device;
import com.tum.ident.user.User;
import com.tum.ident.wlan.WLANData;

public class UserDevice implements Serializable {
	private static final long serialVersionUID = 1L;
	private User user = new User();
	private Device device = new Device();

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

	public User getUser() {
		return user;
	}

	public Device getDevice() {
		return device;
	}

	public void collectData(Context context) {

		BluetoothData.init();
		WLANData.init(context);

		user.collectData(context);
		device.collectData(context);

		BluetoothData.finish();
		WLANData.finish();
	}
}
