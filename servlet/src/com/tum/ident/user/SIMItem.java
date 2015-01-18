package com.tum.ident.user;

import java.io.Serializable;

public class SIMItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String imsiString ; 
	private String simID;
	private String phoneNumber;
	private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getImsiString() {
		return imsiString;
	}

	public void setImsiString(String imsiString) {
		this.imsiString = imsiString;
	}

	public String getSimID() {
		return simID;
	}

	public void setSimID(String simID) {
		this.simID = simID;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public SIMItem(String imsiString, String simID,String phoneNumber) {
		this.imsiString = imsiString; 
		this.simID  = simID;
		this.phoneNumber = phoneNumber;
	}
	
}
