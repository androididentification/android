package com.tum.ident.orientation;

import android.graphics.Bitmap;
import android.util.Log;

import com.tum.ident.data.DataController;
import com.tum.ident.data.DataItem;
import com.tum.ident.realtime.RealtimeData;
import com.tum.ident.sensors.SensorData;
import com.tum.ident.storage.StorageHandler;

public class OrientationData {

	private static final String TAG = "OrientationData";

	private OrientationItem item = new OrientationItem();
	private  long startTime = 0;
	private  long waitTime = 4000;
	private SensorData sensorData = null;

	private float[] lastOrientation = null;
	private boolean lastOrientationSet = false;
	private boolean listening = false;
	private float movementThreshold = 0.1f;
	private float movementThresholdX = 0.9f;
	private DataController dataController;

	private Bitmap bmp = null;

	public OrientationData(DataController dataController) {
		load();
		
		
		this.dataController = dataController;
	}

	public void setSensorData(SensorData sensorData) {
		this.sensorData = sensorData;
	}

	public DataItem getDataItem() {
		return new DataItem("", item);
	}

	public void startListening() {
		startTime = System.currentTimeMillis();
		lastOrientation = new float[3];
		lastOrientationSet = false;
		listening = true;
	}

	public static float distance(float a, float b) {
		float distance = Math.abs(a - b);
		if (distance > Math.PI) {
			distance = (float) Math.abs(Math.PI * 2 - distance);
		}
		return distance;
	}

	public Bitmap getOrientationImage() {
		bmp = item.getImage(bmp);
		return bmp;
	}

	public void onSensorChanged(float[] mOrientation) {
		if (listening) {
			if (System.currentTimeMillis() - startTime < waitTime) {
				if (lastOrientationSet == false) {
					lastOrientationSet = true;
				} else {
					Log.v(TAG, "o: " + (mOrientation[0]) + " ,"
							+ (mOrientation[1]) + " , " + (mOrientation[2]));
					Log.v(TAG, "l: " + (lastOrientation[0]) + " ,"
							+ (lastOrientation[1]) + " , "
							+ (lastOrientation[2]));

					Log.v(TAG, "-> " + (mOrientation[0] - lastOrientation[0])
							+ " ," + (mOrientation[1] - lastOrientation[1])
							+ " , " + (mOrientation[2] - lastOrientation[2]));
					if (distance(mOrientation[0], lastOrientation[0]) > movementThresholdX) {
						sensorData.unregisterOrientationListeners();
						Log.v(TAG, "->moved!");
						listening = false;
						lastOrientationSet = false;
					} else if (distance(mOrientation[1], lastOrientation[1]) > movementThreshold) {
						sensorData.unregisterOrientationListeners();
						Log.v(TAG, "->moved!");
						listening = false;
						lastOrientationSet = false;
					} else if (distance(mOrientation[2], lastOrientation[2]) > movementThreshold) {
						sensorData.unregisterOrientationListeners();
						Log.v(TAG, "->moved!");
						listening = false;
						lastOrientationSet = false;
					}
				}
				if (listening) {
					System.arraycopy(mOrientation, 0, lastOrientation, 0,
							lastOrientation.length);
				}

			} else {
				if (lastOrientationSet == false) {
					System.arraycopy(mOrientation, 0, lastOrientation, 0,
							lastOrientation.length);
				}

				float[] newOrientation = new float[3];

				System.arraycopy(lastOrientation, 0, newOrientation, 0,
						newOrientation.length);

				Log.v(TAG, "STORE: " + (lastOrientation[0]) + " ,"
						+ (lastOrientation[1]) + " , " + (lastOrientation[2]));

				item.add(newOrientation);

				dataController.addData("", item);
				sensorData.unregisterOrientationListeners();
				listening = false;
				lastOrientationSet = false;
				save();
				startTime = System.currentTimeMillis();
			}
		}
	}

	public void load() {

		String fileName = "orientations.ser";

		Log.v(TAG, "Load Locations:" + fileName);

		item = null;

		item = (OrientationItem) StorageHandler.loadObject(fileName);
		if (item == null) {
			item = new OrientationItem();
		}
	}

	public void save() {

		String fileName = "orientations.ser";

		Log.v(TAG, "Save Locations:" + fileName);

		StorageHandler.saveObject(item, fileName);

	}

}