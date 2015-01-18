package com.tum.ident.device;

import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;

import com.tum.ident.apps.PackageData;
import com.tum.ident.bluetooth.BluetoothData;
import com.tum.ident.system.SystemData;
import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.userdevice.PackageItem;
import com.tum.ident.userdevice.WLANItem;
import com.tum.ident.util.Util;
import com.tum.ident.wlan.WLANData;

public class Device implements Serializable {
	private static final long serialVersionUID = 1L;

	private String gid;
	private long id = 0;

	private ArrayList<DeviceIDItem> deviceIDList = new ArrayList<DeviceIDItem>();
	private ArrayList<BluetoothItem> bluetoothList = new ArrayList<BluetoothItem>();
	private ArrayList<WLANItem> wlanList = new ArrayList<WLANItem>();

	private ArrayList<PackageItem> packageList = new ArrayList<PackageItem>();

	private String manufacturer;
	private String model;
	private String cpuInfo;
	private String internalStorageSize;
	private String totalMem;

	public void setId(long id) {
		this.id = id;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getGid() {
		return gid;
	}

	public long getId() {
		return id;
	}

	public String getInternalStorageSize() {
		return internalStorageSize;
	}

	public ArrayList<DeviceIDItem> getDeviceIDList() {
		return deviceIDList;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public String getModel() {
		return model;
	}

	public String getCpuInfo() {
		return cpuInfo;
	}

	public String getTotalMem() {
		return totalMem;
	}

	public ArrayList<BluetoothItem> getBluetooth() {
		return bluetoothList;
	}

	public ArrayList<WLANItem> getWLAN() {
		return wlanList;
	}

	public ArrayList<PackageItem> getPackageList() {
		return packageList;
	}

	public void collectData(Context context) {

		deviceIDList.clear();
		deviceIDList.add(SystemData.getDeviceIDs(context));

		bluetoothList.clear();
		bluetoothList.add(BluetoothData.getBluetooth());

		wlanList.clear();
		wlanList.add(WLANData.getWLAN(context));

		manufacturer = SystemData.getManufacturer();
		model = SystemData.getModel();
		cpuInfo = SystemData.getCPUInfo();
		internalStorageSize = SystemData.getInternalStorageSize();
		totalMem = SystemData.getTotalMemory();

		packageList = PackageData.getPackageData(context, true);

	}

	public String getWLANString() {
		return Util.toStringFilterNewLine(wlanList);
	}

	public String getBluetoothString() {
		return Util.toStringFilterNewLine(bluetoothList);
	}

}
