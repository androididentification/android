package com.tum.ident.camera;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

import android.graphics.Bitmap;

public class DarkFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	transient private Bitmap image = null;
	private byte[] data;
	private int index;
	private int width = 400;
	private int height = 400;
	private long weight = 0;
	private double red = 0;
	private double green = 0;
	private double blue = 0;
	
	public Bitmap getImage() {
		return image;
	}
	public void setImage(Bitmap image) {
		this.image = image;
	}
	public byte[] getData() {
		return data;
	}
	
	public void createData(){
		if(image!=null){
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			image.compress(Bitmap.CompressFormat.JPEG, 100,
					bytes);
			this.data = bytes.toByteArray();
			try {
				bytes.close();
			} catch (IOException e) {
			
			}
		}
	}
	

	public void setData(byte[] data) {
		this.data = data;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
	public long getWeight() {
		return weight;
	}
	public void increaseWeight() {
		this.weight++;
	}
	
	public void setWeight(long weight) {
		this.weight = weight;
	}
	public double getRed() {
		return red;
	}
	public void setRed(double red) {
		this.red = red;
	}
	public double getGreen() {
		return green;
	}
	public void setGreen(double green) {
		this.green = green;
	}
	public double getBlue() {
		return blue;
	}
	public void setBlue(double blue) {
		this.blue = blue;
	}
	
	
	public void addRed(double c){
		red = ((red * weight) + c)/ (weight + 1);
	}
	
	public void addGreen(double c){
		green = ((green * weight) + c )/ (weight + 1);
	}
	public void addBlue(double c){
		blue = ((blue * weight) + c)/ (weight + 1);
	}
	

	
}
