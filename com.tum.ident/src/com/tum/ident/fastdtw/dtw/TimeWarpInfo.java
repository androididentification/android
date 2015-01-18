/*
 * TimeWarpInfo.java   Jul 14, 2004
 *
 * Copyright (c) 2004 Stan Salvador
 * stansalvador@hotmail.com
 */

package com.tum.ident.fastdtw.dtw;

public class TimeWarpInfo {
	// PRIVATE DATA
	private final double distance;
	private final WarpPath path;

	// CONSTRUCTOR
	TimeWarpInfo(double dist, WarpPath wp) {
		distance = dist;
		path = wp;
	}

	public double getDistance() {
		return distance;
	}

	public WarpPath getPath() {
		return path;
	}

	@Override
	public String toString() {
		return "(Warp Distance=" + distance + ", Warp Path=" + path + ")";
	}

} // end class TimeWarpInfo
