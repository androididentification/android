package com.tum.ident.bluetooth;

import java.util.ArrayList;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.util.HashGenerator;

public class BluetoothData {
	
	@SuppressWarnings("unused")
	private static final String TAG = "BluetoothData";
	private static boolean enabled = true;
	private static BluetoothAdapter mBluetoothAdapter;

	public static void init() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter != null) {
			if (!mBluetoothAdapter.isEnabled()) {
				mBluetoothAdapter.enable();
				enabled = false;
			}
		}
	}

	public static void finish() {
		if (mBluetoothAdapter != null) {
			if (!enabled) {
				mBluetoothAdapter.disable();
			}
		}
	}

	public static String getMAC() {
		if (mBluetoothAdapter != null) {
			return mBluetoothAdapter.getAddress();
		}
		return "";
	}

	public static String getName() {
		if (mBluetoothAdapter != null) {
			return mBluetoothAdapter.getName();
		}
		return "";

	}

	public static BluetoothItem getBluetooth() {
		if (mBluetoothAdapter != null) {
			return new BluetoothItem(HashGenerator.hash(mBluetoothAdapter
					.getAddress()), HashGenerator.hash(mBluetoothAdapter
					.getName()));
		} else
			return null;
	}

	public static ArrayList<BluetoothItem> getBluetoothData() {
		ArrayList<BluetoothItem> bluetoothList = new ArrayList<BluetoothItem>();
		if (mBluetoothAdapter != null) {
			// if (!mBluetoothAdapter.isEnabled()) {
			// Intent enableBtIntent = new
			// Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			// startActivity(enableBtIntent);
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
					.getBondedDevices();
			if (pairedDevices.size() > 0) {
				for (BluetoothDevice device : pairedDevices) {
					bluetoothList.add(new BluetoothItem(HashGenerator
							.hash(device.getAddress()), HashGenerator
							.hash(HashGenerator.hash(device.getName()))));
				}
			}
			
		}
		return bluetoothList;
	}
}
