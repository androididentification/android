package com.tum.ident.wlan;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.tum.ident.userdevice.WLANItem;
import com.tum.ident.util.HashGenerator;

public class WLANData {
	// private static final String TAG = "WLANData";
	private static WifiManager wifiManager = null;
	private static WifiInfo wifiInf = null;

	private static boolean wasEnabled = false;

	public static void finish() {
		if (wasEnabled == false) {
			if (wifiManager != null) {
				wifiManager.setWifiEnabled(false);
			}
		}
	}

	public static void init(Context context) {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager != null) {
			wifiInf = wifiManager.getConnectionInfo();
		}
		if (wifiManager != null) {
			if (!wifiManager.isWifiEnabled()) {
				wifiManager.setWifiEnabled(true);
				wasEnabled = false;
			} else {
				wasEnabled = true;
			}
		}
	}

	public static String getMAC() {
		if (wifiInf != null)
			return HashGenerator.hash(wifiInf.getMacAddress());
		else
			return null;
	}

	public static String getSSID() {
		if (wifiInf != null)
			return HashGenerator.hash(wifiInf.getSSID());
		else
			return null;
	}

	public static String getBSSID() {
		if (wifiInf != null)
			return HashGenerator.hash(wifiInf.getBSSID());
		else
			return null;
	}

	public static WLANItem getWLAN(Context context) {
		if (wifiInf != null) {
			String SSID = wifiInf.getSSID();
			String BSSID = wifiInf.getBSSID();
			if (SSID != null && BSSID != null) {
				return new WLANItem(HashGenerator.hash(SSID),
						HashGenerator.hash(BSSID), WLANItem.WLANType.Device);
			} else if (SSID != null) {
				return new WLANItem(HashGenerator.hash(SSID), "",
						WLANItem.WLANType.Device);
			} else if (BSSID != null) {
				return new WLANItem("", HashGenerator.hash(BSSID),
						WLANItem.WLANType.Device);
			} else {
				return null;
			}

		} else
			return null;
	}

	public static ArrayList<WLANItem> getWLANData(Context context) {
		if (wifiManager != null) {
			ArrayList<WLANItem> wlanList = new ArrayList<WLANItem>();
			// getCurrentSsid(context);
			List<WifiConfiguration> configs = wifiManager
					.getConfiguredNetworks();
			if (configs != null) {
				if (configs.size() > 0) {
					for (WifiConfiguration config : configs) {
						wlanList.add(new WLANItem(HashGenerator
								.hash(config.SSID), "",
								WLANItem.WLANType.Configured));
						// "\n\n"+config.SSID+"\n\n---------------------";
					}
				}
			}
			return wlanList;
		} else
			return null;
	}
}

/*
 * 
 * public static String getMACAddress(String interfaceName) { try {
 * List<NetworkInterface> interfaces =
 * Collections.list(NetworkInterface.getNetworkInterfaces()); for
 * (NetworkInterface intf : interfaces) { if (interfaceName != null) { if
 * (!intf.getName().equalsIgnoreCase(interfaceName)) continue; } byte[] mac =
 * intf.getHardwareAddress(); if (mac==null) return ""; StringBuilder buf = new
 * StringBuilder(); for (int idx=0; idx<mac.length; idx++)
 * buf.append(String.format("%02X:", mac[idx])); if (buf.length()>0)
 * buf.deleteCharAt(buf.length()-1); return buf.toString(); } } catch (Exception
 * ex) { } // for now eat exceptions return ""; }
 * 
 * public String getCurrentSsid(Context context) {
 * 
 * String ssid = null; ConnectivityManager connManager = (ConnectivityManager)
 * context.getSystemService(Context.CONNECTIVITY_SERVICE); NetworkInfo
 * networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI); if
 * (networkInfo.isConnected()) { final WifiManager wifiManager = (WifiManager)
 * context.getSystemService(Context.WIFI_SERVICE); WifiInfo info =
 * wifiManager.getConnectionInfo(); String textStatus = ""; textStatus +=
 * "\n\nWiFi Status: " + info.toString(); String BSSID = info.getBSSID(); String
 * MAC = info.getMacAddress();
 * 
 * List<ScanResult> results = wifiManager.getScanResults(); ScanResult
 * bestSignal = null; int count = 1; String etWifiList = ""; for (ScanResult
 * result : results) { etWifiList += count++ + ". " + result.SSID + " : " +
 * result.level + "\n" + result.BSSID + "\n" + result.capabilities +"\n" +
 * "\n=======================\n"; } Log.v(TAG, "from SO: \n"+etWifiList);
 * 
 * // List stored networks
 * Log.v(TAG,"\n\nList stored networks---------------------");
 * List<WifiConfiguration> configs = wifiManager.getConfiguredNetworks(); for
 * (WifiConfiguration config : configs) { //textStatus+= "\n\n" +
 * config.toString();
 * 
 * textStatus+= "\n\n"+config.SSID+"\n\n---------------------"; }
 * Log.v(TAG,"from marakana: \n"+textStatus); } return ssid; }
 */

