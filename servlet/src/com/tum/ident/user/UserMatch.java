package com.tum.ident.user;

import java.io.Serializable;

public class UserMatch implements Serializable {
	private static final long serialVersionUID = 1L;
	transient private long id;
	
	private String gid;
	
	private double bluetoothMatch = 0;
	private double bluetoothSignificance = 0;
	
	private double wlanMatch = 0;
	private double wlanSignificance = 0;
	
	private double packageMatch = 0;
	private double packageSignificance = 0;
	
	private double contactMatch = 0;
	private double contactSignificance = 0;
	
	private double accountMatch = 0;
	private double accountSignificance = 0;
	
	private double callLogMatch = 0;
	private double callLogSignificance = 0;
	
	private double userNameMatch;

	private double match;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}


	public double getMatch() {
		return match;
	}
	
	public void setMatch(double match) {
		this.match = match;
	}
	
	public void calculateMatch() {
		double absolutWeight = 7;
		match = bluetoothMatch;
		match = match +wlanMatch;
		match = match +contactMatch;
		match = match +packageMatch;
		match = match  +accountMatch;
		match = match  +callLogMatch;
		match = match +userNameMatch;
		match = match/absolutWeight;
	}
	
	
	public double getBluetoothMatch() {
		return bluetoothMatch;
	}

	public void setBluetoothMatch(double bluetoothMatch) {
		this.bluetoothMatch = bluetoothMatch;
	}

	public double getBluetoothSignificance() {
		return bluetoothSignificance;
	}

	public void setBluetoothSignificance(double bluetoothSignificance) {
		this.bluetoothSignificance = bluetoothSignificance;
	}

	public double getWlanMatch() {
		return wlanMatch;
	}

	public void setWlanMatch(double wlanMatch) {
		this.wlanMatch = wlanMatch;
	}

	public double getWlanSignificance() {
		return wlanSignificance;
	}

	public void setWlanSignificance(double wlanSignificance) {
		this.wlanSignificance = wlanSignificance;
	}

	public double getPackageMatch() {
		return packageMatch;
	}

	public void setPackageMatch(double packageMatch) {
		this.packageMatch = packageMatch;
	}

	public double getPackageSignificance() {
		return packageSignificance;
	}

	public void setPackageSignificance(double packageSignificance) {
		this.packageSignificance = packageSignificance;
	}

	public double getContactMatch() {
		return contactMatch;
	}

	public void setContactMatch(double contactMatch) {
		this.contactMatch = contactMatch;
	}

	public double getContactSignificance() {
		return contactSignificance;
	}

	public void setContactSignificance(double contactSignificance) {
		this.contactSignificance = contactSignificance;
	}

	public double getAccountMatch() {
		return accountMatch;
	}

	public void setAccountMatch(double accountMatch) {
		this.accountMatch = accountMatch;
	}

	public double getAccountSignificance() {
		return accountSignificance;
	}

	public void setAccountSignificance(double accountSignificance) {
		this.accountSignificance = accountSignificance;
	}

	public double getCallLogMatch() {
		return callLogMatch;
	}

	public void setCallLogMatch(double callLogMatch) {
		this.callLogMatch = callLogMatch;
	}

	public double getCallLogSignificance() {
		return callLogSignificance;
	}

	public void setCallLogSignificance(double callLogSignificance) {
		this.callLogSignificance = callLogSignificance;
	}

	public double getUserNameMatch() {
		return userNameMatch;
	}

	public void setUserNameMatch(double userNameMatch) {
		this.userNameMatch = userNameMatch;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}
	
}
