package com.tum.ident.spectrum;

import java.io.Serializable;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.tum.ident.fastdtw.dtw.FastDTW;
import com.tum.ident.fastdtw.dtw.TimeWarpInfo;
import com.tum.ident.fastdtw.timeseries.TimeSeries;
import com.tum.ident.fastdtw.util.DistanceFunction;
import com.tum.ident.fastdtw.util.DistanceFunctionFactory;

import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;



public class SpectrumItem implements Serializable {
	private  static final long serialVersionUID = 1L;
	private final int radius = 8;
    transient private static final String TAG = "SpectrumData";

	private  double[] spectrum = null;
	private  double[] variance = null;
	private  double[] signum = null;

	private  long weight = 1;

	private  double minSpectrum, maxSpectrum;
	private  double minVariance, maxVariance;
	private  double minSpecVar, maxSpecVar;

	private  transient  boolean changed = true;

	public SpectrumItem(double[] spectrum, double[] variance, double[] signum) {
		this.spectrum = spectrum;
		this.variance = variance;
		this.signum = signum;
		for (int i = 0; i < spectrum.length; i++) {
			if (minSpectrum < spectrum[i] || i == 0) {
				minSpectrum = spectrum[i];
			}
			if (maxSpectrum > spectrum[i] || i == 0) {
				maxSpectrum = spectrum[i];
			}
			if (minVariance < variance[i] || i == 0) {
				minVariance = variance[i];
			}
			if (maxVariance > variance[i] || i == 0) {
				maxVariance = variance[i];
			}
			if (minSpecVar < spectrum[i] - variance[i] || i == 0) {
				minSpecVar = spectrum[i] - variance[i];
			}
			if (maxSpecVar > spectrum[i] + variance[i] || i == 0) {
				maxSpecVar = spectrum[i] + variance[i];
			}
		}
		changed = true;

	}


	
	public int getRadius() {
		return radius;
	}

	public double[] getSpectrum() {
		return spectrum;
	}

	public double[] getVariance() {
		return variance;
	}

	public double[] getSignum() {
		return signum;
	}


	public void setWeight(long weight) {
		this.weight = weight;
	}


	public double dwtDistance(SpectrumItem spectrumItem) {
		double result = -1;
		if (spectrum != null) {
			result = dwtDistance(this, spectrumItem, radius);
			if (result > -1) {
			}
		}
		return result;
	}

	
	
	public static double distance(SpectrumItem spectrumItem1, SpectrumItem spectrumItem2){
		double result = 0;
		PearsonsCorrelation corr = new PearsonsCorrelation();
		result = corr.correlation(spectrumItem1.spectrum,spectrumItem2.spectrum);
		Log.v(TAG,"PearsonsCorrelation: "+result);
		return result;
	}
	
	
	public void merge(SpectrumItem spectrumItem){
		for (int i = 0; i < spectrum.length; i++) {
			spectrum[i] = (spectrum[i]*weight + spectrumItem.spectrum[i])/(weight+1);
			variance[i] = (variance[i]*weight + spectrumItem.variance[i])/(weight+1);
			signum[i]   = (signum[i]*weight   + spectrumItem.signum[i])/(weight+1);
		}
		weight++;
	}
	
	public static double dwtDistance(SpectrumItem spectrumItem1,
			SpectrumItem spectrumItem2, int radius) {
		double result = -1;
		if (spectrumItem1.spectrum != null && spectrumItem2.spectrum != null) {
			final TimeSeries tsI = new TimeSeries(spectrumItem1.spectrum);
			final TimeSeries tsJ = new TimeSeries(spectrumItem2.spectrum);
			final DistanceFunction distFn;
			distFn = DistanceFunctionFactory
					.getDistFnByName("ManhattanDistance");
			final TimeWarpInfo info = FastDTW.getWarpInfoBetween(tsI, tsJ,
					radius, distFn);
			result = info.getDistance();
		}
		return result;
	}

	public long getWeight(){
		return weight;
	}
	
	
	public Bitmap getImage(SpectrumItem lastItem, Bitmap bmp) {
		if (changed || bmp == null || lastItem != this) {
			Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf
															// types
			if (bmp == null) {
				bmp = Bitmap.createBitmap(SpectrumData.blockSize * 6,
						SpectrumData.blockSize * 6, conf);
			}
			Canvas canvas = new Canvas(bmp);
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawColor(Color.WHITE);

			paint.setStrokeWidth(4);
			int red = 0;// (int)(rand.nextFloat()*128)+127;
			int green = 0;// (int)(rand.nextFloat()*128)+127;
			int blue = 255;// (int)(rand.nextFloat()*128)+127;
			int alpha = 180;
			int color = Color.argb(alpha, red, green, blue);
			paint.setColor(color);
			long oldy = -1;
			for (int v = 0; v < 3; v++) {
				for (int x = 0; x < variance.length; x++) {
					double varianceY = 0;
					int y = 0;
					if (v == 0) {
						varianceY = spectrum[x] - variance[x];
						y = (int) (((varianceY - minSpecVar) / (maxSpecVar - minSpecVar)) * (SpectrumData.blockSize - 5));
					} else {
						varianceY = spectrum[x] + variance[x];
						y = 8 + (int) (((varianceY - minSpecVar) / (maxSpecVar - minSpecVar)) * (SpectrumData.blockSize - 5));
					}

					long ypos = y * 6;
					if (ypos < 0) {
						ypos = 0;
					}
					if (ypos >= bmp.getHeight() - 1) {
						ypos = bmp.getWidth() - 1;
					}
					long xpos1 = (x - 1) * 6 - 1;
					long xpos2 = x * 6 + 1;
					if (xpos1 < 0) {
						xpos1 = 0;
					}
					if (xpos2 > bmp.getWidth() - 1) {
						xpos2 = bmp.getWidth() - 1;
					}

					if (oldy != -1) {
						canvas.drawLine(xpos1, oldy, xpos2, ypos, paint);
					}
					oldy = ypos;
				}
			}

			paint.setStrokeWidth(10);

			oldy = -1;
			color = Color.argb(255, 255, 0, 0);
			paint.setColor(color);
			for (int x = 0; x < spectrum.length; x++) {
				double spectrumY = spectrum[x];
				int y = 4 + (int) (((spectrumY - minSpecVar) / (maxSpecVar - minSpecVar)) * (SpectrumData.blockSize - 5));

				long ypos = y * 6;
				if (ypos < 0) {
					ypos = 0;
				}
				if (ypos >= bmp.getHeight() - 1) {
					ypos = bmp.getWidth() - 1;
				}
				long xpos1 = (x - 1) * 6 - 1;
				long xpos2 = x * 6 + 1;
				if (xpos1 < 0) {
					xpos1 = 0;
				}
				if (xpos2 > bmp.getWidth() - 1) {
					xpos2 = bmp.getWidth() - 1;
				}

				if (oldy != -1) {
					canvas.drawLine(xpos1, oldy, xpos2, ypos, paint);
				}
				oldy = ypos;
			}
			changed = false;
		}
		return bmp;
	}

}
