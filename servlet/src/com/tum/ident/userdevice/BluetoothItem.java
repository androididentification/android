package com.tum.ident.userdevice;

import java.io.Serializable;

public class BluetoothItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private String MAC;
	private String name;
	private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getMAC() {
		return MAC;
	}
	
	
	public String getName() {
		return name;
	}
	
	public BluetoothItem(String MAC, String name) {
		this.MAC  = MAC;
		this.name = name;
	}

}
