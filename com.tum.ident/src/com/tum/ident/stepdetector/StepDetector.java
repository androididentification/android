package com.tum.ident.stepdetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;

import com.tum.ident.gaitrecognition.StepData;

public class StepDetector {
	private final static String TAG = "StepDetector";
	private float mLimit = 7;

	private float lastValue;

	private float lastDirection;
	private float mLastExtremes[] = new float[2];
	private float mLastDiff;
	private int mLastMatch = -1;

	private StepData stepData = null;

	public StepDetector() {

	}

	public void setSensitivity(float sensitivity) {
		mLimit = sensitivity;
	}

	public void setStepData(StepData stepData) {
		this.stepData = stepData;
	}

	// public void onSensorChanged(int sensor, float[] values) {
	public void onSensorChanged(SensorEvent event) {
		Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];

			float v = (float) Math.sqrt(x * x + y * y + z * z) * 4;
			float direction = (v > lastValue ? 1 : (v < lastValue ? -1 : 0));
			if (direction == -lastDirection) {
				// Direction changed
				int extType = (direction > 0 ? 0 : 1);
				mLastExtremes[extType] = lastValue;
				float diff = Math.abs(mLastExtremes[extType]
						- mLastExtremes[1 - extType]);
				if (diff > mLimit) {
					boolean isAlmostAsLargeAsPrevious = diff > (mLastDiff * 2 / 3);
					boolean isPreviousLargeEnough = mLastDiff > (diff / 3);
					boolean isNotContra = (mLastMatch != 1 - extType);
					boolean isAccelerating = (direction == 1);
					if (isAccelerating && isAlmostAsLargeAsPrevious
							&& isPreviousLargeEnough && isNotContra) {
						Log.v(TAG, "step!!!!!!!!!!!!!!!!!!!!!!!!");
						if (stepData != null) {
							stepData.onSensorChanged(event);
						}
						mLastMatch = extType;
					} else {
						mLastMatch = -1;
					}
				}
				mLastDiff = diff;
			}
			lastDirection = direction;
			lastValue = v;
		}
	}
}
