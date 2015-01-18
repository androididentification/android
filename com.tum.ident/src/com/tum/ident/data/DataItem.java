package com.tum.ident.data;

import java.io.Serializable;

import android.util.Log;

import com.tum.ident.battery.BatteryItemList;
import com.tum.ident.camera.CameraPixelList;
import com.tum.ident.camera.DarkFrame;
import com.tum.ident.data.DataController.DataType;
import com.tum.ident.files.FileItemList;
import com.tum.ident.gaitrecognition.StepStatistics;
import com.tum.ident.identification.IdentificationItem;
import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.orientation.OrientationItem;
import com.tum.ident.spectrum.SpectrumItemList;

public class DataItem implements Serializable {
	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	transient private final static String TAG = "DataItem";
	transient private static String lastError = "";

	private byte[] data;
	private DataType type;
	private String parameter;


	public byte[] getData() {
		return data;
	}

	public DataType getType() {
		return type;
	}
	
	public int getTypeIndex() {
		return type.ordinal();
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public void clear(){
		data = null;
		parameter = "";
	}

	public static String getLastError() {
		return lastError;
	}
	
	
	public static DataType getDataType(Object obj) {
		if (obj instanceof BatteryItemList) {
			return DataType.Battery;
		} else if (obj instanceof SpectrumItemList) {
			return DataType.Spectrum;
		} else if (obj instanceof OrientationItem) {
			return DataType.Orientation;
		} else if (obj instanceof FileItemList) {
			return DataType.File;
		} else if (obj instanceof MusicItemList) {
			return DataType.Music;
		} else if (obj instanceof IdentificationItem) {
			return DataType.UserDevice;
		} else if (obj instanceof StepStatistics) {
			return DataType.StepDetection;
		} else if (obj instanceof LocationAreaList) {
			return DataType.Location;
		} else if (obj instanceof CameraPixelList) {
			CameraPixelList cameraPixels = (CameraPixelList) obj;
			if (cameraPixels.getIndex() == 0) {
				return DataType.PixelErrorBack;
			} else {
				return DataType.PixelErrorFront;
			}
		} else if (obj instanceof DarkFrame) {
			DarkFrame darkFrame = (DarkFrame) obj;
			if (darkFrame.getIndex() == 0) {
				return DataType.DarkFrameBack;
			} else {
				return DataType.DarkFrameFront;
			}

		} else {
			Log.v("DEBUG", "CLASS NOT FOUND: " + obj);
		}
		return null;
	}

	public DataItem(String parameter, byte[] data, DataType type) {
		this.parameter = parameter;
		this.data = data;
		this.type = type;
	}

	/*
	 * public DataItem(String parameter,Object dataObject,DataType type) { byte
	 * data[]= getData(dataObject); this.parameter = parameter; this.data =
	 * data; this.type = type; }
	 */

	public DataItem(String parameter, Object dataObject) {
		this.parameter = parameter;
		this.type = getDataType(dataObject);
		Log.v("DEBUG", "getData: " + this.type);
		this.data = DataFactory.getData(dataObject);

	}


}
