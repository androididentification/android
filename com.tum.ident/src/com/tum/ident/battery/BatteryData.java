package com.tum.ident.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.tum.ident.data.DataController;
import com.tum.ident.data.DataItem;
import com.tum.ident.storage.StorageHandler;


public class BatteryData extends BroadcastReceiver {

	@SuppressWarnings("unused")
	private final static String TAG = "BatteryData";

	private BatteryItemList battery = new BatteryItemList();

	private Context context;

	private DataController dataController;

	private boolean received = false;

	public BatteryData(Context context, DataController dataController) {
		this.dataController = dataController;
		this.context = context;
		IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		load();
		this.context.registerReceiver(this, filter);
		//Log.v(TAG, "registerReceiver");
	}

	public BatteryItemList getBatteryItemList() {
		return battery;
	}

	public String getBatteryString() {
		if (battery != null) {
			return battery.toString();
		}
		return "";
	}

	public DataItem getDataItem() {
		//Log.v(TAG, "batteryData.getDataItem");
		save();
		return new DataItem("", battery);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (battery == null) {
			battery = new BatteryItemList();
		}
		//Log.v(TAG, "onReceive!!!");
		int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
		int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
		//Log.v(TAG, "level: " + level);
		//Log.v(TAG, "scale: " + scale);

		int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
		boolean present = intent.getExtras().getBoolean(
				BatteryManager.EXTRA_PRESENT);
		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
		int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,
				0);
		if (battery.update(level, scale, plugged, present, status, temperature)) {
			save();
			dataController.addData("", battery);
			received = true;
		} else if (received == false) {
			dataController.addData("", battery);
			received = true;
		}
	}

	public void load() {

		String fileName = "battery.ser";

		battery = null;
		battery = (BatteryItemList) StorageHandler.loadObject(fileName);
		if (battery == null) {
			battery = new BatteryItemList();
		}

	}
	
	public void save() {
		
		String fileName = "battery.ser";

		StorageHandler.saveObject(battery, fileName);

	}

}
