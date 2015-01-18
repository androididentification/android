package com.tum.ident.device;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.userdevice.PackageItem;
import com.tum.ident.userdevice.WLANItem;
import com.tum.servlets.DataFactory;
import com.tum.servlets.DataService;


public class Device implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String gid;
	private long id = 0;
	
	private ArrayList<DeviceIDItem> deviceIDList =  new ArrayList<DeviceIDItem>();
	private ArrayList<BluetoothItem> bluetoothList = new ArrayList<BluetoothItem>();
	private ArrayList<WLANItem> wlanList      = new ArrayList<WLANItem>();
	private ArrayList<PackageItem>   packageList     = new ArrayList<PackageItem>();
	
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

	public  ArrayList<DeviceIDItem>  getDeviceIDList(){
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
	
	public void prepareData(boolean generateGID){
		if(generateGID){
			DigestSHA3 md = new DigestSHA3(512);
			byte[] data = DataFactory.createObjectData(this); //todo
			md.update(data);
			byte[] digest = md.digest();
			gid = org.bouncycastle.util.encoders.Hex.toHexString(digest).substring(0,DataService.getHashLength());
		}
		id  = 0;
	}
	
	
	/*
	public void setDeviceData(HttpServletRequest request,boolean generateGID){
		if(generateGID){
			DigestSHA3 md = new DigestSHA3(224);
			String body = request.getParameterMap().toString()+"DEVICEDATA"; //todo
			try {
				md.update(body.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			byte[] digest = md.digest();
			gid = org.bouncycastle.util.encoders.Hex.toHexString(digest).substring(0,32);
		}
		id  = 0;
		String gsfAndroidID  = request.getParameter("gsfAndroidID");
		String imeiString = request.getParameter("imeiString"); 
		String serialNum = request.getParameter("serialNum"); 
		String androidID = request.getParameter("androidID");
		
		deviceIDList.add(new DeviceIDItem(gsfAndroidID,imeiString,serialNum,androidID));
		manufacturer = request.getParameter("manufacturer");
		model = request.getParameter("model");
		
		cpuInfo = request.getParameter("cpuInfo");

		
		totalMem = request.getParameter("availableMem");
		internalStorageSize = request.getParameter("internalStorageSize");

		String ownWLANMAC = request.getParameter("deviceWLAN");
		if(ownWLANMAC!=null){
			wlanList.add(new WLANItem("",ownWLANMAC,WLANItem.WLANType.Device));
		}
		String bluetoothMAC = request.getParameter("ownBluetoothMAC");
		String bluetoothName = request.getParameter("ownBluetoothName");
		System.out.println("ownBluetoothMAC: "+bluetoothMAC);
		System.out.println("ownBluetoothName: "+bluetoothName);
		if(bluetoothMAC!=null && bluetoothName != null){
			bluetoothList.add(new BluetoothItem(bluetoothMAC,bluetoothName)); //todo
		}
		
	}
	*/
}
