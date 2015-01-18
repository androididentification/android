package com.tum.servlets;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;

import javax.servlet.http.HttpServletRequest;

import com.tum.servlets.camera.Camera;
import com.tum.servlets.files.Files;
import com.tum.servlets.gaitrecognition.GaitRecognition;
import com.tum.servlets.identification.Identification;
import com.tum.servlets.location.Location;
import com.tum.servlets.music.Music;
import com.tum.servlets.battery.Battery;
import com.tum.servlets.orientation.Orientation;
import com.tum.ident.battery.BatteryItemList;
import com.tum.ident.camera.CameraPixelList;
import com.tum.ident.camera.DarkFrame;
import com.tum.ident.files.FileItemList;
import com.tum.ident.gaitrecognition.*;
import com.tum.ident.identification.IdentificationItem;
import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.orientation.OrientationItem;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;



public class DataFactory {
	public enum DataType{
		PixelErrorFront,
		PixelErrorBack,
		DarkFrameFront,
		DarkFrameBack,
		Location,
		StepDetection,
		UserDevice,
		Music,
		File,
		Orientation,
		Battery,
		Spectrum
	}
	

	public static byte[] decompress(byte[] compressed) {
		byte [] uncompressed = null;
		if(compressed!=null){
			if(compressed.length > 2){
				if(compressed[0] == 31){
					java.io.ByteArrayInputStream bytein = new java.io.ByteArrayInputStream(compressed);
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

	
	public static byte[] decompress(InputStream bytein) {
		byte [] uncompressed = null;
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
		return uncompressed;
    }

	public static byte[] compress(byte[] data){
		byte[] compressed = null;
		try
	    {
	      ByteArrayOutputStream byteStream =
	        new ByteArrayOutputStream(data.length*2);
	      try
	      {
	        GZIPOutputStream zipStream  = new GZIPOutputStream(byteStream);
	        try
	        {
	          zipStream.write(data);
	        }
	        finally
	        {
	          zipStream.close();
	        }
	      }
	      finally
	      {
	        byteStream.close();
	      }

	      compressed = byteStream.toByteArray();
	    }
	    catch(Exception e)
	    {
	      e.printStackTrace();
	    }
		return compressed;
	}
	
	
	
	public static Object loadObject(byte[] compressed){
		Object object=null;
		byte[] data = decompress(compressed);
		if(data!=null){
			ByteArrayInputStream is = new ByteArrayInputStream(data);
			try
			{
				ObjectInputStream in = new ObjectInputStream(is);
				object =  in.readObject();
				in.close();
			}catch(IOException i)
			{
				i.printStackTrace();
				return null;
			}catch(ClassNotFoundException c)
			{
				//System.out.println("Class not found");
				c.printStackTrace();
				return null;
			}
		}
		//System.out.println("OBJECT: "+object);
		
		return object;
	}
	
	public static byte[] createObjectData(Object item){
		byte data[]= null;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bytes);
			out.writeObject(item);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data =  bytes.toByteArray();

		
		return compress(data);
		
	}
	
	public static byte[] createResultData(ResultItem resultItem){
		return createObjectData(resultItem);
	}
	
	
	
	private static HashMap<String,String> getQueryParts(HttpServletRequest request){
		String query = request.getQueryString(); 
		//System.out.println("query = "+query);
		String[] parts = query.split("&");
		HashMap<String,String> map = new HashMap<String,String>();
		for(int i = 0;i<parts.length;i++){
			String[] keyvalue = parts[i].split("=");
			if(keyvalue.length==2){
				//System.out.println(keyvalue[0] + " = "+keyvalue[1]);
				map.put(keyvalue[0], keyvalue[1]);
			}
		}
		return map;
	}
	

	
	
	
	public static byte[] streamToByteArray(InputStream is){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int reads;
		try {
			do{
				reads = is.read();
				baos.write(reads);
			}
			while(reads != -1);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	
	
	public static byte[] newRequest(HttpServletRequest request){
		ResultItem resultItem = new ResultItem();
		String requestURI = request.getRequestURI();
		HashMap<String,String> map = getQueryParts(request);
		String typeString = map.get("type");
		int typeValue = Integer.valueOf(typeString);
		if(typeValue>=0 && typeValue < DataType.values().length){
			String deviceID = map.get("deviceID");
			String userID   = map.get("userID");
			if(userID!=null && deviceID != null){
				if(userID.length()!=DataService.getHashLength() || deviceID.length()!=DataService.getHashLength()){
					userID = null;
					deviceID = null;
				}
			}
			DataType type = DataType.values()[typeValue];
			InputStream reader = null;
			try {
				reader = request.getInputStream();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(reader!=null){
				byte[] compressed = streamToByteArray(reader);
				
				if(type==DataType.StepDetection){
					StepStatistics steps = (StepStatistics) loadObject(compressed);
					if(steps!=null){
						GaitRecognition gaitRecognition = new GaitRecognition();
						resultItem = gaitRecognition.newRequest(userID,deviceID,steps,compressed,map);
				        gaitRecognition.finish();
					}
				}else if(type==DataType.PixelErrorFront || type==DataType.PixelErrorBack){
					CameraPixelList pixelList = (CameraPixelList)loadObject(compressed);
					if(pixelList!=null){
						Camera camera = new Camera();
						resultItem = camera.newRequest(userID,deviceID,pixelList,compressed,map);
						camera.finish();
					}
				}
				else if(type==DataType.DarkFrameFront || type==DataType.DarkFrameBack){
					DarkFrame darkFrame = (DarkFrame)loadObject(compressed);
					if(darkFrame!=null){
						Camera camera = new Camera();
						resultItem = camera.newRequest(userID,deviceID,darkFrame,map);
						camera.finish();
					}
				}
				else if(type==DataType.Location){
					LocationAreaList itemList = (LocationAreaList)loadObject(compressed);
					if(itemList!=null){
						Location location = new Location();
						resultItem = location.newRequest(userID,deviceID,itemList,compressed,map);
						location.finish();
					}
				}
				else if(type==DataType.UserDevice){
					IdentificationItem identificationItem = (IdentificationItem)loadObject(compressed);
					if(identificationItem!=null){
						
						////System.out.println("DEVICE PACKAGES: "+identificationItem.userDevice.device.getPackageList().size());
						
						Identification identification = new Identification();
						resultItem = identification.newRequest(identificationItem,map);
						identification.finish();
					}
				}
				else if(type==DataType.Music){
					MusicItemList itemList = (MusicItemList)loadObject(compressed);
					if(itemList!=null){
						Music music = new Music();
						//System.out.println("MusicItemList - Size: "+itemList.size());
						resultItem = music.newRequest(userID,deviceID,itemList,map);
						music.finish();
					}
				}
				else if(type==DataType.File){
					FileItemList itemList = (FileItemList)loadObject(compressed);
					if(itemList!=null){
						Files files = new Files();
						resultItem = files.newRequest(userID,deviceID,itemList,map);
						files.finish();
					}
				}
				else if(type==DataType.Battery){
					BatteryItemList itemList = (BatteryItemList)loadObject(compressed);
					if(itemList!=null){
						Battery battery = new Battery();
						resultItem = battery.newRequest(userID,deviceID,itemList,compressed,map);
						battery.finish();
					}
				}
				else if(type==DataType.Orientation){
					OrientationItem item = (OrientationItem)loadObject(compressed);
					if(item!=null){
						Orientation orientation = new Orientation();
						resultItem = orientation.newRequest(userID,deviceID,item,compressed,map);
						orientation.finish();
					}
				}
				else if(type==DataType.Spectrum){
					//TODO
					resultItem = new ResultItem(ResultItem.Type.Value,new ResultValueItem(true));
				}
				System.out.println("Request: "+type);
			}
		}
		
		byte[] result = createResultData(resultItem);
		//System.out.println("ResultItem.type:"+resultItem.type);
		//System.out.println("ResultItem compressed:"+result.length);
		return result;
	}
	
}
