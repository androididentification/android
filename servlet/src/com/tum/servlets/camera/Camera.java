package com.tum.servlets.camera;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;

import javax.imageio.ImageIO;

import com.tum.ident.camera.CameraPixelList;
import com.tum.ident.camera.DarkFrame;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;

public class Camera {
	Connection connection;
	
	public Camera() {
		connection = DataService.connect();
	}
	
	UserDevice userDevice =  new UserDevice();
	
	
	public ResultItem newRequest(String userID,String deviceID,CameraPixelList pixelList,byte[] compressed,HashMap<String,String> map){
		boolean requestOK = false;
		userDevice.user.setGid(userID);
		userDevice.device.setGid(deviceID);
		long uID = DataService.getIDfromDatabase(connection,userDevice.user);
		long dID = DataService.getIDfromDatabase(connection,userDevice.device);
		if(uID!=0&&dID!=0){
			userDevice.user.setId(uID);
			userDevice.device.setId(dID);
			requestOK = DataService.insert(connection,dID,pixelList,compressed);	
		}

		return  new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
	}
	
	public ResultItem newRequest(String userID,String deviceID,DarkFrame darkFrame,HashMap<String,String> map){
		boolean requestOK = false;
		if(darkFrame.data!=null){
			InputStream in = new ByteArrayInputStream(darkFrame.data);
			try {
				darkFrame.image = ImageIO.read(in);
				if(darkFrame.image!=null){
					userDevice.user.setGid(userID);
					userDevice.device.setGid(deviceID);
					long uID = DataService.getIDfromDatabase(connection,userDevice.user);
					long dID = DataService.getIDfromDatabase(connection,userDevice.device);
					if(uID!=0&&dID!=0){
						userDevice.user.setId(uID);
						userDevice.device.setId(dID);
						requestOK = DataService.insert(connection,dID,darkFrame);	
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else{
			requestOK = true;
		}
		return new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
	}
	

	
	public void finish(){
		DataService.close(connection);
	}
}
