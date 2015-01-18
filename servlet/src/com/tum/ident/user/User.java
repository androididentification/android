package com.tum.ident.user;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;

import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.userdevice.PackageItem;
import com.tum.ident.userdevice.WLANItem;
import com.tum.servlets.DataFactory;
import com.tum.servlets.DataService;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String gid;
	private long id = 0;

	private String userName;
	private ArrayList<ContactItem> contactList     = new ArrayList<ContactItem>();
	private ArrayList<CallLogItem> callLogList     = new ArrayList<CallLogItem>();
	private ArrayList<BluetoothItem> bluetoothList = new ArrayList<BluetoothItem>(); 
	private ArrayList<WLANItem> wlanList           = new ArrayList<WLANItem>();
	private ArrayList<PackageItem> packageList     = new ArrayList<PackageItem>();
	private ArrayList<AccountItem> accountList     = new ArrayList<AccountItem>();
	private ArrayList<String> phoneNumberList      = new ArrayList<String>();
	private ArrayList<SIMItem> simList             = new ArrayList<SIMItem>();
	private ArrayList<String> carrierList          = new ArrayList<String>();

	public void setId(long id) {
		this.id = id;
	}
	public String getUserName() {
		return userName;
	}
	public void setGid(String gid) {
		this.gid = gid;
	}
	public String getGid() {
		return gid;
	}
	public long getId() {
		return id;
	}
	public ArrayList<String> getCarrierList() {
		return carrierList;
	}
	public ArrayList<SIMItem> getSIMList() {
		return simList;
	}
	public ArrayList<AccountItem> getAccountList() {
		return accountList;
	}
	public ArrayList<ContactItem> getContactList() {
		return contactList;
	}
	public ArrayList<CallLogItem> getCallLogList() {
		return callLogList;
	}
	public ArrayList<BluetoothItem> getBluetoothList() {
		return bluetoothList;
	}
	public ArrayList<WLANItem> getWLANList() {
		return wlanList;
	}
	public ArrayList<PackageItem> getPackageList() {
		return packageList;
	}
	public ArrayList<String> getPhoneNumberList() {
		return phoneNumberList;
	}
	private String getUserNameFromAccount(String accountName){
		String name = accountName;
		if(accountName.equals("Sync") || accountName.equals("Office") || accountName.equals("WhatsApp")){
			name = "";
		}else
		{
			accountName = accountName+"@";
			String[] parts = accountName.split("@");
			if(parts.length >= 1){
				name = parts[0];
			    name = name.replaceAll("\\."," ");
			    name = name.replaceAll("_"," ");
			    name = name.replaceAll("-"," ");
			    name = name.replaceAll("	"," ");
			    name = name.toLowerCase();
			    if(name.indexOf(" ")==-1){
			    	name = "";
			    }
			}
		}
		return name; 
	}
	
	private void extractUserName(){
		String name = "";
	    HashMap<String,Long> map = new HashMap<String,Long>();
		for (AccountItem accountItem : accountList) {
			String extracted = getUserNameFromAccount(accountItem.getName());
			if(extracted.length()>0){
				Long counter = map.get(extracted);
				if(counter==null){
					map.put(extracted, Long.valueOf(0));
				}
				else{
					map.put(extracted,counter+1);
				}
			}
		}
		long max = -1;
		Iterator<Map.Entry<String,Long>> it = map.entrySet().iterator();
	    while (it.hasNext()) {
	    	Map.Entry<String,Long> pairs = (Map.Entry<String,Long>)it.next();
	        if(max == -1 || pairs.getValue().longValue() > max){
	        	max = pairs.getValue().longValue();
	        	name = pairs.getKey();
	        }
	        it.remove(); 
	    }
		if(name!=null){
			if(name.length()>0){
				StringBuilder result = new StringBuilder(name.length());
				String[] words = name.split("\\s");
				for(int i=0;i<words.length;++i) {
				  if(i>0){
					  result.append(" ");     
				  }
				  if(words[i].length()>0){
					  if(words[i].length()>1){
						  result.append(Character.toUpperCase(words[i].charAt(0))).append(words[i].substring(1));
					  }
					  else
					  {
						  result.append(words[i].charAt(0));
					  }
				  }  
				}
				userName = result.toString();
			}
		}
	}

	public void prepareData(boolean generateGID){
		if(generateGID){
			DigestSHA3 md = new DigestSHA3(512);
			byte[] data = DataFactory.createObjectData(this); //todo
			md.update(data);
			byte[] digest = md.digest();
			gid = org.bouncycastle.util.encoders.Hex.toHexString(digest).substring(0,DataService.getHashLength());
		}
		id  = 0;
	}
	
	
	/*
	
	public void setUserData(HttpServletRequest request,boolean generateGID){
		if(generateGID){
			DigestSHA3 md = new DigestSHA3(224);
			String body = request.getParameterMap().toString()+"USERDATA"; //todo
			try {
				md.update(body.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			byte[] digest = md.digest();
			gid = org.bouncycastle.util.encoders.Hex.toHexString(digest).substring(0,32);
		}
		id  = 0;
		
		String phoneNumber = request.getParameter("phoneNumber");
		if(phoneNumber!=null){
			phoneNumberList.add(phoneNumber);
		}
		String imsiString = request.getParameter("imsiString"); 
		String simID      = request.getParameter("simID");
		
		simList.add(new SIMItem(imsiString,simID,phoneNumber));
		
		String carrierName = request.getParameter("carrierName");
		carrierList.add(carrierName);
		
		
		String contactNumString = request.getParameter("contactNum");
		//System.out.println();
		//System.out.println("contactNum: "+contactNumString);
		//System.out.println();
		if(contactNumString!=null){
			int contactNum = Integer.valueOf(contactNumString);
			//System.out.println("contactNum int: "+contactNum);
			for(int s = 0;s<contactNum;s++){
				String itemString = request.getParameter("contact_"+s);
				//System.out.println("contact_"+s);
				if(itemString!=null){
					try {
						itemString = URLDecoder.decode(itemString, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String[] parts = itemString.split("\\|");
					if(parts.length == 2){
						//System.out.println("ContactItem: "+itemString);
						contactList.add(new ContactItem(parts[0],parts[1]));
					}
				}
			}
		}
		System.out.println();
		System.out.println();
		String callLogNumString = request.getParameter("callLogNum");
		if(callLogNumString!=null){
			int callLogNum = Integer.valueOf(callLogNumString);
			for(int s = 0;s<callLogNum;s++){
				String itemString = request.getParameter("callLog_"+s);
				if(itemString!=null){
					try {
						itemString = URLDecoder.decode(itemString, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String[] parts = itemString.split("\\|");
					if(parts.length == 4){
						//System.out.println("CallLogItem: "+itemString);
						callLogList.add(new CallLogItem(parts[0],parts[1],parts[2],parts[3]));
					}
				}
			}
		}
		//Sound Search für Google Play|com.google.android.ears|1.1.8|Sat Sep 06 02:06:15 MESZ 2014|Sat Sep 06 02:06:15 MESZ 2014
		String packageNumString = request.getParameter("packageNum");
		if(packageNumString!=null){
			int packageNum = Integer.valueOf(packageNumString);
			for(int s = 0;s<packageNum;s++){
				String itemString = request.getParameter("package_"+s);
				if(itemString!=null){
					try {
						itemString = URLDecoder.decode(itemString, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String[] parts = itemString.split("\\|");
					if(parts.length == 5){
						//System.out.println("PackageItem: "+itemString);
						packageList.add(new PackageItem(parts[0],parts[1],parts[2],Long.valueOf(parts[3]),Long.valueOf(parts[4])));
					}
				}
			}
		}
		String accountNumString = request.getParameter("accountNum");
		if(accountNumString!=null){
			int accountNum = Integer.valueOf(accountNumString);
			for(int s = 0;s<accountNum;s++){
				String itemString = request.getParameter("account_"+s);
				if(itemString!=null){
					try {
						itemString = URLDecoder.decode(itemString, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String[] parts = itemString.split("\\|");
					if(parts.length == 2){
						//System.out.println("AccountItem: "+itemString);
						accountList.add(new AccountItem(parts[0],parts[1]));
					}
				}
			}
		}
		
		String bluetoothNumString = request.getParameter("bluetoothNum");
		if(bluetoothNumString!=null){
			int bluetoothNum = Integer.valueOf(bluetoothNumString);
			for(int s = 0;s<bluetoothNum;s++){
				String itemString = request.getParameter("bluetooth_"+s);
				if(itemString!=null){
					try {
						itemString = URLDecoder.decode(itemString, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String[] parts = itemString.split("\\|");
					if(parts.length == 2){
						//System.out.println("BluetoothItem: "+itemString);
						bluetoothList.add(new BluetoothItem(parts[0],parts[1]));
					}
				}
			}
		}
	
		
		
		String wlanNumString = request.getParameter("configuredWLANNum");
		if(wlanNumString!=null){
			int wlanNum = Integer.valueOf(wlanNumString);
			for(int s = 0;s<wlanNum;s++){
				String wlanSSID = request.getParameter("configuredWLAN_"+s);
				
				if(wlanSSID!=null){
					try {
						wlanSSID = URLDecoder.decode(wlanSSID, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					wlanList.add(new WLANItem(wlanSSID,"",WLANItem.WLANType.Configured));
				}
					
			}
		}
		
		extractUserName();
	  
	}
	*/
	
	
}



/*
  DigestSHA3 md = new DigestSHA3(256);
		String body = request.getParameterMap().toString(); //todo
		try {
			md.update(body.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		byte[] digest = md.digest();
	    String userID =  org.bouncycastle.util.encoders.Hex.toHexString(digest);
	    */
