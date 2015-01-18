package com.tum.ident.camera;

import java.io.Serializable;
import java.util.ArrayList;

public class CameraPixelList implements Serializable {
	private static final long serialVersionUID = 1L;
	private int index = 0;
	private ArrayList<CameraPixel> list = new ArrayList<CameraPixel>();
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public ArrayList<CameraPixel> getList() {
		return list;
	}
	public void setList(ArrayList<CameraPixel> list) {
		this.list = list;
	}
	
	
}
