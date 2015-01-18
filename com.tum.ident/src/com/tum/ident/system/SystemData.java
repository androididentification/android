package com.tum.ident.system;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.tum.ident.device.DeviceIDItem;
import com.tum.ident.util.HashGenerator;
import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.ads.identifier.AdvertisingIdClient.Info;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;



public class SystemData {

	private static final String TAG = "SystemData";

	private static final Uri GSFURI = Uri
			.parse("content://com.google.android.gsf.gservices");

	public static DeviceIDItem getDeviceIDs(Context context) {

		TelephonyManager phoneManager;
		phoneManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imeiString = phoneManager.getDeviceId();
		String gsfAndroidID = getGsfAndroidId(context);
		String serialNum = "";
		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class, String.class);
			serialNum = (String) (get.invoke(c, "ro.serialno", "unknown"));
		} catch (Exception ignored) {
		}
		String androidID = Settings.Secure.getString(
				context.getContentResolver(), Settings.Secure.ANDROID_ID);
		

		Info adInfo = null;
		String AdId = "";
		try {
			adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
			AdId = adInfo.getId();
		} catch (IOException e) {

		} catch (GooglePlayServicesNotAvailableException e) {

		} catch (IllegalStateException e) {

		} catch (GooglePlayServicesRepairableException e) {

		}

		Log.v(TAG,"AdId: "+AdId);
		Log.v(TAG,"gsfAndroidID: "+gsfAndroidID);
		Log.v(TAG,"imeiString: "+imeiString);
		Log.v(TAG,"serialNum: "+serialNum);
		Log.v(TAG,"androidID: "+androidID);

		return new DeviceIDItem(HashGenerator.hash(gsfAndroidID),
				HashGenerator.hash(imeiString), HashGenerator.hash(serialNum),
				HashGenerator.hash(androidID));
	}

	public static String getManufacturer() {
		return HashGenerator.hash(Build.MANUFACTURER);
	}

	public static String getModel() {
		return HashGenerator.hash(Build.MODEL);
	}

	@SuppressWarnings("deprecation")
	public static String getInternalStorageSize() {
		StatFs statFs = new StatFs(Environment.getRootDirectory()
				.getAbsolutePath());
		long blockSize = statFs.getBlockSize();
		return String.valueOf(statFs.getBlockCount() * blockSize);
	}

	public static String getTotalMemory() {

		String str1 = "/proc/meminfo";
		String str2;
		String[] arrayOfString;
		long initial_memory = 0;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// meminfo
			arrayOfString = str2.split("\\s+");
			Log.v(TAG, str2);
			for (String num : arrayOfString) {
				Log.v(TAG, num + "\t");
			}
			do {
				str2 = localBufferedReader.readLine();
				if (str2 != null)
					Log.v(TAG, str2);
			} while (str2 != null);
			initial_memory = Integer.valueOf(arrayOfString[1]).intValue() * 1024;
			localBufferedReader.close();
			return String.valueOf(initial_memory);
		} catch (IOException e) {
			return "-1";
		}
	}

	private static String getGsfAndroidId(Context context) {
		if (context != null) {
			String ID_KEY = "android_id";
			String params[] = { ID_KEY };
			Cursor c = context.getContentResolver().query(GSFURI, null, null,
					params, null);
			if (c != null) {
				if (!c.moveToFirst() || c.getColumnCount() < 2) {
					return "";
				}
				try {
					return HashGenerator.hash(Long.toHexString(Long.parseLong(c
							.getString(1))));
				} catch (NumberFormatException e) {
					return "";
				}
			}
		}
		return "";
	}

	public static String getCPUInfo() {

		StringBuffer sb = new StringBuffer();

		Log.v(TAG, "Build.HARDWARE: " + Build.HARDWARE);
		Log.v(TAG, "Build.BRAND: " + Build.BRAND);
		Log.v(TAG, "Build.DEVICE: " + Build.DEVICE);
		Log.v(TAG, "Build.DISPLAY: " + Build.DISPLAY);
		Log.v(TAG, "Build.PRODUCT: " + Build.PRODUCT);

		// Log.v(TAG,"Build.SERIAL: "+Build.SERIAL);

		// String deviceSerial = (String)
		// Build.class.getField("SERIAL").get(null);

		sb.append("CPU-ABI: ").append(Build.CPU_ABI).append("<br>");

		if (new File("/proc/cpuinfo").exists()) {

			try {

				BufferedReader br = new BufferedReader(new FileReader(new File(
						"/proc/cpuinfo")));

				String aLine;

				while ((aLine = br.readLine()) != null) {

					sb.append(aLine + "<br>");

				}

				if (br != null) {

					br.close();

				}

			} catch (IOException e) {

				e.printStackTrace();

			}

		}

		return HashGenerator.hash(sb.toString());

	}

}

/*
 * public void getSystemData(Context context) {
 * 
 * String msg = "System properties\n"; msg += "-------------\n";
 * java.util.Properties props = System.getProperties(); java.util.Enumeration e
 * = props.propertyNames(); while (e.hasMoreElements()) { String k = (String)
 * e.nextElement(); String v = props.getProperty(k); msg += k+": "+v+"\n"; }
 * 
 * msg += "\n"; msg += "Envirionment variables\n"; msg += "-------------\n";
 * java.util.Map envs = System.getenv(); java.util.Set keys = envs.keySet();
 * java.util.Iterator i = keys.iterator(); while (i.hasNext()) { String k =
 * (String) i.next(); String v = (String) envs.get(k); msg += k+": "+v+"\n"; }
 * 
 * Log.v(TAG,msg);
 * 
 * 
 * // Get Phone model and manufacturer name
 * 
 * }
 */
