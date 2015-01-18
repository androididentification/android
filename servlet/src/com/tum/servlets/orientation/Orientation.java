package com.tum.servlets.orientation;



import java.sql.Connection;
import java.util.HashMap;

import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.orientation.OrientationItem;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;

public class Orientation {


		Connection connection;
		
		public Orientation() {
			connection = DataService.connect();
			
		}
		
		UserDevice userDevice =  new UserDevice();
		
		public ResultItem newRequest(String userID,String deviceID,OrientationItem item,byte[] compressed,HashMap<String,String> map){
			boolean requestOK = false;
			userDevice.user.setGid(userID);
			userDevice.device.setGid(deviceID);
			long uID = DataService.getIDfromDatabase(connection,userDevice.user);
			long dID = DataService.getIDfromDatabase(connection,userDevice.device);
			if(uID!=0&&dID!=0){
				userDevice.user.setId(uID);
				userDevice.device.setId(dID);
				requestOK = DataService.insert(connection,uID,item,compressed);
			}
			return new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
		}
		

		
		public void finish(){
			DataService.close(connection);
		}
	}

