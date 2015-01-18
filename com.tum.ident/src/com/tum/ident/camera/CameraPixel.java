package com.tum.ident.camera;

import java.io.Serializable;

public class CameraPixel implements Serializable {
	private static final long serialVersionUID = 1L;

	enum Type {
		HotPixel, DeadPixel
	}

	private int x;
	private int y;
	private int color;
	private Type type;
	private int counter = 0;

	public CameraPixel(int x, int y, int color) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.counter = 0;
	}
	
	public void incrementCounter(){
		counter++;
	}
	
	
	public void setType(Type type) {
		this.type = type;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getColor() {
		return color;
	}

	public Type getType() {
		return type;
	}

	public int getCounter() {
		return counter;
	}

}
