package com.tum.ident.device;

import java.io.Serializable;

public class DeviceMatch implements Serializable {
	private static final long serialVersionUID = 1L;
	private long id;
	private String gid;

	private double match = 0;
	private double[] properties = null;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setMatch(double match) {
		this.match = Math.max(match, this.match);
	}

	public double getMatch() {
		return match;
	}

	public double[] getProperties() {
		return properties;
	}

	public void setProperties(double[] properties) {
		this.properties = properties;
	}

	public String getGid() {
		return gid;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

}
