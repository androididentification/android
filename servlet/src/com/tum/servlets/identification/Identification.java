package com.tum.servlets.identification;



import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.tum.ident.device.DeviceMatch;
import com.tum.ident.identification.IdentificationItem;
import com.tum.ident.result.ResultIDItem;
import com.tum.ident.result.ResultItem;
import com.tum.ident.result.ResultMatchItem;
import com.tum.ident.user.User;
import com.tum.ident.user.UserMatch;
import com.tum.ident.userdevice.UserDevice;
import com.tum.servlets.DataService;


public class Identification {

	UserDevice userDevice =  new UserDevice();
	Connection connection;
			
	public Identification() {
		connection = DataService.connect();
	}
	
	public void finish(){
		DataService.close(connection);
	}
	
	
	/*
	public MatchList calculateMatches(String id){
		MatchList matchList = calculateMatches(id,methods,type); // returns a list of matching users or devices
		for(Match match : matchList.getList()){ //matching user or device
			for(Method method : match.getMethods()){ //dentification method 
				if(method.getMatch()==0)
				{
					if(method.getFalseNegativeRate()==0){ //for example hardware properties
						match.setMatch(0); //no match;
						break;
					}
				}
				else if(method.getMatch()==1)
					if(method.getFalsePositiveRate()==0){ //for example user accounts 
						match.setMatch(1); //match
						break;
					}
				}
			}
			if(!match.isMatchAlreadySet())
				match.initMatch();
				for(Method method : match.getMethods()){
					match.updateMatch(method.getMatch(),method.getWeight()); //the results of each method are weighted and combined
				}
				match.normalize(); //match between 0 and 1
			}
			IdentificationResult result= match.getResult();
			for(Method method : match.getMethods()){
				Method.adjustWeights(result,method);//the weight of the method gets updated
			}
		}
		return matchList;
	}
	
	
	
	public calculateMatch(String id1, String id2){
		double match = 0;
		double weight = 0;
		double[] m = new double[methods.size()];
		boolean done= false;
		for(Method method : methods){
			if(method.isAvailable() && method.isActive()){
				if( method.getFalseNegativeRate()==0){
					m[method.i()] = method.compare(id1,id2);
					if(m[method.i()]==0){
						match = 0;weight = 1;done=true;
						break;
					}
					
				} 
				else if(method.getFalsePositiveRate()==0){
					m[method.i()] = method.compare(id1,id2);
					if(m[method.i()]==1){
						match = 1;weight = 1;done=true;
						break;
					}
				}
				
		if(!done){
			for(Method method : methods){
				if(method.isAvailable() && method.isActive()){
					m[method.i()] = method.compare(id1,id2);
					match  = match + m[method.i()]*method.weight();
					weight = weight+method.weight();
				}
			}
		}
		match = match/weight;
		for(Method method : methods){
			if(method.isAvailable()){
				method.adjustWeight(m[method.i()],match);
			}
		}
	}


	
	private double compareSets(IdentificationSet s1, IdentificationSet s2){
		double sum = 0, weight = 0;
		ArrayList<SetItem> union        = s1.copyList().addAll(s2.copyList());
		ArrayList<SetItem> intersection = s1.copyList().retainAll(s2.copyList());
		for(SetItem i : union){
			sum = sum + i.weight();
		}
		if(sum>0){
			for(SetItem i : intersection){
				weight = weight + i.weight();
			}
			return weight/sum;
		}
		else{
			return 1;
		}
	}
	*/
	
	public void updateDatabase()
	{
		ArrayList<Match> packageMatches =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.Package);
		ArrayList<Match> accountMatches =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.Account);
		
		ArrayList<Match> contactMatches =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.Contact);

		ArrayList<Match> calllogMatches =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.CallLog);

		ArrayList<Match> bluetoothMatches =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.WLAN);
		
		ArrayList<Match> wlanMatches      =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.Bluetooth);
	
		ArrayList<Match> userMatches      =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.User);
		
		ArrayList<Match> deviceMatches       =  DataService.getSimilarIDs(connection,userDevice,DataService.DataType.Device);
		
		HashMap<Long,DeviceMatch> deviceMap  = new HashMap<Long,DeviceMatch>();
		HashMap<Long,UserMatch> userMap      = new HashMap<Long,UserMatch>();
		
		//System.out.println("Matching IDs:");
		
		//System.out.println("bluetoothMatches--");
		for(Match m : bluetoothMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setBluetoothMatch(m.getMatch());
			userMatch.setBluetoothSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : wlanMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setWlanMatch(m.getMatch());
			userMatch.setWlanSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : packageMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setPackageMatch(m.getMatch());
			userMatch.setPackageSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
			
		}
		for(Match m : accountMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setAccountMatch(m.getMatch());
			userMatch.setAccountSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : contactMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setContactMatch(m.getMatch());
			userMatch.setContactSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : calllogMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setCallLogMatch(m.getMatch());
			userMatch.setCallLogSignificance(m.getSignificance());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : userMatches){
			UserMatch userMatch = userMap.get(m.getId());
			if(userMatch==null){
				userMatch  = new UserMatch();
			}
			userMatch.setId(m.getId());
			userMatch.setUserNameMatch(m.getMatch());
			userMap.put(m.getId(),userMatch);
		}
		for(Match m : deviceMatches){
			DeviceMatch deviceMatch = deviceMap.get(m.getId());
			if(deviceMatch==null){
				deviceMatch  = new DeviceMatch();
			}
			deviceMatch.setId(m.getId());
			deviceMatch.setMatch(m.getMatch());
			deviceMatch.setProperties(m.getProperties());
			deviceMap.put(m.getId(),deviceMatch);
		}
		DataService.insertDeviceIdentification(connection,deviceMap,userDevice.device.getId());
		DataService.insertUserIdentification(connection,userMap,userDevice.user.getId());
	}
	
	public ResultIDItem insertData(IdentificationItem identificationItem){
		userDevice.prepareData(true);
		DataService.insert(connection,userDevice);
		updateDatabase();
		//System.out.println();
		//System.out.println("DeviceGid:  "+userDevice.getDeviceGid());
		//System.out.println("UserGid:  "+userDevice.getUserGid());
		ResultIDItem resultItem = new ResultIDItem(ResultIDItem.Type.Insert,userDevice.getDeviceGid(),userDevice.getUserGid());
		return resultItem;
	}
	
	public ResultIDItem updateData(IdentificationItem identificationItem){
		String deviceID = identificationItem.deviceID;
		String userID   = identificationItem.userID;
		//System.out.println("update deviceID: "+deviceID+" - "+deviceID.length());
		//System.out.println("update userID: "+userID+" - "+userID.length());
		if(userID!=null && deviceID != null){
			if(userID.length()==DataService.getHashLength() && deviceID.length()==DataService.getHashLength()){
				userDevice.user.setGid(userID);
				userDevice.device.setGid(deviceID);
				long uID = DataService.getIDfromDatabase(connection,userDevice.user);
				long dID = DataService.getIDfromDatabase(connection,userDevice.device);
				if(uID!=0&&dID!=0){
					userDevice.user.setId(uID);
					userDevice.device.setId(dID);
					updateDatabase();
	
					
					long time = java.lang.System.currentTimeMillis();
					

					boolean updateSuccesfull = DataService.update(connection,userDevice);

					//System.out.println("SQLService.update(connection,ud); finished!");
					//System.out.println((java.lang.System.currentTimeMillis()-time)+" ms");
					//System.out.println();
					//System.out.println("DeviceGid:  "+userDevice.getDeviceGid());
					//System.out.println("UserGid:  "+userDevice.getUserGid());
					if(updateSuccesfull){
						ResultIDItem resultItem = new ResultIDItem(ResultIDItem.Type.Update,userDevice.getDeviceGid(),userDevice.getUserGid());
						return resultItem;
					}
					else{
						ResultIDItem resultItem = new ResultIDItem(ResultIDItem.Type.Insert,userDevice.getDeviceGid(),userDevice.getUserGid());
						return resultItem;
					}
				}
			}
		}
		ResultIDItem resultItem = new ResultIDItem(ResultIDItem.Type.Error,"","");
		return resultItem;

	}
	
	public ResultMatchItem compareData(IdentificationItem identificationItem){
		ResultMatchItem result = null;
		//System.out.println("compareData(HttpServletRequest request)");
		String deviceID = identificationItem.deviceID;
		String userID   = identificationItem.userID;
		//System.out.println("deviceID: "+deviceID);
		//System.out.println("userID: "+userID);
		
		if(userID!=null && deviceID != null){
			//System.out.println("userID.length(): "+userID.length());
			//System.out.println("deviceID.length(): "+deviceID.length());
			if(userID.length()==DataService.getHashLength() && deviceID.length()==DataService.getHashLength()){
				result = DataService.getIdentificationData(connection,deviceID,userID);
				return result;
			}
		}
		return result;
	}
	
	
	public ResultItem newRequest(IdentificationItem identificationItem,HashMap<String,String> map){
		ResultItem result = new ResultItem();
		String method = identificationItem.method;
		userDevice = identificationItem.userDevice;
		//System.out.println("identificationItem.userID: "+identificationItem.userID);
		//System.out.println("identificationItem.deviceID: "+identificationItem.deviceID);
		
		
		if(method!=null){
			//System.out.println("Method: "+method);
			if(method.equals("add")){
				System.out.println("add");
				//System.out.println("ADD REQUEST");
				if(identificationItem.userID.equals("") || identificationItem.deviceID.equals("")){
					//System.out.println("add (1)");
					ResultIDItem resultIDs = insertData(identificationItem);
					result = new ResultItem(ResultItem.Type.ID,resultIDs);
				}
				else
				{
					long uID = DataService.getIDfromDatabase(connection,userDevice.user);
					long dID = DataService.getIDfromDatabase(connection,userDevice.device);
					if(uID==0||dID==0){
						//System.out.println("add (2)");
						ResultIDItem resultIDs = insertData(identificationItem);
						result = new ResultItem(ResultItem.Type.ID,resultIDs);
					}
					else{
						//System.out.println("update (1)");
						//System.out.println("UPDATE REQUEST");
						ResultIDItem resultIDs = updateData(identificationItem);
						result = new ResultItem(ResultItem.Type.ID,resultIDs);
					}
				}
			}
			else if(method.equals("update"))
			{
				System.out.println("update");
				//System.out.println("UPDATE REQUEST");
				ResultIDItem resultIDs = updateData(identificationItem);
				if(resultIDs.getType()==ResultIDItem.Type.Error){
					//System.out.println("ADD REQUEST");
					resultIDs = insertData(identificationItem);
					
				}
				result = new ResultItem(ResultItem.Type.ID,resultIDs);
			}
			else if(method.equals("compare"))
			{
				System.out.println("compare");
				//System.out.println("COMPARE REQUEST");
				ResultMatchItem resultMatch = compareData(identificationItem);
				result = new ResultItem(ResultItem.Type.Match,resultMatch);
			}
		}
		return result;
	}

}
