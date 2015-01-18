package com.tum.ident.sensors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;
import android.util.Log;

import com.tum.ident.data.DataController;
import com.tum.ident.orientation.OrientationData;
import com.tum.ident.realtime.RealtimeData;
import com.tum.ident.stepdetector.StepDetector;
import com.tum.ident.util.Util;

public class SensorData implements SensorEventListener {
	private static final String TAG = "SensorData";

	private SensorManager sensorManager;
	private Sensor mAccelerometer;
	private Sensor mMagnetometer;

	private long accelerometerTimestamp = 0;

	private ArrayList<SensorItem> accelerometerData = new ArrayList<SensorItem>();
	private ArrayList<SensorItem> history = new ArrayList<SensorItem>();
	private static long historyCounter = 0;
	private static boolean recordHistory = false;
	private long historyStart = -1;
	private long historyEnd = -1;
	private long lastHistoryEventTime = -1;

	private float[] mLastAccelerometer = new float[3];
	private float[] mLastMagnetometer = new float[3];
	private boolean mLastAccelerometerSet = false;
	private boolean mLastMagnetometerSet = false;

	private float[] mR = new float[9];
	private float[] mOrientation = new float[3];

	boolean firstGravity = true;

	final static float alpha = 0.99f;
	private float[] gravity = new float[3];
	long lastSensorTime = 0;

	Context context;

	DataController dataController;
	OrientationData orientationData;
	StepDetector stepDetector = null;
	PowerManager.WakeLock lock;

	public SensorData(Context context, OrientationData orientationData,
			DataController dataController) {
		this.context = context;
		this.dataController = dataController;
		this.orientationData = orientationData;
		if (context != null) {
			sensorManager = (SensorManager) context
					.getSystemService(Context.SENSOR_SERVICE);
			mAccelerometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			mMagnetometer = sensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
			mLastAccelerometerSet = false;
			mLastMagnetometerSet = false;
		}
		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		context.registerReceiver(screenReceiver, filter);
		PowerManager pm = (PowerManager) context
				.getSystemService(Context.POWER_SERVICE);
		lock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SensorRead");
		lock.acquire();
	}

	public void release() {
		if (listeningOrientation == true || listeningSteps == true) {
			sensorManager.unregisterListener(this, mAccelerometer);
		}
		if (listeningOrientation == true) {
			sensorManager.unregisterListener(this, mMagnetometer);
		}
		lock.release();
	}

	public BroadcastReceiver screenReceiver = new BroadcastReceiver() {

		@SuppressLint("Wakelock")
		@Override
		public void onReceive(Context context, Intent intent) {
			// Check action just to be on the safe side.
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				Log.v(TAG, "trying re-registration");

				lock.release();
				lock.acquire();
				resetListeners();
			}
		}
	};

	public float[] getGravity() {
		return gravity;
	}

	public void setStepDetector(StepDetector stepDetector) {
		this.stepDetector = stepDetector;
	}

	public boolean listeningSteps = false;
	public boolean listeningOrientation = false;

	public long lastEventTime = 0;
	public long firstEventTime = 0;

	public long getLastEventTime() {
		return lastEventTime;
	}

	public long getFirstEventTime() {
		return firstEventTime;
	}

	public void resetListeners() {
		if (listeningOrientation == true || listeningSteps == true) {
			sensorManager.unregisterListener(this, mAccelerometer);
		}
		if (listeningOrientation == true) {
			sensorManager.unregisterListener(this, mMagnetometer);
		}
		if (listeningOrientation == true || listeningSteps == true) {
			sensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		if (listeningOrientation == true) {
			sensorManager.registerListener(this, mMagnetometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
	}

	public void registerOrientationListeners() {
		if (listeningOrientation == false) {
			if (listeningSteps == false) {
				sensorManager.registerListener(this, mAccelerometer,
						SensorManager.SENSOR_DELAY_FASTEST);
			}
			sensorManager.registerListener(this, mMagnetometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		mLastAccelerometerSet = false;
		mLastMagnetometerSet = false;
		listeningOrientation = true;

	}

	public void unregisterOrientationListeners() {
		if (RealtimeData.isSending() == false) {
			if (listeningOrientation == true) {
				if (listeningSteps == false) {
					sensorManager.unregisterListener(this, mAccelerometer);
				}
				sensorManager.unregisterListener(this, mMagnetometer);
			}
			listeningOrientation = false;
		}
	}

	public void registerStepListeners() {
		if (listeningSteps == false && listeningOrientation == false) {
			sensorManager.registerListener(this, mAccelerometer,
					SensorManager.SENSOR_DELAY_FASTEST);
		}
		listeningSteps = true;
		firstGravity = true;
	}

	public void unregisterStepListeners() {
		if (RealtimeData.isSending() == false) {
			if (listeningSteps == true) {
				if (listeningOrientation == false) {
					sensorManager.unregisterListener(this, mAccelerometer);
					firstEventTime = -1;
				}
			}
			listeningSteps = false;
		}
	}

	public boolean listeningSteps() {
		return listeningSteps;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public double[] getHistoryN(long startTime, long endTime, boolean gravity) {
		if (historyStart != -1) {
			SensorItem[] puffer = getHistoryEvents(startTime, endTime);
			double[] acceleration = null;
			if (puffer != null) {
				acceleration = new double[puffer.length];
				float x;
				float y;
				float z;
				if (gravity) {
					for (int i = 0; i < puffer.length; i++) {
						x = puffer[i].values[0];
						y = puffer[i].values[1];
						z = puffer[i].values[2];
						acceleration[i] = Math.sqrt(x * x + y * y + z * z);
					}
				} else {
					for (int i = 0; i < puffer.length; i++) {
						x = puffer[i].values2[0];
						y = puffer[i].values2[1];
						z = puffer[i].values2[2];
						acceleration[i] = (float) Math.sqrt(x * x + y * y + z
								* z);
					}
				}
			}
			return Util.normalize(acceleration);
		} else {
			return null;
		}
	}

	public SensorItem[] getHistoryEvents(long startTime, long endTime) {
		if (historyStart <= startTime) {
			if (historyEnd >= endTime || historyEnd == -1) {
				SensorItem[] puffer = new SensorItem[history.size()];
				int counter = 0;
				for (SensorItem sEvent : history) {
					if (sEvent.timestamp > endTime) {
						break;
					}
					if (sEvent.timestamp >= startTime) {
						puffer[counter] = sEvent;
						counter++;
					}

				}
				if (counter > 0) {
					SensorItem[] events = new SensorItem[counter];
					System.arraycopy(puffer, 0, events, 0, counter);
					return events;
				}
			}
		}
		return null;
	}

	public double[] getAccelerationN(long startTime, long endTime,
			boolean gravity) {

		double[] acceleration = getAcceleration(startTime, endTime, gravity);
		if (acceleration != null) {
			acceleration = Util.normalize(acceleration);
		}
		return acceleration;

	}

	public double[] getAcceleration(long startTime, long endTime,
			boolean gravity) {
		SensorItem[] puffer = getAccelerometerEvents(startTime, endTime);
		double[] acceleration = null;
		if (puffer != null) {
			acceleration = new double[puffer.length];
			float x;
			float y;
			float z;
			if (gravity) {
				for (int i = 0; i < puffer.length; i++) {
					x = puffer[i].values[0];
					y = puffer[i].values[1];
					z = puffer[i].values[2];
					acceleration[i] = Math.sqrt(x * x + y * y + z * z);
				}
			} else {
				for (int i = 0; i < puffer.length; i++) {
					x = puffer[i].values2[0];
					y = puffer[i].values2[1];
					z = puffer[i].values2[2];
					acceleration[i] = (float) Math.sqrt(x * x + y * y + z * z);
				}
			}
		}
		return acceleration;

	}

	public SensorItem[] getAccelerometerEvents(long startTime, long endTime) {
		SensorItem[] puffer = new SensorItem[accelerometerData.size()];
		int counter = 0;
		for (SensorItem sEvent : accelerometerData) {
			if (sEvent.timestamp > endTime) {
				break;
			}
			if (sEvent.timestamp >= startTime) {
				puffer[counter] = sEvent;
				counter++;
			}

		}
		if (counter > 0) {
			SensorItem[] events = new SensorItem[counter];
			System.arraycopy(puffer, 0, events, 0, counter);
			return events;
		} else {
			return null;
		}
	}

	public void recordHistory(boolean record) {

		if (record == false && recordHistory == true) {
			historyCounter = 0;
			historyEnd = lastHistoryEventTime;
		} else if (record == true && recordHistory == false) {
			historyStart = -1;
			historyEnd = -1;
		}
		recordHistory = record;

	}

	public void addAccelerometerEvent(SensorEvent event) {
		SensorItem eventData = new SensorItem();
		accelerometerTimestamp = event.timestamp;
		eventData.timestamp = accelerometerTimestamp;
		if (firstGravity) {
			gravity[0] = event.values[0];
			gravity[1] = event.values[1];
			gravity[2] = event.values[2];
		} else {
			gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
			gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
			gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
		}
		eventData.values2[0] = event.values[0] - gravity[0];
		eventData.values2[1] = event.values[1] - gravity[1];
		eventData.values2[2] = event.values[2] - gravity[2];
		System.arraycopy(event.values, 0, eventData.values, 0,
				event.values.length);

		accelerometerData.add(eventData);

		Iterator<SensorItem> i = accelerometerData.iterator();
		while (i.hasNext()) {
			SensorItem sEvent = i.next();
			long time = accelerometerTimestamp - sEvent.timestamp;
			if (time > 5000000000L) {
				i.remove();
			} else {
				break;
			}
		}

		if (recordHistory) {
			if (historyStart == -1) {
				historyStart = event.timestamp;
			}
			lastHistoryEventTime = event.timestamp;
			if (historyCounter % 2 == 0) {
				history.add(eventData);
			}
			historyCounter++;
			i = history.iterator();
			while (i.hasNext()) {
				SensorItem sEvent = i.next();
				long time = accelerometerTimestamp - sEvent.timestamp;
				if (time > 240000000000L) {
					i.remove();
				} else {
					break;
				}
			}
		}

		System.arraycopy(event.values, 0, mLastAccelerometer, 0,
				event.values.length);
		mLastAccelerometerSet = true;
	}

	static long tmpcounter = 0;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event != null) {
			// Log.v(TAG,"onSensorChanged: listeningOrientation: "+listeningOrientation+" listeningSteps: "+listeningSteps);
			if (event.sensor == mAccelerometer) {
				if (firstEventTime == -1) {
					firstEventTime = event.timestamp;
				}
				lastEventTime = event.timestamp;
				if (stepDetector != null) {
					stepDetector.onSensorChanged(event);
				}
				addAccelerometerEvent(event);

				tmpcounter++;

			} else if (event.sensor == mMagnetometer) {
				System.arraycopy(event.values, 0, mLastMagnetometer, 0,
						event.values.length);
				mLastMagnetometerSet = true;
			}
			if (listeningOrientation) {
				// Log.v(TAG,"mLastAccelerometerSet: "+mLastAccelerometerSet+" mLastMagnetometerSet: "+mLastMagnetometerSet);
				if (mLastAccelerometerSet && mLastMagnetometerSet) {
					long currentTime = System.currentTimeMillis();
					if (currentTime - lastSensorTime > 100) {
						lastSensorTime = currentTime;
						SensorManager.getRotationMatrix(mR, null,
								mLastAccelerometer, mLastMagnetometer);
						SensorManager.getOrientation(mR, mOrientation);

						if (orientationData != null) {
							Log.v(TAG, "orientationData.onSensorChanged!!");
							orientationData.onSensorChanged(mOrientation);
						}

						String accelerometerString = String.format(
								Locale.getDefault(), "?ax=%f&ay=%f&az=%f",
								mLastAccelerometer[0], mLastAccelerometer[1],
								mLastAccelerometer[2]);
						String magnetometerString = String.format(
								Locale.getDefault(), "&mx=%f&my=%f&mz=%f",
								mLastMagnetometer[0], mLastMagnetometer[1],
								mLastMagnetometer[2]);
						String orientationString = String.format(
								Locale.getDefault(), "&ox=%f&oy=%f&oz=%f",
								mOrientation[0], mOrientation[1],
								mOrientation[2]);

						if (RealtimeData.isMode("Orientation")) {
							RealtimeData.sendNotification(":81/orientation/"
									+ accelerometerString + magnetometerString
									+ orientationString);
						} 
					}
				}

			}
		}

	}
}
