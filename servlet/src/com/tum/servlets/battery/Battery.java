package com.tum.servlets.battery;



import java.sql.Connection;
import java.util.HashMap;

import com.tum.ident.battery.BatteryItemList;
import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;

public class Battery {


		Connection connection;
		
		public Battery() {
			connection = DataService.connect();
			
		}
		
		UserDevice userDevice =  new UserDevice();
		
		public ResultItem newRequest(String userID,String deviceID,BatteryItemList itemList,byte[] compressed,HashMap<String,String> map){
			//System.out.println("FileItemList newRequest!");
			boolean requestOK = false;
			userDevice.user.setGid(userID);
			userDevice.device.setGid(deviceID);
			long uID = DataService.getIDfromDatabase(connection,userDevice.user);
			long dID = DataService.getIDfromDatabase(connection,userDevice.device);
			//System.out.println("uID: "+uID+" dID"+dID);
			if(uID!=0&&dID!=0){
				userDevice.user.setId(uID);
				userDevice.device.setId(dID);
				requestOK = DataService.insert(connection,dID,uID,itemList,compressed);
			}
			return new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
		}
		

		public void finish(){
			DataService.close(connection);
		}
	}

