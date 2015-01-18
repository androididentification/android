package com.tum.ident.orientation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import com.tum.ident.realtime.RealtimeData;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class OrientationItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private static final String TAG = "OrientationData";

	private int rx = 40;
	private int ry = 20;
	private int rz = 20;

	private long[][] timecounter = new long[7][24];

	private long[][][][] orientationCounter;
	
	@SuppressWarnings("unused")
	private long counter = 0;

	private  transient boolean changed = true;

	OrientationItem() {
		this.counter = 0;
		this.orientationCounter = new long[rx][ry][rz][2];
	}

	static public int getMinutes() {
		Calendar calendar = Calendar.getInstance();
		int hours = calendar.get(Calendar.HOUR_OF_DAY);
		int minutes = calendar.get(Calendar.MINUTE);
		return hours * 60 + minutes;
	}

	public void add(float[] orientation) {
		int xindex = (int) (((orientation[0] / (Math.PI * 2)) + 0.5) * rx);
		int yindex = (int) (((orientation[1] / (Math.PI * 2)) + 0.5) * ry);
		int zindex = (int) (((orientation[2] / (Math.PI * 2)) + 0.5) * rz);
		if (xindex < 0) {
			xindex = rx + xindex;
		}
		if (yindex < 0) {
			yindex = ry + yindex;
		}
		if (zindex < 0) {
			zindex = rz + zindex;
		}
		if (xindex > rx) {
			xindex = xindex - rx;
		}
		if (yindex > ry) {
			yindex = yindex - ry;
		}
		if (zindex > rz) {
			zindex = zindex - rz;
		}
		Log.v(TAG, "INDEX: " + xindex + " ," + yindex + " , " + zindex);
		long c = orientationCounter[xindex][yindex][zindex][0];
		long t = orientationCounter[xindex][yindex][zindex][1];
		long time = getMinutes();
		t = (long) (((double) t * c + time) / (c + 1.0d));
		orientationCounter[xindex][yindex][zindex][1] = t;
		orientationCounter[xindex][yindex][zindex][0] = c + 1;
		counter++;
		changed = true;
	}
	
	public void sendRealtime(){
		
		for (int z = 0; z < ry; z++) {
			for (int x = 0; x < rx; x++) {
				for (int y = 0; y < ry; y++) {
					//int xindex = (int) (((orientation[0] / (Math.PI * 2)) + 0.5) * rx);
					//int yindex = (int) (((orientation[1] / (Math.PI * 2)) + 0.5) * ry);
					//int zindex = (int) (((orientation[2] / (Math.PI * 2)) + 0.5) * rz);
	
			
					String orientationString = String.format(
							Locale.getDefault(), "&ox2=%f&oy2=%f&oz2=%f",
							x,y,
							z);
					RealtimeData.sendNotification(":82/cube/"
							+ orientationString + "&");

				}

			}
		}
			
	
	}

	public long[][] getTimecounter() {
		return timecounter;
	}

	public void setTimecounter(long[][] timecounter) {
		this.timecounter = timecounter;
	}

	public Bitmap getImage(Bitmap bmp) {

		if (changed == true || bmp == null) {
			// PearsonsCorrelation corr = new PearsonsCorrelation();
			// double result = corr.correlation(x, y); //result = NaN.

			Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf
															// types

			double scale = 5;
			int width = (int) ((rx + rz / 1.4) * scale);
			int height = (int) ((ry + rz / 1.4) * scale);
			if (bmp == null) {
				bmp = Bitmap.createBitmap(width, height, conf);
			}
			Canvas canvas = new Canvas(bmp);
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawColor(Color.WHITE);
			paint.setStrokeWidth(1);
			double[][][] orientationBlur = new double[rx][ry][rz];
			double max = 0;
			for (int z = 0; z < ry; z++) {
				for (int x = 0; x < rx; x++) {
					for (int y = 0; y < ry; y++) {

						orientationBlur[x][y][z] = getCounterBlur(x, y, z);
						if (orientationBlur[x][y][z] > max) {
							max = orientationBlur[x][y][z];
						}

					}

				}
			}

			paint.setColor(Color.argb(255, 150, 150, 150));
			paint.setStrokeWidth(1);
			canvas.drawRect(0, height - 1 - ((int) (ry * scale)),
					(int) (rx * scale), height - 1, paint);

			canvas.drawLine((int) (rx * scale), height, width, height
					- (int) (rz * scale / 1.4), paint);

			canvas.drawLine(0, height - ((int) (ry * scale)), width
					- (int) (rx * scale), 0, paint);

			canvas.drawLine((int) (rx * scale), height - ((int) (ry * scale)),
					width, 0, paint);

			paint.setColor(Color.argb(255, 220, 220, 220));

			canvas.drawRect(width - 1 - (int) (rx * scale), 0, width - 1,
					(int) (ry * scale), paint);

			canvas.drawLine(0, height, (int) (rz * scale / 1.4), height
					- (int) (rz * scale / 1.4), paint);

			paint.setStyle(Paint.Style.FILL);
			for (int z = 0; z < rz; z++) {
				for (int x = 0; x < rx; x++) {
					for (int y = 0; y < ry; y++) {
						int xpos = 1 + (int) ((x + z / 1.4f) * scale);
						int ypos = (int) (height - 1 - ((y + z / 1.4f) * scale));
						int alpha = 1 + (int) ((orientationBlur[x][y][z] / max) * 254);
						int red = ((x * 255) / rx);
						int green = ((y * 255) / ry);
						int blue = ((z * 255) / rz);
						paint.setColor(Color.argb(alpha, red, green, blue));
						canvas.drawCircle(xpos, ypos, (int) (scale), paint);
						paint.setColor(Color.argb((int) (alpha * 0.5), red,
								green, blue));
						canvas.drawCircle(xpos, ypos, (int) (scale * 0.5),
								paint);
					}
				}
			}
			paint.setColor(Color.argb(80, 0, 0, 0));
			canvas.drawText("yaw", (int) (rz * scale / 1.4 * 0.5 - 10), height
					- (int) (rz * scale / 1.4 * 0.5 - 5), paint);
			canvas.drawText("pitch", 3, height - (int) (ry * scale - 15), paint);
			canvas.drawText("roll", (int) (rx * scale - 23), height - 6, paint);
			changed = false;
		}
		return bmp;
	}

	private double getCounterBlur(int xc, int yc, int zc) {
		double value = 0;
		double weightSum = 0;
		int s = 1;
		int sSum = s * 3;
		for (int z = zc - s; z <= zc + s; z++) {

			for (int x = xc - s; x <= xc + s; x++) {

				for (int y = yc - s; y <= yc + s; y++) {
					if (x >= 0 && y >= 0 && z >= 0) {
						if (x < rx && y < ry && z < rz) {
							double weight = 1.0
									- (Math.abs(x - xc) + Math.abs(y - yc) - Math
											.abs(z - zc)) / ((double) sSum);
							weight = (weight + 0.1) / 1.1;
							value = value + orientationCounter[x][y][z][0]
									* weight;
							weightSum = weightSum + weight;
						}
					}
				}
			}
		}
		if (weightSum > 0) {
			value = value / weightSum;
		}
		return value;
	}

}
