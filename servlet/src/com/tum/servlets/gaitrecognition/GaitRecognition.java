package com.tum.servlets.gaitrecognition;
import java.sql.Connection;
import java.util.HashMap;

import com.tum.ident.gaitrecognition.StepClusterList;
import com.tum.ident.gaitrecognition.StepStatistics;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultValueItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;


public class GaitRecognition {
	
		Connection connection;
		
		public GaitRecognition() {
			connection = DataService.connect();
		}
		
		UserDevice userDevice =  new UserDevice();
		
		
		public ResultItem newRequest(String userID,String deviceID,StepStatistics steps,byte[] compressed,HashMap<String,String> map){
			boolean requestOK = false;
			userDevice.user.setGid(userID);
			userDevice.device.setGid(deviceID);
			long uID = DataService.getIDfromDatabase(connection,userDevice.user);
			long dID = DataService.getIDfromDatabase(connection,userDevice.device);
			if(uID!=0&&dID!=0){
				userDevice.user.setId(uID);
				userDevice.device.setId(dID);
				requestOK = DataService.insert(connection,uID,steps,compressed);	
			}

			return  new ResultItem(ResultItem.Type.Value,new ResultValueItem(requestOK));
		}
		

		
		public void finish(){
			DataService.close(connection);
		}
	}

