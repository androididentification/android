package com.tum.ident.camera;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class DarkFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	transient public BufferedImage image = null;
	public byte[] data;
	public int index;
	public int width = 400;
	public int height = 400;
	public long weight = 0;
	public double red = 0;
	public double green = 0;
	public double blue = 0;
	
}
