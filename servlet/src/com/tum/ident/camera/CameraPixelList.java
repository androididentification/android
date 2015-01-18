package com.tum.ident.camera;

import java.io.Serializable;
import java.util.ArrayList;

public class CameraPixelList implements Serializable {
	private static final long serialVersionUID = 1L;
	public int index = 0;
	ArrayList<CameraPixel> list     = new ArrayList<CameraPixel>();
}
