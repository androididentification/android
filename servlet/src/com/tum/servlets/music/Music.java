package com.tum.servlets.music;



import java.sql.Connection;
import java.util.HashMap;

import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;

public class Music {


		Connection connection;
		
		public Music() {
			connection = DataService.connect();
			
		}
		
		UserDevice userDevice =  new UserDevice();
		
		public ResultItem newRequest(String userID,String deviceID,MusicItemList itemList,HashMap<String,String> map){
			boolean requestOK = false;
			userDevice.user.setGid(userID);
			userDevice.device.setGid(deviceID);
			long uID = DataService.getIDfromDatabase(connection,userDevice.user);
			long dID = DataService.getIDfromDatabase(connection,userDevice.device);
			if(uID!=0&&dID!=0){
				userDevice.user.setId(uID);
				userDevice.device.setId(dID);
				requestOK = DataService.insert(connection,uID,itemList);
			}
			return new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
		}
		

		
		public void finish(){
			DataService.close(connection);
		}
	}

