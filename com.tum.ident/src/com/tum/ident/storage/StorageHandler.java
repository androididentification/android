package com.tum.ident.storage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.tum.ident.data.DataFactory;

public class StorageHandler {

	private static final String TAG = "StorageHandler";

	private static Context context;

	private transient static Semaphore storageSemaphore = new Semaphore(1);

	public static void setContext(Context c) {
		context = c;
	}

	public static Date lastModified(String path) {
		File file = new File(path);
		Date lastModDate = new Date(file.lastModified());
		return lastModDate;
	}

	public static FileOutputStream openFile(String path) {
		try {
			FileOutputStream fout = context.openFileOutput(path,
					Context.MODE_PRIVATE);
			if (fout != null) {
				return fout;
			}
		} catch (IOException e) {
			Log.v(TAG, "write failed: " + e.toString());
		}
		return null;
	}

	public static FileInputStream readFile(String path) {
		try {
			FileInputStream fin = context.openFileInput(path);
			if (fin != null) {
				return fin;
			}
		} catch (IOException e) {
			Log.v(TAG, "read failed: " + e.toString());
		}
		return null;
	}

	public static byte readByte(FileInputStream fin) {
		byte data = 0;
		try {
			DataInputStream dataStream = new DataInputStream(fin);
			if (dataStream.available() > 0) {
				data = dataStream.readByte();
			}
		} catch (IOException e) {
			Log.v(TAG, "read data failed: " + e.toString());
		}
		return data;
	}

	public static void closeFile(FileOutputStream fout) {
		try {
			fout.close();
		} catch (IOException e) {
			Log.v(TAG, "close failed: " + e.toString());
		}
	}

	public static void closeFile(FileInputStream fin) {
		try {
			fin.close();
		} catch (IOException e) {
			Log.v(TAG, "close failed: " + e.toString());
		}
	}

	public static void writeData(FileOutputStream fout, byte[] data) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(fout);
			dataOutputStream.write(data);
		} catch (IOException e) {
			Log.v(TAG, "write data failed: " + e.toString());
		}
	}

	public static void writeByte(FileOutputStream fout, byte data) {
		try {
			DataOutputStream dataOutputStream = new DataOutputStream(fout);
			dataOutputStream.write(data);
		} catch (IOException e) {
			Log.v(TAG, "write data failed: " + e.toString());
		}
	}

	public static void writeStringN(FileOutputStream fout, String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fout);
			BufferedWriter bwriter = new BufferedWriter(outputStreamWriter);
			bwriter.write(data);
			bwriter.newLine();
		} catch (IOException e) {
			Log.v(TAG, "write string failed: " + e.toString());
		}
	}

	public static void writeString(FileOutputStream fout, String data) {
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fout);
			outputStreamWriter.write(data);
		} catch (IOException e) {
			Log.v(TAG, "write string failed: " + e.toString());
		}
	}

	public static void writeToFile(String path, byte[] data) {
		try {
			FileOutputStream fout = context.openFileOutput(path,
					Context.MODE_PRIVATE);
			if (fout != null) {
				DataOutputStream dataOutputStream = new DataOutputStream(fout);
				dataOutputStream.write(data);
				fout.close();
			}
		} catch (IOException e) {
			Log.v(TAG, "File write failed: " + e.toString());
		}
	}

	public static boolean createDirectory(String path) {
		File folder = new File(path);
		boolean success = true;
		if (!folder.exists()) {
			success = folder.mkdir();
		}
		return success;
	}

	public static void compressObject(Object object, String path) {
		byte[] data = DataFactory.compress(object);
		if (data != null) {
			FileOutputStream file = openFile(path);
			if (file != null) {
				writeData(file, data);
				closeFile(file);
			}
		}
	}

	public static void saveObject(Object object, String path) {
		saveObject(object, path, true);
	}

	public static void saveObject(Object object, String path, boolean internal) {
		Log.v(TAG, "\nSave Object: " + path);
		FileOutputStream fileOut;
		try {
			storageSemaphore.acquire();
		} catch (InterruptedException e) {
		}
		try {
			if (internal == false) {
				fileOut = new FileOutputStream(path);// context.openFileInput(path);//
			} else {
				fileOut = context.openFileOutput(path, Context.MODE_PRIVATE);//
			}
			if (fileOut != null) {
				ObjectOutputStream out;
				out = new ObjectOutputStream(fileOut);
				out.writeObject(object);
				out.close();
				fileOut.close();
			}
			Log.v(TAG, "Object saved: " + path);
		} catch (FileNotFoundException e) {
			Log.v(TAG, "saveObject - FileNotFoundException", e);
		} catch (IOException e) {
			Log.v(TAG, "saveObject - IOException", e);
		} catch (Exception e) {
			Log.v(TAG, "saveObject - Exception", e);
		} catch (StackOverflowError e) {
			Log.v(TAG, "saveObject - StackOverflowError", e);
		}
		storageSemaphore.release();
	}

	public static Object loadObject(String path) { // TODO
		Log.v(TAG, "\nLoad Object: " + path);
		Object object = null;
		String baseDir = Environment.getExternalStorageDirectory()
				.getAbsolutePath();
		String path2 = baseDir + "/" + path;
		object = loadObject(path2, false);
		if (object == null) {
			object = loadObject(path, true);
		} else {
			saveObject(object, path);
			File file = new File(path2);
			if (file != null) {
				if (file.exists()) {
					file.delete();
				}
			}
		}
		return object;
	}

	public static Object loadObject(String path, boolean internal) {
		try {
			storageSemaphore.acquire();
		} catch (InterruptedException e) {
		}
		Log.v("DEBUG", "loadObject: " + path);
		Object object = null;
		try {

			FileInputStream fileIn = null;
			if (internal == false) {
				fileIn = new FileInputStream(path);// context.openFileInput(path);//
			} else {
				fileIn = context.openFileInput(path);//
			}
			if (fileIn != null) {
				ObjectInputStream in = new ObjectInputStream(fileIn);
				object = in.readObject();
				in.close();
				fileIn.close();
			}
			Log.v(TAG, "Object loaded: " + path);
		} catch (IOException i) {
			object = null;
			// Log.v(TAG,"loadObject - IOException",i);
		} catch (ClassNotFoundException c) {
			object = null;
			Log.v(TAG, "loadObject - ClassNotFoundException", c);
		} catch (ArrayIndexOutOfBoundsException a) {
			object = null;
			Log.v(TAG, "ArrayIndexOutOfBoundsException");
		} catch (Exception e) {
			object = null;
			Log.v(TAG, "loadObject - Exception", e);
		} catch (StackOverflowError e) {
			Log.v("DEBUG", "loadObject - StackOverflowError: " + path, e);
		}
		storageSemaphore.release();
		return object;
	}

	public static void writeToFile(String path, String data) {
		try {
			FileOutputStream fout = context.openFileOutput(path,
					Context.MODE_PRIVATE);
			if (fout != null) {
				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						fout);
				outputStreamWriter.write(data);
				outputStreamWriter.close();
				fout.close();
				Log.v(TAG, "OutputStreamWriter finished!");
			} else
				Log.v(TAG, "FileOutputStream == null");
		} catch (IOException e) {
			Log.v(TAG, "File write failed: " + e.toString());
		}

	}

	public static String readFromFile(String path) {

		String ret = "";

		try {
			InputStream inputStream = context.openFileInput(path);

			if (inputStream != null) {
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);
				String receiveString = "";
				StringBuilder stringBuilder = new StringBuilder();

				while ((receiveString = bufferedReader.readLine()) != null) {
					stringBuilder.append(receiveString);
				}

				inputStream.close();
				ret = stringBuilder.toString();
			}
		} catch (FileNotFoundException e) {
			Log.v(TAG, "File not found: " + e.toString());
		} catch (IOException e) {
			Log.v(TAG, "Can not read file: " + e.toString());
		}

		return ret;
	}

}
