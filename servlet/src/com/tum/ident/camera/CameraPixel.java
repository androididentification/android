package com.tum.ident.camera;

import java.io.Serializable;

public class CameraPixel implements Serializable {
	private static final long serialVersionUID = 1L;

	enum Type{
		HotPixel,
		DeadPixel
	}
	
	public int x;
	public int y;
	public int color;
	public Type type;
	public int counter = 0;

	
	public CameraPixel(int x,int y,int color) {
		this.x=x;
		this.y=y;
		this.color = color;
		this.counter = 0;
	}
	
	
}
