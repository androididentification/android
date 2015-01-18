package com.tum.servlets.identification;

import com.tum.servlets.DataService;


public class Match {
	private long id = 0;
	private double match = 0;
	private double significance = 0;
	double[] properties = null;
	DataService.DataType type;
	
	public double[] getProperties() {
		return properties;
	}

	public void setProperties(double[] properties) {
		this.properties = properties;
	}

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

	public double getSignificance() {
		return significance;
	}

	public void setSignificance(double significance) {
		this.significance = significance;
	}

	public DataService.DataType getType() {
		return type;
	}

	public void setType(DataService.DataType type) {
		this.type = type;
	}

	public Match(long id, double match,double significance,DataService.DataType type) {
		this.id = id;
		this.match = match;
		this.significance = significance;
		this.type = type;
	}
}
