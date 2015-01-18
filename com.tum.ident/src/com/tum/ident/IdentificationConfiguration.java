package com.tum.ident;

import android.util.Log;

import com.tum.ident.identification.ConfigurationItem;
import com.tum.ident.storage.StorageHandler;

public class IdentificationConfiguration {

	public static int hashLength = 64;

	static String TAG = "IdentificationConfiguration";

	// StepDetector
	public static double dwtAcceptThreshold = 8000;
	public static double dwtThreshold = 9000;
	public static double dwtAvgThreshold = 12000;
	public static double dwtEmbodiedThreshold = 8000;
	public static int accelerationArrayLength = 150;

	public static long maxStepDuration = 1000000000L; // average: 530973451
	public static long minStepDuration = 180000000L;
	public static long diffStepAvgDuration = 100000000L;
	
	

	// Spectrum
	public static double spectrumThreshold = 0.7;

	public static ConfigurationItem config = new ConfigurationItem();

	public static void init() {
		load();
		save();
	}
	public static void load() {
		if (config == null) {
			config = new ConfigurationItem();
		}
		ConfigurationItem newConfig = config;
		String fileName = "config.ser";
		config = (ConfigurationItem) StorageHandler.loadObject(fileName);
		Log.v(TAG, "config: " + config);
		if (config == null) {
			config = new ConfigurationItem();
		}
		Log.v(TAG, "+config.serverURL: " + config.serverURL);
		if (newConfig.serverURL.length() > 0) {
			config.serverURL = newConfig.serverURL;
		}
	}

	public static void save() {
		String fileName = "config.ser";
		StorageHandler.saveObject(config, fileName);
	}

}
