package com.tum.ident.spectrum;


import java.util.concurrent.Semaphore;

import android.graphics.Bitmap;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.util.Log;


import com.tum.ident.data.DataItem;
import com.tum.ident.fftpack.RealDoubleFFT;
import com.tum.ident.storage.StorageHandler;

public class SpectrumData {

	// private static final String TAG = "SpectrumData";
	
	public final static int blockSize = 256;
	
	private int frequency = 44100;
	private int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
	private int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

	private AudioRecord audioRecord;
	private RealDoubleFFT transformer;
	
	private boolean started = false;

	private RecordAudio recordTask;

	private double[] spectrum;
	private double[] variance;
	private double[] signum;
	private double spectrumWeight = 0;
	private long recordingTimer = 0;
	private long recordingTime = 5000;

	private Bitmap bmp = null;
	private SpectrumItem bitmapItem = null;

	private SpectrumItemList spectrumItems = new SpectrumItemList();
	private Semaphore spectrumSemaphore = new Semaphore(1);

	public SpectrumData() {
		transformer = new RealDoubleFFT(blockSize);
	}

	public Bitmap getSpectrumImage2() {
		if (spectrumItems.getList() != null) {
			long maxWeight = -1;

			SpectrumItem maxItem = null;
			for (SpectrumItem sItem : spectrumItems.getList()) {
				if (sItem.getWeight() > maxWeight) {
					maxWeight = sItem.getWeight();
					maxItem = sItem;
				}
			}
			if (maxItem != null) {
				bmp = maxItem.getImage(bitmapItem, bmp);
				bitmapItem = maxItem;
				return bmp;
			} else {
				return null;
			}
		}
		return null;
	}
	
	public Bitmap getSpectrumImage(int index) {
		if (spectrumItems.getList() != null) {
			if (spectrumItems.getList().size() > 0) {
				lock();
				index = (index) % spectrumItems.getList().size();
				SpectrumItem newBitmapItem = spectrumItems.getList().get(index);
				bmp = newBitmapItem.getImage(bitmapItem, bmp);
				bitmapItem = newBitmapItem;
				unlock();
			}
		}
		return bmp;
	}
	

	public DataItem getDataItem() {
		if (spectrumItems != null) {
			return new DataItem("", spectrumItems);
		}
		return null;
	}

	public void lock() {
		try {
			spectrumSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void unlock() {
		spectrumSemaphore.release();
	}
	
	private class RecordAudio extends AsyncTask<Void, double[], Void> {

		@Override
		protected Void doInBackground(Void... params) {

			if (isCancelled()) {
				return null;
			}

			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					channelConfiguration, audioEncoding);
			audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
					frequency, channelConfiguration, audioEncoding, bufferSize);
			int bufferReadResult;
			short[] buffer = new short[blockSize];
			double[] toTransform = new double[blockSize];
			spectrum = new double[blockSize];
			variance = new double[blockSize];
			signum = new double[blockSize];
			spectrumWeight = 0;

			try {
				audioRecord.startRecording();
			} catch (IllegalStateException e) {
				Log.e("Recording failed", e.toString());

			}
			recordingTimer = System.currentTimeMillis();

			while (System.currentTimeMillis() - recordingTimer < recordingTime) {

				bufferReadResult = audioRecord.read(buffer, 0, blockSize);

				if (isCancelled())
					break;

				for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
					toTransform[i] = buffer[i] / 32768.0; // signed 16 bit
				}

				transformer.ft(toTransform);

				for (int i = 1; i < toTransform.length; i++) {

					double spectrumValue = Math.abs(toTransform[i]);

					spectrum[i] = (spectrum[i] * spectrumWeight + spectrumValue)
							/ (spectrumWeight + 1);

					double signumValue = Math.signum(toTransform[i]);

					signum[i] = (signum[i] * spectrumWeight + signumValue)
							/ (spectrumWeight + 1);

					double varianceValue = spectrumValue - spectrum[i];
					varianceValue = varianceValue * varianceValue;
					variance[i] = (variance[i] * spectrumWeight + varianceValue)
							/ (spectrumWeight + 1);
				}
				spectrumWeight++;

				publishProgress(toTransform);
				if (isCancelled())
					break;
				// return null;
			}

			if(isConstantSound(spectrum,variance)){
				SpectrumItem item = new SpectrumItem(spectrum, variance, signum);
				spectrumItems.add(item);
			}
		

			return null;
		}

		@Override
		protected void onProgressUpdate(double[]... toTransform) {

		}

		@Override
		protected void onPostExecute(Void result) {
			try {
				audioRecord.stop();
			} catch (IllegalStateException e) {
				Log.e("Stop failed", e.toString());

			}
			recordTask.cancel(true);
			started = false;
			// }
		}

	}

	public boolean isConstantSound(double[] spectrum,double[] variance){
		//todo
		return true;
	}
	
	public void start() {
		if (started == false) {
			started = true;
			recordTask = new RecordAudio();
			recordTask.execute();
		}
	}

	public void stop() {
		if (started == true) {
			started = false;
			recordTask.cancel(true);
			// recordTask = null;
		}
	}

	public void onStop() {

		recordTask.cancel(true);

	}
	

	public void load() {

		String fileName = "spectrum.ser";

		spectrumItems = null;
		spectrumItems = (SpectrumItemList) StorageHandler.loadObject(fileName);
		if (spectrumItems == null) {
			spectrumItems = new SpectrumItemList();
		}

	}
	
	public void save() {
		
		String fileName = "spectrum.ser";

		StorageHandler.saveObject(spectrumItems, fileName);

	}


}
