package com.tum.ident.user;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CallLogItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String phoneNumber;
	private String callType;
	private String callDate;
	private String callDuration;

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

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public String getCallType() {
		return callType;
	}

	public String getCallDate() {
		return callDate;
	}

	public String getCallDuration() {
		return callDuration;
	}

	public CallLogItem(String phoneNumber, String callType, String callDate,
			String callDuration) {
		this.phoneNumber = phoneNumber;
		this.callType = callType;
		this.callDate = callDate;
		this.callDuration = callDuration;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
