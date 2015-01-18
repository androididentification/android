package com.tum.ident.camera;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.tum.ident.data.DataController;
import com.tum.ident.data.DataItem;

public class CameraData implements SurfaceHolder.Callback {
	private static final String TAG = "CameraData";

	private SurfaceHolder surfaceHolder;
	private Camera serviceCamera = null;
	private boolean recordingStatus = false;

	private boolean takingPicture = false;
	private boolean stopCamera = false;

	private CameraPixelList cameraPixelsFront = new CameraPixelList();
	private CameraPixelList cameraPixelsBack = new CameraPixelList();
	private CameraPixelList cameraPixels = cameraPixelsBack;

	private long maxPixelCounter = 0;
	private long maxPixelCounterLimit = 1;

	private boolean noDarkFramesFound = false;
	private long noDarkFrameCounter = 0;
	private long noDarkFrameCounterLimit = 10;

	private Size camSize;

	private static double hotPixelThreshold = 150;

	private BlockingQueue<byte[]> bitmapQueue = new ArrayBlockingQueue<byte[]>(10);

	private DarkFrame darkFrameFront = new DarkFrame();
	private DarkFrame darkFrameBack = new DarkFrame();
	private DarkFrame darkFrame = darkFrameBack;
	private int darkFrameReadyCount = 1;

	private double histogramMax = 0;
	private double[][] histogram = new double[3][256];

	private int colorCubeLimit = 10;
	private double[][][] colorCube = new double[colorCubeLimit][colorCubeLimit][colorCubeLimit];
	private double colorCubeMax = 0;

	private DataController dataController;
	private Context context;

	private static int debugMode = 2;

	public CameraData(Context context, SurfaceHolder surfaceHolder,
			DataController dataController) {
		this.surfaceHolder = surfaceHolder;	
		this.context = context;
		this.dataController = dataController;
		
		if(surfaceHolder==null){
			SurfaceView dummy = new SurfaceView(this.context);
			surfaceHolder = dummy.getHolder();
		}

		darkFrameBack.setIndex(0);
		darkFrameFront.setIndex(1);
		cameraPixelsBack.setIndex(0);
		cameraPixelsFront.setIndex(1);

	}

	
	public void onDestroy() {
		stopRecording();
		recordingStatus = false;

	}

	public void takePictures() {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				if (surfaceHolder == null) {
					return;
				}
				for (int index = 0; index < 2; index++) {
					if (index == 0)
						serviceCamera = openFrontFacingCamera();
					recordingStatus = true;
					darkFrame = darkFrameFront;
					cameraPixels = cameraPixelsFront;
					try {
						serviceCamera = Camera.open();
						recordingStatus = true;
						darkFrame = darkFrameBack;
						cameraPixels = cameraPixelsBack;
					} catch (RuntimeException e) {
						e.printStackTrace();
					}

					Log.v(TAG, "takePictures Runnable");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					try {
						// Toast.makeText(getBaseContext(), "Take Picture",
						// Toast.LENGTH_SHORT).show();
						Camera.Parameters params = serviceCamera
								.getParameters();

						int maxc = params.getMaxExposureCompensation();
						params.setExposureCompensation(maxc);

						serviceCamera.setParameters(params);
						Camera.Parameters p = serviceCamera.getParameters();

						List<Size> listSize = p.getSupportedPreviewSizes();

						Size previewSize = listSize.get(2);
						Log.v(TAG, "use: width = " + previewSize.width
								+ " height = " + previewSize.height);
						listSize = params.getSupportedPictureSizes();
						Log.v(TAG, "num sizes possible: " + listSize.size());
						/*
						 * for (Size sizeItem : listSize) { Log.v(TAG,
						 * "possible: width = " + sizeItem.width+ " height = " +
						 * sizeItem.height); }
						 */
						camSize = listSize.get(0);
						p.setPictureSize(camSize.width, camSize.height);

						p.setPreviewSize(previewSize.width, previewSize.height);
						p.setPreviewFormat(ImageFormat.NV21);// PixelFormat.YCbCr_420_SP);
						serviceCamera.setParameters(p);
						try {
							serviceCamera.setPreviewDisplay(surfaceHolder);
						} catch (IOException e) {
							Log.e(TAG, e.getMessage());
							e.printStackTrace();
						}
					} catch (IllegalStateException e) {
						Log.d(TAG, e.getMessage());
						e.printStackTrace();
					}
					Log.v(TAG, "serviceCamera.takePicture!!");

					takingPicture = false;
					// for (int i = 0; i <= 300000000; i++) {
					long startTime = System.currentTimeMillis();
					long cameraPictureTime = 20000;

					byte[] data = null;
					while (System.currentTimeMillis() - startTime < cameraPictureTime) {
						if (takingPicture) {
							data = bitmapQueue.poll();
							if (data != null) {
								newPicture(data);
							}
						}
						if (stopCamera) {
							break;
						}
						if (noDarkFramesFound) {
							break;
						}
						if (takingPicture == false) {
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							try {
								Log.v(TAG, "serviceCamera.startPreview!!");
								serviceCamera.startPreview();
								Log.v(TAG, "serviceCamera.startPreview ready!!");
								takingPicture = true;
								serviceCamera.takePicture(null, null, pictureCallback);
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (RuntimeException e) {
								e.printStackTrace();
							}
							try {
								Thread.sleep(5000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						} else {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
					serviceCamera.stopPreview();
					serviceCamera.setPreviewCallback(null);
					serviceCamera.release();
				}
				recordingStatus = false;

			}
		};

		if (recordingStatus == false) {
			noDarkFramesFound = false;
			recordingStatus = true;

			if (serviceCamera != null) {
				new Thread(runnable).start();
			}
		}

	}

	public DataItem getDataItem(DataController.DataType type) {
		if (type == DataController.DataType.DarkFrameFront) {
			if (darkFrameFront.getImage() != null) {
				darkFrameFront.createData();
				
			} else {
				darkFrameFront.setData(null);
			}
			return new DataItem("", darkFrameFront);
		} else if (type == DataController.DataType.DarkFrameBack) {
			if (darkFrameBack.getImage() != null) {
				darkFrameBack.createData();
				
			} else {
				darkFrameBack.setData(null);
			}

			return new DataItem("", darkFrameBack);
		} else if (type == DataController.DataType.PixelErrorFront) {
			return new DataItem("", cameraPixelsFront);
		} else if (type == DataController.DataType.PixelErrorBack) {
			return new DataItem("", cameraPixelsBack);
		}
		return null;
	}

	private Camera openFrontFacingCamera() {
		int cameraCount = 0;
		Camera cam = null;
		CameraInfo cameraInfo = new CameraInfo();
		cameraCount = Camera.getNumberOfCameras();
		for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
			Camera.getCameraInfo(camIdx, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				try {
					cam = Camera.open(camIdx);
				} catch (RuntimeException e) {
				}
			}
		}
		return cam;
	}
	
	private void newPicture(byte[] data) {

		System.gc();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPurgeable = true;
		Bitmap bmp = convertToMutable(BitmapFactory.decodeByteArray(data, 0,
				data.length, options));
		data = null;

		boolean darkFrameFound = false;

		colorCubeMax = 0;
		boolean imageSaved = false;
		ArrayList<CameraPixel> newCameraPixels = new ArrayList<CameraPixel>();
		// decode the data obtained by the camera into a Bitmap
		Log.v(TAG, "Image taken!!");

		System.gc();
		long numPixel = bmp.getWidth() * bmp.getHeight();
		long sumRed = 0;
		long sumGreen = 0;
		long sumBlue = 0;
		long counter = 0;



		colorCube = new double[colorCubeLimit][colorCubeLimit][colorCubeLimit];

		// int[][][] bmpColorCube = new
		// int[colorCubeLimit][colorCubeLimit][colorCubeLimit];

		for (int i = 0; i < bmp.getWidth(); i = i + 300) {
			for (int j = 0; j < bmp.getHeight(); j = j + 300) {
				int rgb = bmp.getPixel(i, j); // ;rgbValues[i][j];
				int red = Color.red(rgb);
				int green = Color.green(rgb);
				int blue = Color.blue(rgb);
				sumRed = sumRed + red;
				sumGreen = sumGreen + green;
				sumBlue = sumBlue + blue;
				counter++;
			}
		}
		double avgRed = 0;
		double avgGreen = 0;
		double avgBlue = 0;
		if (counter > 0) {
			avgRed = (double) sumRed / counter;
			avgGreen = (double) sumGreen / counter;
			avgBlue = (double) sumBlue / counter;
		}
		Log.v(TAG, "avgColor (fast): (" + avgRed + "," + avgGreen + ","
				+ avgBlue + ")");

		if (debugMode == 2 || (avgRed < 2 && avgGreen < 2 && avgBlue < 2)) {

			if (debugMode == 0) {
				sumRed = 0;
				sumGreen = 0;
				sumBlue = 0;

				for (int i = 0; i < bmp.getWidth(); i++) {
					for (int j = 0; j < bmp.getHeight(); j++) {
						int rgb = bmp.getPixel(i, j);
						// rgbValues[i][j] = rgb;

						int red = Color.red(rgb);
						int green = Color.green(rgb);
						int blue = Color.blue(rgb);
						int max = red;

						/*
						 * bmpHistogram[0][red]++; bmpHistogram[1][green]++;
						 * bmpHistogram[2][blue]++;
						 * 
						 * 
						 * int r = (red<colorCubeLimit?red:colorCubeLimit-1);
						 * int g =
						 * (green<colorCubeLimit?green:colorCubeLimit-1); int b
						 * = (blue<colorCubeLimit?blue:colorCubeLimit-1);
						 * if(r!=0||g!=0||b!=0){ colorCube[r][g][b] =
						 * colorCube[r][g][b]+1;
						 * 
						 * if(colorCube[r][g][b]>colorCubeMax){ } }
						 */

						sumRed = sumRed + red;
						sumGreen = sumGreen + green;
						sumBlue = sumBlue + blue;

						if (red > avgRed + hotPixelThreshold
								|| green > avgGreen + hotPixelThreshold
								|| blue > avgBlue + hotPixelThreshold) {
							if (newCameraPixels.size() < 100) {
								newCameraPixels.add(new CameraPixel(i, j, rgb));
							}
						}

						if (green > max) {
							max = green;
						}
						if (blue > max) {
							max = blue;
						}
						if (max > 0) {
							double scale = 250 / (double) max;
							red = (int) (red * scale);
							green = (int) (green * scale);
							blue = (int) (blue * scale);
						}

						// rgb = Color.argb(alpha, red, green, blue);
						// bmp.setPixel(i, j,rgb);
					}
				}

				avgRed = (double) sumRed / numPixel;
				avgGreen = (double) sumGreen / numPixel;
				avgBlue = (double) sumBlue / numPixel;
				Log.v(TAG, "avgColor (complete): (" + avgRed + "," + avgGreen
						+ "," + avgBlue + ")");
			}
			if (debugMode == 2 || (avgRed < 2 && avgGreen < 2 && avgBlue < 2)) {
				Log.v(TAG, "DARKFRAME!!");
				/*
				 * Log.v(TAG, "SAVE DARKFRAME!!"); for(int i=0; i < 3; i++) {
				 * for(int j=0; j < 256; j++) { histogram[i][j] =
				 * (bmpHistogram[i
				 * ][j]+histogram[i][j]*histogramWeight)/(histogramWeight+1);
				 * if(histogramMax<histogram[i][j]){ histogramMax =
				 * histogram[i][j]; } } } histogramWeight++;
				 */
				Iterator<CameraPixel> i = newCameraPixels.iterator();
				while (i.hasNext()) {
					CameraPixel hp2 = i.next();
					boolean found = false;
					for (CameraPixel hp1 : cameraPixels.getList()) {
						if (hp1.getX() == hp2.getX() && hp2.getY() == hp1.getY()) {
							hp1.incrementCounter();
							if (maxPixelCounter < hp1.getCounter()) {
								maxPixelCounter = hp1.getCounter();
							}
							if (hp1.getColor() == hp2.getColor()) {
								hp1.setType(CameraPixel.Type.DeadPixel);
							} else {
								hp1.setType(CameraPixel.Type.HotPixel);
							}
							found = true;
							break;

						}
					}
					if (found) {
						i.remove();
					}
				}
				for (CameraPixel hp2 : newCameraPixels) {
					cameraPixels.getList().add(hp2);
				}

				if (maxPixelCounter > maxPixelCounterLimit) {
					sendPixelError();
				}

				newCameraPixels.clear();

				Log.v(TAG, "Pixels found: " + cameraPixels.getList().size());

				Bitmap scaledbmp = Bitmap.createScaledBitmap(bmp,
						darkFrame.getWidth(), darkFrame.getHeight(), false);

				saveBitmap(bmp, "cam_" + System.currentTimeMillis() + ".png");

				updateDarkFrame(scaledbmp, avgRed, avgGreen, avgBlue);
				darkFrameFound = true;
				// dataController.addData(data, type);
				scaledbmp.recycle();
				System.gc();
				imageSaved = true;
				// saveHistogram();
				// saveColorCube();
			}

			// Toast.makeText(getApplicationContext(),"Your Picture has been taken !",
			// Toast.LENGTH_LONG).show();

		}
		if (imageSaved == false) {
			bmp.recycle();
			System.gc();
		}
		if (darkFrameFound == false) {
			noDarkFrameCounter++;
			if (noDarkFrameCounter > noDarkFrameCounterLimit) {
				noDarkFramesFound = true;
			}
		}
		takingPicture = false;
	}

	private PictureCallback pictureCallback = new PictureCallback() {
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			try {
				bitmapQueue.put(data);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	};

	private void sendPixelError() {
		if (cameraPixels.getList().size() > 0) {
			dataController.addData("", cameraPixels);
		}
	}

	private void sendDarkFrame() {
		if (darkFrame != null) {
			darkFrame.createData();
			if (darkFrame.getData() != null) {
				dataController.addData("", darkFrame);
			}
		}
	}

	private void updateDarkFrame(Bitmap scaledbmp, double avgRed,
			double avgGreen, double avgBlue) {

		double scaleRed = 0;
		double scaleGreen = 0;
		double scaleBlue = 0;

		scaleRed = 30;
		scaleGreen = 30;
		scaleBlue = 30;

		
		darkFrame.addRed(avgRed * scaleRed);
		darkFrame.addGreen(avgGreen * scaleGreen);
		darkFrame.addBlue(avgBlue * scaleBlue);
		
		for (int i = 0; i < scaledbmp.getWidth(); i++) {
			for (int j = 0; j < scaledbmp.getHeight(); j++) {
				int rgb = scaledbmp.getPixel(i, j);
				int alpha = Color.alpha(rgb);
				int red = Color.red(rgb);
				int green = Color.green(rgb);
				int blue = Color.blue(rgb);
				red = (int) (red * scaleRed);
				green = (int) (green * scaleGreen);
				blue = (int) (blue * scaleBlue);
				rgb = Color.argb(alpha, red, green, blue);
				scaledbmp.setPixel(i, j, rgb);

			}
		}

		if (darkFrame.getImage() == null || darkFrame.getWeight() == 0) {
			darkFrame.setImage(scaledbmp.copy(scaledbmp.getConfig(), true));
			saveBitmap(scaledbmp, "scaledbmp_" + System.currentTimeMillis()
					+ ".png");
			darkFrame.setWeight(1);
		} else {
			Canvas canvas = new Canvas(darkFrame.getImage());
			Paint paint = new Paint();
			int alpha = (int) ((1.0 / (darkFrame.getWeight() + 1.0)) * 255.0);
			if (alpha < 2) {
				alpha = 2;
			}
			paint.setAlpha(alpha);
			canvas.drawBitmap(scaledbmp, 0, 0, paint);
			darkFrame.increaseWeight();
		}
		if (darkFrame.getWeight() % darkFrameReadyCount == 0 || debugMode == 2) {
			Log.v(TAG, "Send DARKFRAME");
			sendDarkFrame();
		
		}
	}

	@SuppressWarnings("unused")
	private void saveColorCube() {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		int cubeSize = colorCubeLimit * colorCubeLimit * colorCubeLimit;
		Bitmap bmp = Bitmap.createBitmap(cubeSize, 400, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();
		int counter = 0;
		Log.v(TAG, "colorCubeMax: " + colorCubeMax);
		for (int r = 0; r < colorCubeLimit; r++) {
			for (int g = 0; g < colorCubeLimit; g++) {
				for (int b = 0; b < colorCubeLimit; b++) {
					int red = (int) (((double) r) / colorCubeLimit * 255);
					int green = (int) (((double) g) / colorCubeLimit * 255);
					int blue = (int) (((double) b) / colorCubeLimit * 255);
					int color = Color.argb(255, red, green, blue);
					paint.setColor(color);
					int y = (int) ((1 - (colorCube[r][g][b] / colorCubeMax)) * 400.0);
					canvas.drawLine(counter, y, counter, 400, paint);
					counter++;
				}
			}
		}
		String filename = "colorcube_" + System.currentTimeMillis() + ".png";
		CameraData.saveBitmap(bmp, filename);
		Log.v(TAG, filename);
	}

	@SuppressWarnings("unused")
	private void saveHistogram() {
		Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
		Bitmap bmp = Bitmap.createBitmap(256, 256, conf);
		Canvas canvas = new Canvas(bmp);
		Paint paint = new Paint();

		for (int i = 0; i < 3; i = i + 1) {
			for (int j = 0; j < 256; j = j + 1) {
				int color = Color.argb(255, (i == 0) ? 255 : 0, (i == 1) ? 255
						: 0, (i == 2) ? 255 : 0);
				paint.setColor(color);
				int y = (int) ((1 - (histogram[i][j] / histogramMax)) * 255.0);
				canvas.drawLine(j, y, j, 255, paint);
			}
		}
		String filename = "histogram_" + System.currentTimeMillis() + ".png";
		CameraData.saveBitmap(bmp, filename);
		Log.v(TAG, filename);
	}

	public static void saveBitmap(Bitmap bmp, String filename) {
		FileOutputStream fo = null;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		if (bmp != null) {
			bmp.compress(Bitmap.CompressFormat.PNG, 100, bytes);
			bmp.recycle();
			System.gc();
		}
		File imagesFolder = new File(Environment.getExternalStorageDirectory(),
				"Identification");
		if (!imagesFolder.exists())
			imagesFolder.mkdirs(); // <----
		File image = new File(imagesFolder, filename);
		// "acceleration_"+System.currentTimeMillis() + ".jpg");
		Log.v(TAG, "file: " + image.getAbsolutePath());
		// write the bytes in file
		try {
			fo = new FileOutputStream(image);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		}
		if (fo != null) {
			try {
				fo.write(bytes.toByteArray());
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

			// remember close de FileOutput
			try {
				fo.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
	}

	public void stop() {
		stopCamera = true;
		try {
			serviceCamera.reconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		serviceCamera.stopPreview();
		serviceCamera.release();
		serviceCamera = null;
	}

	private void stopRecording() {
		stopCamera = true;
		if (recordingStatus == true) {
			try {
				serviceCamera.reconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			serviceCamera.stopPreview();

			serviceCamera.release();
			serviceCamera = null;
		}
		// mediaRecorder.stop();
		// mediaRecorder.reset();
		// mediaRecorder.release();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public static Bitmap convertToMutable(Bitmap imgIn) {
		try {

			File file = new File(Environment.getExternalStorageDirectory()
					+ File.separator + "temp.tmp");

			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			int width = imgIn.getWidth();
			int height = imgIn.getHeight();
			Config type = imgIn.getConfig();
			FileChannel channel = randomAccessFile.getChannel();
			MappedByteBuffer map = channel.map(MapMode.READ_WRITE, 0,
					imgIn.getRowBytes() * height);
			imgIn.copyPixelsToBuffer(map);
			imgIn.recycle();
			imgIn = null;
			System.gc();
			imgIn = Bitmap.createBitmap(width, height, type);
			map.position(0);

			imgIn.copyPixelsFromBuffer(map);

			channel.close();
			randomAccessFile.close();

			file.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return imgIn;
	}
	
	
	
}

/*
 * 
 * public boolean takePicture(){ try { Log.v(TAG, "takePicture() called!");
 * Camera.Parameters params = serviceCamera.getParameters();
 * serviceCamera.setParameters(params); Camera.Parameters p =
 * serviceCamera.getParameters();
 * 
 * final List<Size> listSize = p.getSupportedPreviewSizes(); Size previewSize =
 * listSize.get(2); Log.v(TAG, "use: width = " + previewSize.width +
 * " height = " + previewSize.height); p.setPreviewSize(previewSize.width,
 * previewSize.height);
 * p.setPreviewFormat(ImageFormat.NV21);//PixelFormat.YCbCr_420_SP);
 * serviceCamera.setParameters(p); try {
 * serviceCamera.setPreviewDisplay(surfaceHolder); serviceCamera.startPreview();
 * } catch (IOException e) { Log.e(TAG, e.getMessage()); e.printStackTrace(); }
 * Log.v(TAG, "takePicture!"); serviceCamera.takePicture(null, null, mCall);
 * return true; } catch (IllegalStateException e) { Log.d(TAG, e.getMessage());
 * e.printStackTrace(); return false; } }
 * 
 * public boolean startRecording(){ try { //serviceCamera = Camera.open();
 * Camera.Parameters params = serviceCamera.getParameters();
 * serviceCamera.setParameters(params); Camera.Parameters p =
 * serviceCamera.getParameters();
 * 
 * final List<Size> listSize = p.getSupportedPreviewSizes(); Log.v(TAG,
 * "num sizes possible: " + listSize.size()); for (Size sizeItem : listSize) {
 * Log.v(TAG, "possible: width = " + sizeItem.width+ " height = " +
 * sizeItem.height); } Size previewSize = listSize.get(2); Log.v(TAG,
 * "use: width = " + previewSize.width + " height = " + previewSize.height);
 * p.setPreviewSize(previewSize.width, previewSize.height); Log.v(TAG,
 * " p.setPreviewFormat(ImageFormat.NV21);");
 * p.setPreviewFormat(ImageFormat.NV21);//PixelFormat.YCbCr_420_SP); Log.v(TAG,
 * " serviceCamera.setParameters(p);"); serviceCamera.setParameters(p);
 * Log.v(TAG, " serviceCamera.setPreviewDisplay(surfaceHolder);"); try {
 * serviceCamera.setPreviewDisplay(surfaceHolder); serviceCamera.startPreview();
 * } catch (IOException e) { Log.e(TAG, e.getMessage()); e.printStackTrace(); }
 * 
 * 
 * serviceCamera.unlock(); mediaRecorder = new MediaRecorder();
 * mediaRecorder.setCamera(serviceCamera);
 * mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
 * mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
 * mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
 * mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
 * mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
 * 
 * File sampleDir = Environment.getExternalStorageDirectory();
 * 
 * Log.v(TAG,sampleDir.getAbsolutePath()+"/video.mp4");
 * 
 * mediaRecorder.setOutputFile(sampleDir.getAbsolutePath()+"/video.mp4");
 * mediaRecorder.setVideoFrameRate(30);
 * mediaRecorder.setVideoSize(previewSize.width, previewSize.height);
 * mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
 * 
 * mediaRecorder.prepare(); mediaRecorder.start(); recordingStatus = true;
 * 
 * return true; } catch (IllegalStateException e) { Log.d(TAG, e.getMessage());
 * e.printStackTrace(); return false; } catch (IOException e) { Log.d(TAG,
 * e.getMessage()); e.printStackTrace(); return false; } }
 * 
 * Camera.PictureCallback mCall2 = new Camera.PictureCallback() {
 * 
 * @Override public void onPictureTaken(byte[] data, Camera camera) { // decode
 * the data obtained by the camera into a Bitmap Log.v(TAG, "Image taken!!");
 * 
 * bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
 * 
 * ByteArrayOutputStream bytes = new ByteArrayOutputStream(); if (bmp != null)
 * bmp.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
 * 
 * File imagesFolder = new File( Environment.getExternalStorageDirectory(),
 * "Identification"); if (!imagesFolder.exists()) imagesFolder.mkdirs(); //
 * <---- File image = new File(imagesFolder, System.currentTimeMillis() +
 * ".jpg"); Log.v(TAG, "file: "+image.getAbsolutePath()); // write the bytes in
 * file try { fo = new FileOutputStream(image); } catch (FileNotFoundException
 * e) { // TODO Auto-generated catch block } try {
 * fo.write(bytes.toByteArray()); } catch (IOException e) { // TODO
 * Auto-generated catch block }
 * 
 * // remember close de FileOutput try { fo.close();
 * 
 * } catch (IOException e) { // TODO Auto-generated catch block }
 * //Toast.makeText(getApplicationContext(),"Your Picture has been taken !",
 * Toast.LENGTH_LONG).show(); if (bmp != null) { bmp.recycle(); bmp = null;
 * System.gc(); } takingPicture=false;
 * 
 * } };
 */

