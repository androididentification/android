package com.tum.ident.locations;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.tum.ident.data.DataController;
import com.tum.ident.data.DataItem;
import com.tum.ident.storage.StorageHandler;

public class LocationData implements Runnable, LocationListener,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener,
		com.google.android.gms.location.LocationListener {

	private static final String TAG = "LocationData";
	private LocationAreaList areaList = new LocationAreaList();

	private boolean sendLocations = true;

	private double latitude;
	private double longitude;

	private Context context;
	private DataController dataController;
	private LocationManager locationManager;
	private LocationClient locationClient;
	private  boolean listening = false;
	private  boolean connected = false;

	private  Location lastLocation = null;
	private  boolean locationReceived = false;

	private  boolean wasGPSenabled = false;

	private  boolean locationClientListening = false;
	private  boolean locationManagerListening = false;

	private boolean running = false;
	private long currentTime;
	private long startTime;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(0).setFastestInterval(0)
			.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

	public LocationData(Context context, DataController dataController) {
		this.context = context;
		this.dataController = dataController;
		latitude = 0;
		longitude = 0;
		locationClient = new LocationClient(context, this, this);
		locationClient.connect();
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
		loadLocations();
		registerListener(false);
	}

	public void registerListener(boolean force) {
		toggleGPS(true);
		if (locationManagerListening == false) {

			locationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, this);
			locationManagerListening = true;
			Log.v(TAG, "registerListener: " + LocationManager.GPS_PROVIDER);
		}
		if (locationClientListening == false) {
			if (connected) {
				locationClient.requestLocationUpdates(REQUEST, this);
				locationClientListening = true;
				Log.v(TAG, "registerListener (google): " + REQUEST);
			}
		}
		// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 0, 0,this);
		currentTime = System.currentTimeMillis();
		startTime = currentTime;
		if (running == false) {
			running = true;
			lastLocation = null;
			locationReceived = false;
			new Thread(this).start();
		}

	}

	public void unregisterListener() {
		Log.v(TAG, "unregisterListeners");
		saveLocations();
		if (locationManagerListening) {
			locationManager.removeUpdates(this);
			locationManagerListening = false;
		}
		if (locationClientListening) {

			if (connected) {
				locationClient.removeLocationUpdates(this);
			}
			locationClientListening = false;
		}
		toggleGPS(false);
	}

	public String getLocationString() {
		return areaList.getLocationString();

	}

	public DataItem getDataItem() {
		saveLocations();
		return new DataItem("", areaList);
	}

	public String getSummary() {
		return "Number of Areas: " + areaList.size();
	}

	public LocationAreaList getLocationAreaList() {
		return areaList;
	}

	public long locationWaitTime = 600000;
	public long locationTimer = 0;

	@Override
	public void onLocationChanged(Location location) {
		Log.v(TAG, "onLocationChanged");
		lastLocation = location;
		locationReceived = true;
	}

	public void sendLocations(boolean sendLocations) {
		this.sendLocations = sendLocations;
	}

	public void loadLocations() {
		String fileName = "locations.ser";
		areaList = null;
		areaList = (LocationAreaList) StorageHandler.loadObject(fileName);
		if (areaList == null) {
			areaList = new LocationAreaList();
		}

	}

	public void saveLocations() {
		String fileName = "locations.ser";
		Log.v(TAG, "Save Locations:" + fileName);
		StorageHandler.saveObject(areaList, fileName);

	}

	public Location getBestLocation() {
		Location bestResult = null;
		try {
			Location lastFusedLocation = null;
			if (connected) {
				lastFusedLocation = locationClient.getLastLocation();
			}
			Location gpsLocation = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			Location networkLocation = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (gpsLocation != null && networkLocation != null) {
				if (gpsLocation.getTime() > networkLocation.getTime())
					bestResult = gpsLocation;
			} else if (gpsLocation != null) {
				bestResult = gpsLocation;
			} else if (networkLocation != null) {
				bestResult = networkLocation;
			}
			if (bestResult != null && lastFusedLocation != null) {
				if (bestResult.getTime() < lastFusedLocation.getTime())
					bestResult = lastFusedLocation;
			}
			if (bestResult == null) {
				bestResult = lastLocation;
			}
		} catch (Exception e) {
			bestResult = null;
		}
		return bestResult;
	}

	public void addLocation(Location location) {
		Log.v(TAG, "NEW LOCATION!!!!");
		if (location != null) {
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			// double acc = location.getAccuracy();
			// double speed = location.getSpeed();
			Log.v(TAG, "onLocationChanged");
			if (lat != latitude && lng != longitude) {
				latitude = lat;
				longitude = lng;
				if (!areaList.addLocation(location)) {
					Log.v(TAG, "User moved to new Area!");
				}
				if (sendLocations) {
					dataController.addData("", areaList);
					sendLocations = false;
				}
			}
		}
	}

	@Override
	public void run() {
		currentTime = System.currentTimeMillis();
		while (currentTime - startTime < 300000) {
			currentTime = System.currentTimeMillis();
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Log.v(TAG, "Error: Thread.sleep()", e);
			}
			if (locationReceived) {
				Log.v(TAG, "locationReceived! connected: " + connected);
				if (connected) {
					break;
				} else if (currentTime - startTime > 5000) {
					break;
				}
			}

		}
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
			Log.v(TAG, "Error: Thread.sleep()", e);
		}
		Log.v(TAG, "wait for location finished!");
		Location location = getBestLocation();
		Log.v(TAG, "Location: " + location);
		addLocation(location);
		unregisterListener();
		locationReceived = false;
		running = false;
	}

	public void toggleGPS(boolean activate) {
		if (activate) {
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
				Intent intent = new Intent(
						"android.location.GPS_ENABLED_CHANGE");
				intent.putExtra("enabled", true);
				context.sendBroadcast(intent);
			}
			@SuppressWarnings("deprecation")
			String provider = Settings.Secure.getString(
					context.getContentResolver(),
					Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
			if (!provider.contains("gps")) {
				wasGPSenabled = false;
				// if gps is disabled
				final Intent poke = new Intent();
				poke.setClassName("com.android.settings",
						"com.android.settings.widget.SettingsAppWidgetProvider");
				poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
				poke.setData(Uri.parse("3"));
				context.sendBroadcast(poke);
			} else {
				wasGPSenabled = true;
			}
		} else {
			if (wasGPSenabled == false) {
				@SuppressWarnings("deprecation")
				String provider = Settings.Secure.getString(
						context.getContentResolver(),
						Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
				if (provider.contains("gps")) {
					final Intent poke = new Intent();
					poke.setClassName("com.android.settings",
							"com.android.settings.widget.SettingsAppWidgetProvider");
					poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
					poke.setData(Uri.parse("3"));
					context.sendBroadcast(poke);
				}
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onConnected(Bundle arg0) {
		Log.v(TAG, "connected!");
		connected = true;
		if (listening == true) {
			registerListener(true);
		}

	}

	@Override
	public void onDisconnected() {

		connected = false;
		// TODO Auto-generated method stub

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub

	}

}
