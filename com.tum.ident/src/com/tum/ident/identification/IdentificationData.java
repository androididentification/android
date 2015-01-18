package com.tum.ident.identification;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;

import com.tum.ident.data.DataController;
import com.tum.ident.data.DataItem;


public class IdentificationData {
	
	@SuppressWarnings("unused")
	private static final String TAG = "IdentificationData";
	private IdentificationItem identificationItem;
	private Context context = null;
	private boolean dataCollected = false;
	private boolean answerReceived = false;
	private long startTime;
	private boolean stopIdentification = false;
	private boolean collectingData = false;
	private DataController dataController;

	public IdentificationData(Context context, DataController dataController) {
		this.dataController = dataController;
		this.context = context;
		identificationItem = new IdentificationItem();
		startTime = java.lang.System.currentTimeMillis();
	}

	public void onReceiveIds(String deviceID, String userID) {
		identificationItem.setUserID(userID);
		identificationItem.setDeviceID(deviceID);
		identificationItem.getUserDevice().getUser().setGid(userID);
		identificationItem.getUserDevice().getDevice().setGid(deviceID);
		answerReceived = true;
	}

	private void collectIdentificationData(Context context) {
		identificationItem.getUserDevice().collectData(context);
		dataCollected = true;
	}

	public boolean isReady(){
		return dataCollected;
	}
	
	public void startIdentification() {
		stopIdentification = false;
		new IdentificationAsync().execute();
	}

	public boolean addIdentificationData() {
		if (collectingData) {
			return false;
		}
		collectingData = true;
		Bundle parameter = new Bundle();
		parameter.putString("method", "add");
		new CollectAsync().execute(new Bundle(parameter));
		return true;
	}

	public boolean updateIdentificationData() {
		if (collectingData) {
			return false;
		}
		collectingData = true;
		Bundle parameter = new Bundle();
		parameter.putString("method", "update");
		new CollectAsync().execute(new Bundle(parameter));
		return true;
	}

	public void stopIdentification() {
		stopIdentification = true;
	}

	public DataItem getDataItem() {
		if (identificationItem != null) {
			if (dataCollected) {
				return new DataItem("", identificationItem);
			}
		}
		return null;
	}
	
	public IdentificationItem getIdentificationItem(){
		if(dataCollected){
			return identificationItem;
		}
		else{
			return null;
		}
	}

	private class IdentificationAsync extends AsyncTask<Void, String, String> {
		@Override
		protected String doInBackground(Void... params) {
			Looper.prepare();
			do {
				if (answerReceived
						|| java.lang.System.currentTimeMillis() - startTime > 20000) {
					if (identificationItem.getDeviceID().length() > 0
							&& identificationItem.getUserID().length() > 0) {

						IdentificationItem compareItem = new IdentificationItem(identificationItem.getDeviceID(),identificationItem.getUserID());
						dataController.addData("", compareItem);
						break;
					}
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			} while (stopIdentification == false);
			Looper.loop();
			return null;
		}

	}

	private class CollectAsync extends AsyncTask<Bundle, String, String> {
		@Override
		protected String doInBackground(Bundle... params) {
			Looper.prepare();
			if (dataCollected == false) {
				collectIdentificationData(context);
			}
			Object methodObject = params[0].get("method");
			if (methodObject != null) {
				identificationItem.setMethod(methodObject.toString());

			}
			if (identificationItem.getMethod().equals("add")) {
				dataController.addData("void", identificationItem);
			} else {
				dataController.addData("", identificationItem);
			}
			collectingData = false;
			Looper.loop();
			return null;
		}

	}

}
