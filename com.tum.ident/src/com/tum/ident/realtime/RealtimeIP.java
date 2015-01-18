package com.tum.ident.realtime;

import java.io.UnsupportedEncodingException;

import android.os.Bundle;
import android.util.Log;

import com.tum.ident.network.NetworkClient;
import com.tum.ident.storage.StorageHandler;

public class RealtimeIP implements Runnable {
	private final static String TAG = "RealtimeData";
	private String url;
	private String[] addresses;
	private int index;

	private  static boolean found = false;
	private  static int foundIndex = 0;

	public RealtimeIP(int[] ip, int index, String[] addresses) {
		this.url = "http://" + ip[0] + "." + ip[1] + "." + ip[2] + "." + ip[3];
		this.addresses = addresses;
		this.index = index;
	}

	@Override
	public void run() {
		Bundle params = new Bundle();
		NetworkClient networkClient = new NetworkClient();
		networkClient.setTimeout(10000);
		params.putString("ping", "true");
		byte[] bytes = networkClient.postBundleData(url + "/ping/", params,
				NetworkClient.DataType.Byte, true);
		if (bytes != null) {
			if (bytes.length > 0) {
				try {
					String result = new String(bytes, "UTF-8");
					if (result.equals("AndroidIdentification")) {
						addresses[index] = url;
						foundIndex = index;
						found = true;
					}
				} catch (UnsupportedEncodingException e) {

				}
			}
		}
	}

	public static String lookUp(int[] ip) {
		String storedIP = StorageHandler.readFromFile("realtimeIP.dat");
		String[] addresses = new String[254];
		Thread threads[] = new Thread[254];
		boolean started[] = new boolean[254];
		found = false;
		boolean running = true;
		int startIndex = 0;
		if (storedIP.length() > 0) {
			String[] ips = storedIP.split(",");
			if (ips.length > 0) {
				for (int i = 0; i < ips.length; ++i) {
					Log.v(TAG, "check storedIP: " + ips[i]);
					if (ips[i].length() > 0) {
						ip[3] = Integer.valueOf(ips[i]);
						if (ip[3] > 0) {
							threads[ip[3] - 1] = new Thread(new RealtimeIP(ip,
									ip[3] - 1, addresses));
							threads[ip[3] - 1].start();
							started[ip[3] - 1] = true;
						}
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {

				}
			}
		}
		while (found == false && running) {
			if (startIndex < 254) {
				if (started[startIndex] == false) {
					ip[3] = startIndex + 1;
					threads[startIndex] = new Thread(new RealtimeIP(ip,
							startIndex, addresses));
					threads[startIndex].start();
					started[startIndex] = true;
				}
				startIndex++;
			} else {
				try {
					Thread.sleep(60);
				} catch (InterruptedException e) {

				}
				running = false;
				for (int i = 0; i < 254; i++) {
					if (threads[i].isAlive()) {
						running = true;
						break;
					}
				}
			}
		}
		if (found) {
			String newEntry = (foundIndex + 1) + ",";
			if (storedIP.contains(newEntry) == false) {
				storedIP = newEntry + storedIP;
			}
			StorageHandler.writeToFile("realtimeIP.dat", storedIP);
			return addresses[foundIndex];
		} else {
			return "";
		}
	}
}
