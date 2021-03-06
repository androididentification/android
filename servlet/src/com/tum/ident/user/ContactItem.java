package com.tum.ident.user;

import java.io.Serializable;


public class ContactItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String idContact;
	private String phoneNumber;
	private long phoneNumberID = 0;
	private long userID = 0;

	public long getPhoneNumberID() {
		return phoneNumberID;
	}
	public void setPhoneNumberID(long phoneNumberID) {
		this.phoneNumberID = phoneNumberID;
	}
	public long getUserID() {
		return userID;
	}
	public void setUserID(long userID) {
		this.userID = userID;
	}
	public String getIdContact() {
		return idContact;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}

	public ContactItem(String idContact, String phoneNumber) {
		this.idContact = idContact;
		this.phoneNumber = phoneNumber; //todo: Normalize Phone Number
	}
	
	
	
}
