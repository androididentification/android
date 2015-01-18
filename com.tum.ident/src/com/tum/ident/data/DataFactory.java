package com.tum.ident.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.Semaphore;
import java.util.zip.GZIPOutputStream;

public class DataFactory {
	private transient static Semaphore itemSemaphore = new Semaphore(1);
	
	public static byte[] getData(Object object) {
		try {
			itemSemaphore.acquire();
		} catch (InterruptedException e1) {
		}

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out;
		byte[] data = new byte[0];
		try {
			out = new ObjectOutputStream(bytes);
			out.writeObject(object);
			out.close();
			data = bytes.toByteArray();
		} catch (IOException e) {
			data = new byte[0];
		} catch (Exception e) {
			data = new byte[0];
		} catch (StackOverflowError e) {
			data = new byte[0];
		}
		itemSemaphore.release();
		return data;
	}

	public static Object getObject(byte[] data) {
		try {
			itemSemaphore.acquire();
		} catch (InterruptedException e1) {
		}
		ByteArrayInputStream is = new ByteArrayInputStream(data);
		Object object = null;
		try {
			ObjectInputStream in = new ObjectInputStream(is);
			object = in.readObject();
			in.close();
		} catch (IOException i) {
			i.printStackTrace();
			object = null;
		} catch (ClassNotFoundException c) {
			System.out.println("Class not found");
			c.printStackTrace();
			object = null;
		} catch (ArrayIndexOutOfBoundsException a) {
			System.out.println("ArrayIndexOutOfBoundsException");
			a.printStackTrace();
			object = null;
		} catch (Exception c) {
			System.out.println("Exception");
			c.printStackTrace();
			object = null;
		} catch (StackOverflowError e) {
			object = null;
		}
		itemSemaphore.release();
		return object;
	}

	public static byte[] decompress(byte[] compressed) {

		byte[] uncompressed = null;
		if (compressed != null) {
			if (compressed.length > 2) {
				if (compressed[0] == 31) {
					java.io.ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(
							compressed);
					java.util.zip.GZIPInputStream gzin = null;

					try {
						gzin = new java.util.zip.GZIPInputStream(bytein);

						java.io.ByteArrayOutputStream byteout = new java.io.ByteArrayOutputStream();

						int res = 0;
						byte buf[] = new byte[1024];
						while (res >= 0) {
							res = gzin.read(buf, 0, buf.length);
							if (res > 0) {
								byteout.write(buf, 0, res);
							}
						}
						uncompressed = byteout.toByteArray();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return uncompressed;
	}

	public static byte[] compress(Object object) {
		byte[] data = DataFactory.getData(object);
		if (data != null) {
			return compress(data);
		}
		return null;
	}

	public static byte[] compress(byte[] data) {
		byte[] compressed = null;
		try {
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(
					data.length * 2);
			try {
				GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
				try {
					zipStream.write(data);
				} finally {
					zipStream.close();
				}
			} finally {
				byteStream.close();
			}

			compressed = byteStream.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return compressed;
	}

	public static Object decompressObject(byte[] compressed) {
		Object object = null;
		byte[] data = decompress(compressed);

		if (data != null) {
			object = getObject(data);

		}
		System.out.println("OBJECT: " + object);

		return object;
	}
	
}
