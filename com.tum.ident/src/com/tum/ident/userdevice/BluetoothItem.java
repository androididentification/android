package com.tum.ident.userdevice;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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

	public String getMACValue() {
		return "x'" + MAC.toUpperCase(Locale.ENGLISH).replaceAll(":", "") + "'";
	}

	public String getName() {
		return name;
	}

	public BluetoothItem(String MAC, String name) {
		this.MAC = MAC;
		this.name = name;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
