package com.tum.ident.identification;

import java.io.Serializable;

import com.tum.ident.userdevice.UserDevice;

public class IdentificationItem implements Serializable {
	private static final long serialVersionUID = 1L;
	public String method = "";
	public UserDevice userDevice = null;
	public String userID = "";
	public String deviceID = "";
}
