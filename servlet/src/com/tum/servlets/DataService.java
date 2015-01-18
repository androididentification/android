package com.tum.servlets;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import com.mysql.jdbc.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import com.mysql.jdbc.exceptions.jdbc4.MySQLTransactionRollbackException;
import com.tum.ident.battery.BatteryItemList;
import com.tum.ident.camera.CameraPixelList;
import com.tum.ident.camera.DarkFrame;
import com.tum.ident.device.Device;
import com.tum.ident.device.DeviceIDItem;
import com.tum.ident.device.DeviceMatch;
import com.tum.ident.files.FileItem;
import com.tum.ident.files.FileItemList;
import com.tum.ident.gaitrecognition.StepStatistics;
import com.tum.ident.locations.LocationAreaList;
import com.tum.ident.music.MusicItem;
import com.tum.ident.music.MusicItemList;
import com.tum.ident.orientation.OrientationItem;
import com.tum.ident.result.ResultMatchItem;
import com.tum.ident.user.AccountItem;
import com.tum.ident.user.CallLogItem;
import com.tum.ident.user.ContactItem;
import com.tum.ident.user.User;
import com.tum.ident.user.UserMatch;
import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.userdevice.PackageItem;
import com.tum.ident.userdevice.UserDevice;
import com.tum.ident.userdevice.WLANItem;
import com.tum.servlets.identification.Match;


public  class DataService {

	public enum DataType {
		PhoneNumber,
		WLAN,
		Package,
		Account,
		Contact,
		CallLog,
		Bluetooth,
		UserName,
		User,
		Device
	}

	static int hashLength = 64;

	public static String rootPath = "";

	
	public static Semaphore dataBaseSemaphore = new Semaphore(1); 

	private static boolean tableExists(Connection connection,String name){
		boolean result = false;
		String sqlString = "SHOW TABLES LIKE '"+name+"'";	
		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			if(rs!=null){
				try {
					if(rs.next() ) {
						result = true;
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}


	private static int loadHashLength(Connection connection){
		int newHashLength = 0;
		String sqlString = "SELECT hash_length FROM database_properties";	
		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			if(rs!=null){
				try {
					if(rs.next() ) {
						newHashLength = rs.getInt("hash_length");
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return newHashLength;
	}

	public static boolean init(Connection connection){
		if(tableExists(connection,"database_properties")==false){
			//System.out.println("tableExists == false");  
			return false;
		}
		int newHashLength = loadHashLength(connection);
		if(newHashLength > 0){
			//System.out.println("hash_length: "+newHashLength);
			hashLength = newHashLength;
			//System.out.println("hashLength = "+newHashLength);  
			return true;
		}
		else{
			//System.out.println("error: hashLength = 0");  
			return false;
		}

	}

	public static void setHashLength(int l){
		hashLength = l;
	}

	public static int getHashLength(){
		return hashLength;
	}

	private static void prepareDatabase(Connection connection){
		String sqlString = "INSERT IGNORE INTO database_properties (hash_length) VALUES ("+hashLength+")";	
		execute(sqlString,connection);
	}


	public static void lock(){
		try {
			dataBaseSemaphore.tryAcquire(20,TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			//System.out.println("dataBaseSemaphore.acquire failed!");
		}
	}
	public static void unlock(){
		dataBaseSemaphore.release();
	}
	

	public static boolean createDatabase(Connection connection,boolean update){
		
		if(connection!=null){
			lock();
			//System.out.println("prepareDatabase start...");
			String sqlString = "SET AUTOCOMMIT=0";
			execute(sqlString,connection);
			sqlString ="START TRANSACTION";
			execute(sqlString,connection);

			if(update==false){
				if(tableExists(connection,"database_properties")){
					unlock();
					return false;
				}
			}

			String sqlCreate;

			sqlCreate = "CREATE TABLE IF NOT EXISTS database_properties"
					+ "  (hash_length    INT UNSIGNED)";

			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS users"
					+ "  (user_id  	    BIGINT UNSIGNED AUTO_INCREMENT,"
					+ "   gid           VARCHAR("+hashLength+") NOT NULL UNIQUE,"
					+ "   INDEX         (gid),"
					+ "   UNIQUE   		(gid),"
					+ "   PRIMARY KEY (user_id))";

			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS devices"
					+ "  (device_id      BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   gid            VARCHAR("+hashLength+") NOT NULL UNIQUE,"
					+ "   manufacturer   VARCHAR("+hashLength+") ,"
					+ "   model          VARCHAR("+hashLength+") ,"
					+ "   cpu            VARCHAR("+hashLength+") ,"
					+ "   ram            BIGINT,"
					+ "   storage_size   BIGINT,"
					+ "   INDEX         (gid),"
					+ "   UNIQUE   		(gid),"
					+ "   PRIMARY KEY (device_id))";

			execute(sqlCreate,connection);





			sqlCreate = "CREATE TABLE IF NOT EXISTS device_properties"
					+ "  (dp_device_id     BIGINT UNSIGNED  NOT NULL,"
					+ "   android_id       VARCHAR("+hashLength+"),"
					+ "   serial           VARCHAR("+hashLength+") ,"
					+ "   gsf_id           VARCHAR("+hashLength+"),"
					+ "   imei             VARCHAR("+hashLength+"),"
					+ "   UNIQUE   (dp_device_id,android_id,serial,gsf_id,imei),"
					+ "   CONSTRAINT dp_fk_device FOREIGN KEY (dp_device_id) REFERENCES devices(device_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS pixel_errors"
					+ "  (pe_device_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   camera_index    INT,"
					+ "   data_file       TEXT,"
					+ "   CONSTRAINT pe_fk_device FOREIGN KEY (pe_device_id) REFERENCES devices(device_id))";

			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS dark_frames"
					+ "  (df_device_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   avg_red         DOUBLE,"
					+ "   avg_blue        DOUBLE,"
					+ "   avg_green       DOUBLE,"
					+ "   camera_index    INT,"
					+ "   data_file       TEXT,"
					+ "   CONSTRAINT df_fk_device FOREIGN KEY (df_device_id) REFERENCES devices(device_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS packages"
					+ "  (package_id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   app_name       	 VARCHAR("+hashLength+") ,"
					+ "   package_name       VARCHAR("+hashLength+") ,"
					+ "   INDEX   (app_name),"
					+ "   INDEX   (package_name),"
					+ "   UNIQUE   (app_name,package_name),"
					+ "   PRIMARY KEY (package_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS accounts"
					+ "  (account_id         BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   name        		 VARCHAR("+hashLength+") ,"
					+ "   type        	     VARCHAR("+hashLength+") ,"
					+ "   UNIQUE   (name,type),"
					+ "   PRIMARY KEY (account_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS phone_nrs"
					+ "  (phone_nr_id        BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   phone_nr           VARCHAR("+hashLength+") ,"
					+ "   INDEX     (phone_nr),"
					+ "   UNIQUE   (phone_nr),"
					+ "   PRIMARY KEY (phone_nr_id))";
			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS bluetooth_adapters"
					+ "  (bluetooth_id       BIGINT UNSIGNED  NOT NULL  AUTO_INCREMENT,"
					+ "   mac        		 VARCHAR("+hashLength+"),"
					+ "   name        		 VARCHAR("+hashLength+"),"
					+ "   INDEX              (mac),"
					+ "   UNIQUE             (mac,name),"
					+ "   PRIMARY KEY (bluetooth_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS wlan_networks"
					+ "  (wlan_id       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   bssid         VARCHAR("+hashLength+"),"
					+ "   ssid          VARCHAR("+hashLength+"),"
					+ "   type          INTEGER,"
					+ "   INDEX         (bssid),"
					+ "   UNIQUE  (bssid,ssid,type),"
					+ "   PRIMARY KEY (wlan_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS configured_wlan_networks"
					+ "  (wlan_id       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   ssid          VARCHAR("+hashLength+"),"
					+ "   INDEX         (ssid),"
					+ "   UNIQUE  (ssid),"
					+ "   PRIMARY KEY  (wlan_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS wlan_adapters"
					+ "  (wlan_id       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   mac           VARCHAR("+hashLength+"),"
					+ "   type          INTEGER,"
					+ "   INDEX         (mac),"
					+ "   UNIQUE  (mac,type),"
					+ "   PRIMARY KEY   (wlan_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS files"
					+ "  (file_id       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   file_hash     VARCHAR("+hashLength+"),"
					+ "   UNIQUE  (file_hash),"
					+ "   PRIMARY KEY   (file_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS music"
					+ "  (music_id       BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,"
					+ "   artist_hash     VARCHAR("+hashLength+"),"
					+ "   UNIQUE  (artist_hash),"
					+ "   PRIMARY KEY   (music_id))";
			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS device_identification"
					+ "  (di_device_1_id   BIGINT UNSIGNED NOT NULL,"
					+ "   di_device_2_id   BIGINT UNSIGNED NOT NULL,"
					+ "   m                DOUBLE,"
					+ "   gsf_id           INTEGER,"
					+ "   imei             INTEGER,"
					+ "   serial           INTEGER,"
					+ "   android_id       INTEGER,"
					+ "   bluetooth        DOUBLE,"
					+ "   wlan             DOUBLE,"
					+ "   INDEX           (di_device_1_id),"
					+ "   INDEX           (di_device_2_id),"
					+ "   CONSTRAINT di_fk_device_1 FOREIGN KEY (di_device_1_id) REFERENCES devices(device_id),"
					+ "   CONSTRAINT di_fk_device_2 FOREIGN KEY (di_device_2_id) REFERENCES devices(device_id))";

			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS user_identification"
					+ "  (ui_user_1_id   BIGINT UNSIGNED NOT NULL,"
					+ "   ui_user_2_id   BIGINT UNSIGNED NOT NULL,"
					+ "   m                DOUBLE ,"
					+ "   contacts         DOUBLE ,"
					+ "   contacts_s       DOUBLE ,"
					+ "   call_log         DOUBLE ,"
					+ "   call_log_s       DOUBLE ,"
					+ "   packages         DOUBLE ,"
					+ "   packages_s       DOUBLE ,"
					+ "   accounts         DOUBLE ,"
					+ "   accounts_s       DOUBLE ,"
					+ "   bluetooth        DOUBLE ,"
					+ "   bluetooth_s      DOUBLE ,"
					+ "   wlan             DOUBLE ,"
					+ "   wlan_s           DOUBLE ,"
					+ "   user_name        DOUBLE ,"
					+ "   INDEX           (ui_user_1_id),"
					+ "   INDEX           (ui_user_2_id),"
					+ "   CONSTRAINT un_fk_user_1 FOREIGN KEY (ui_user_1_id) REFERENCES users(user_id),"
					+ "   CONSTRAINT un_fk_user_2 FOREIGN KEY (ui_user_2_id) REFERENCES users(user_id))";


			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS user_locations"
					+ "  (ul_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   data_file      TEXT,"
					+ "   INDEX         (ul_user_id),"
					+ "   CONSTRAINT ul_fk_user FOREIGN KEY (ul_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS user_orientations"
					+ "  (uo_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   data_file      TEXT,"
					+ "   INDEX         (uo_user_id),"
					+ "   CONSTRAINT uo_fk_user FOREIGN KEY (uo_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS gait_recognition"
					+ "  (gr_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   data_file       TEXT,"
					+ "   INDEX         (gr_user_id),"
					+ "   CONSTRAINT gr_fk_user FOREIGN KEY (gr_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS user_names"
					+ "  (un_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   name          VARCHAR("+hashLength+"),"
					+ "   INDEX         (un_user_id),"
					+ "   UNIQUE  (un_user_id,name),"
					+ "   CONSTRAINT un_fk_user FOREIGN KEY (un_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS user_packages"
					+ "  (up_user_id      BIGINT UNSIGNED  NOT NULL,"
					+ "   up_package_id   BIGINT UNSIGNED  NOT NULL,"
					+ "   up_install_time BIGINT,"
					+ "   up_update_time  BIGINT,"
					+ "   INDEX         (up_user_id),"
					+ "   INDEX         (up_package_id),"
					+ "   UNIQUE  (up_user_id,up_package_id,up_install_time,up_update_time),"
					+ "   CONSTRAINT up_fk_user FOREIGN KEY (up_user_id) REFERENCES users(user_id),"
					+ "   CONSTRAINT up_fk_package_properties FOREIGN KEY (up_package_id) REFERENCES packages(package_id))";
			execute(sqlCreate,connection);

			
			sqlCreate = "CREATE TABLE IF NOT EXISTS device_packages"
					+ "  (dpa_device_id      BIGINT UNSIGNED  NOT NULL,"
					+ "   dpa_package_id   BIGINT UNSIGNED  NOT NULL,"
					+ "   dpa_install_time BIGINT,"
					+ "   dpa_update_time  BIGINT,"
					+ "   INDEX         (dpa_device_id),"
					+ "   INDEX         (dpa_package_id),"
					+ "   UNIQUE  (dpa_device_id,dpa_package_id,dpa_install_time,dpa_update_time),"
					+ "   CONSTRAINT dpa_fk_device FOREIGN KEY (dpa_device_id) REFERENCES devices(device_id),"
					+ "   CONSTRAINT dpa_fk_package_properties FOREIGN KEY (dpa_package_id) REFERENCES packages(package_id))";
			execute(sqlCreate,connection);
			
			
			sqlCreate = "CREATE TABLE IF NOT EXISTS user_files"
					+ "  (uf_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   uf_file_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX         (uf_user_id),"
					+ "   INDEX         (uf_file_id),"
					+ "   UNIQUE  (uf_user_id,uf_file_id),"
					+ "   CONSTRAINT uf_fk_file FOREIGN KEY (uf_file_id) REFERENCES files (file_id),"
					+ "   CONSTRAINT uf_fk_user FOREIGN KEY (uf_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS user_music"
					+ "  (um_user_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   um_music_id    BIGINT UNSIGNED  NOT NULL,"
					+ "   counter        BIGINT,"
					+ "   INDEX         (um_user_id),"
					+ "   INDEX         (um_music_id),"
					+ "   UNIQUE  (um_user_id,um_music_id),"
					+ "   CONSTRAINT um_fk_music FOREIGN KEY (um_music_id) REFERENCES music (music_id),"
					+ "   CONSTRAINT um_fk_user FOREIGN KEY (um_user_id) REFERENCES users(user_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS user_call_logs"
					+ "  (ucl_user_id       BIGINT UNSIGNED  NOT NULL,"
					+ "   ucl_phone_nr_id   BIGINT UNSIGNED  NOT NULL,"
					+ "   type          CHAR(1),"
					+ "   date          BIGINT,"
					+ "   duration      INTEGER,"
					+ "   INDEX         (ucl_user_id),"
					+ "   INDEX         (ucl_phone_nr_id),"
					+ "   INDEX         (type),"
					+ "   INDEX         (date),"
					+ "   UNIQUE  (ucl_user_id,ucl_phone_nr_id,type,date,duration),"
					+ "   CONSTRAINT ucl_fk_user FOREIGN KEY (ucl_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT ucl_fk_phone_nr FOREIGN KEY (ucl_phone_nr_id) REFERENCES phone_nrs(phone_nr_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS batteries"
					+ "  (b_user_id         BIGINT UNSIGNED  NOT NULL,"
					+ "   b_device_id       BIGINT UNSIGNED  NOT NULL,"
					+ "   data_file         TEXT,"
					+ "   INDEX             (b_user_id),"
					+ "   INDEX             (b_device_id),"
					+ "   UNIQUE            (b_user_id,b_device_id),"
					+ "   CONSTRAINT b_fk_user FOREIGN KEY (b_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT b_fk_device FOREIGN KEY (b_device_id) REFERENCES devices(device_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS user_devices"
					+ "  (ud_user_id         BIGINT UNSIGNED  NOT NULL,"
					+ "   ud_device_id       BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX             (ud_user_id),"
					+ "   INDEX             (ud_device_id),"
					+ "   UNIQUE            (ud_user_id,ud_device_id),"
					+ "   CONSTRAINT ud_fk_user FOREIGN KEY (ud_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT ud_fk_device FOREIGN KEY (ud_device_id) REFERENCES devices(device_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS user_contacts"
					+ "  (uc_user_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   uc_phone_nr_id        BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                (uc_user_id),"
					+ "   INDEX                (uc_phone_nr_id),"
					+ "   UNIQUE  (uc_user_id,uc_phone_nr_id),"
					+ "   CONSTRAINT uc_fk_user FOREIGN KEY (uc_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT uc_fk_phone_nr FOREIGN KEY (uc_phone_nr_id) REFERENCES phone_nrs(phone_nr_id))";

			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS user_sim_cards"
					+ "  (usc_user_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   usc_phone_nr_id        BIGINT UNSIGNED  NOT NULL,"
					+ "   imsi                   VARCHAR("+hashLength+"),"
					+ "   ssn                   VARCHAR("+hashLength+"),"
					+ "   INDEX                (usc_user_id),"
					+ "   INDEX                (usc_phone_nr_id),"
					+ "   UNIQUE  (usc_user_id,usc_phone_nr_id,imsi,ssn),"
					+ "   CONSTRAINT usc_fk_user FOREIGN KEY (usc_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT usc_fk_phone_nr FOREIGN KEY (usc_phone_nr_id) REFERENCES phone_nrs(phone_nr_id))";

			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS user_accounts"
					+ "  (ua_user_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   ua_account_id         BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                (ua_user_id),"
					+ "   INDEX                (ua_user_id),"
					+ "   UNIQUE  (ua_user_id,ua_account_id),"
					+ "   CONSTRAINT ua_fk_user FOREIGN KEY (ua_user_id) REFERENCES users (user_id),"
					+ "   CONSTRAINT ua_fk_account FOREIGN KEY (ua_account_id) REFERENCES accounts(account_id))";

			execute(sqlCreate,connection);




			sqlCreate = "CREATE TABLE IF NOT EXISTS bluetooth_connections"
					+ "  (bc_user_id          BIGINT UNSIGNED  NOT NULL,"
					+ "   bc_bluetooth_id       BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                (bc_user_id),"
					+ "   INDEX                (bc_bluetooth_id),"
					+ "   UNIQUE  (bc_user_id,bc_bluetooth_id),"
					+ "   CONSTRAINT bc_fk_user FOREIGN KEY (bc_user_id) REFERENCES users(user_id),"
					+ "   CONSTRAINT bc_fk_bluetooth FOREIGN KEY (bc_bluetooth_id) REFERENCES bluetooth_adapters(bluetooth_id))";

			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS device_bluetooth_adapters"
					+ "  (dba_device_id          BIGINT UNSIGNED  NOT NULL,"
					+ "   dba_bluetooth_id       BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                (dba_device_id),"
					+ "   INDEX                (dba_bluetooth_id),"
					+ "   UNIQUE  (dba_device_id,dba_bluetooth_id),"
					+ "   CONSTRAINT dba_fk_device FOREIGN KEY (dba_device_id) REFERENCES devices(device_id),"
					+ "   CONSTRAINT dba_fk_bluetooth FOREIGN KEY (dba_bluetooth_id) REFERENCES bluetooth_adapters(bluetooth_id))";

			execute(sqlCreate,connection);



			sqlCreate = "CREATE TABLE IF NOT EXISTS configured_wlan_connections"
					+ "  (cwc_user_id            BIGINT UNSIGNED  NOT NULL ,"
					+ "   cwc_wlan_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                 (cwc_user_id),"
					+ "   INDEX                 (cwc_wlan_id),"
					+ "   UNIQUE  (cwc_user_id,cwc_wlan_id),"
					+ "   CONSTRAINT cwc_fk_user FOREIGN KEY (cwc_user_id) REFERENCES users(user_id),"
					+ "   CONSTRAINT cwc_fk_wlan FOREIGN KEY (cwc_wlan_id) REFERENCES configured_wlan_networks(wlan_id))";
			execute(sqlCreate,connection);

			sqlCreate = "CREATE TABLE IF NOT EXISTS wlan_connections"
					+ "  (wc_user_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   wc_wlan_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                 (wc_user_id),"
					+ "   INDEX                 (wc_user_id),"
					+ "   UNIQUE  (wc_user_id,wc_wlan_id),"
					+ "   CONSTRAINT wc_fk_user FOREIGN KEY (wc_user_id) REFERENCES users(user_id),"
					+ "   CONSTRAINT wc_fk_wlan FOREIGN KEY (wc_wlan_id) REFERENCES wlan_networks(wlan_id))";
			execute(sqlCreate,connection);


			sqlCreate = "CREATE TABLE IF NOT EXISTS device_wlan_adapters"
					+ "  (dwa_device_id          BIGINT UNSIGNED  NOT NULL,"
					+ "   dwa_wlan_id            BIGINT UNSIGNED  NOT NULL,"
					+ "   INDEX                 (dwa_device_id),"
					+ "   INDEX                 (dwa_wlan_id),"
					+ "   UNIQUE  (dwa_device_id,dwa_wlan_id),"
					+ "   CONSTRAINT dwa_fk_device FOREIGN KEY (dwa_device_id) REFERENCES devices(device_id),"
					+ "   CONSTRAINT dwa_fk_wlan_nr FOREIGN KEY (dwa_wlan_id) REFERENCES wlan_adapters(wlan_id))";

			execute(sqlCreate,connection);


			sqlString ="COMMIT";
			execute(sqlString,connection);
			sqlString = "SET AUTOCOMMIT=1";
			execute(sqlString,connection);

			prepareDatabase(connection);

			//System.out.println("prepareDatabase finished...");
			unlock();
			return true;
		}
		return false;

	}


	public static boolean alreadyInDatabase(Connection connection,UserDevice userDevice){
		if(userDevice.getUserId()!=0 && userDevice.getDeviceId() != 0){
			String sqlString = "SELECT ud_user_id FROM user_devices WHERE ud_user_id='"+userDevice.getUserId()+"' AND ud_device_id='"+userDevice.getDeviceId()+"'";	
			return alreadyInDatabaseExecute(connection,sqlString,"ud_user_id");
		}
		return false;
	}

	public static boolean alreadyInDatabase(Connection connection,ContactItem contactItem,long userID){
		if(contactItem.getPhoneNumberID() !=0){
			String sqlString = "SELECT uc_user_id FROM user_contacts WHERE uc_user_id='"+userID+"' AND uc_phone_nr_id='"+contactItem.getPhoneNumberID()+"'";	
			return alreadyInDatabaseExecute(connection,sqlString,"uc_user_id");
		}
		return false;
	}

	public static boolean alreadyInDatabase(Connection connection,CallLogItem callLogItem,long userID){
		if(callLogItem.getPhoneNumberID()!=0){
			String sqlString = "SELECT ucl_user_id FROM user_call_logs WHERE ucl_phone_nr_id='"+callLogItem.getPhoneNumberID()+"' AND type='"+callLogItem.getCallType()+"' AND date='"+callLogItem.getCallDate()+"' AND duration='"+callLogItem.getCallDuration()+"'";	
			return alreadyInDatabaseExecute(connection,sqlString,"ucl_user_id");
		}
		return false;
	}




	public static long getIDfromDatabase(Connection connection,WLANItem wlanItem){
		String sqlString = "";
		if(wlanItem.getType()==WLANItem.WLANType.Configured){
			sqlString = "SELECT wlan_id FROM configured_wlan_networks WHERE ssid='"+wlanItem.getSSID()+"'";	
		}
		else if((wlanItem.getType()==WLANItem.WLANType.Device)){
			sqlString = "SELECT wlan_id FROM wlan_adapters WHERE mac='"+wlanItem.getBSSID()+"'";	
		}
		else{
			sqlString = "SELECT wlan_id FROM wlan_networks WHERE bssid='"+wlanItem.getBSSID()+"' AND ssid='"+wlanItem.getSSID()+"'";	
		}
		return getIDfromDatabaseExecute(connection,sqlString,"wlan_id");
	}

	public static long getIDfromDatabase(Connection connection, AccountItem accountItem){
		String sqlString = "SELECT account_id FROM accounts WHERE name='"+accountItem.getName()+"' AND type = '"+accountItem.getType()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"account_id");
	}

	public static long getIDfromDatabase(Connection connection, MusicItem item){
		String sqlString = "SELECT music_id FROM music WHERE artist_hash='"+item.getArtist()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"music_id");
	}

	public static long getIDfromDatabase(Connection connection, FileItem item){
		String sqlString = "SELECT file_id FROM files WHERE file_hash='"+item.getFileHash()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"file_id");
	}


	public static long getIDfromDatabase(Connection connection,BluetoothItem bluetoothItem){
		String sqlString = "SELECT bluetooth_id FROM bluetooth_adapters WHERE mac='"+bluetoothItem.getMAC()+"' AND name = '"+bluetoothItem.getName()+"'";	//todo Name
		return getIDfromDatabaseExecute(connection,sqlString,"bluetooth_id");
	}

	public static long getIDfromDatabase(Connection connection,String stringItem,DataType dataType){
		long id = 0;
		if(dataType == DataType.PhoneNumber){
			String sqlString = "SELECT phone_nr_id FROM phone_nrs WHERE phone_nr='"+stringItem+"'";	
			id = getIDfromDatabaseExecute(connection,sqlString,"phone_nr_id");
		}
		return id;
	}

	public static long getIDfromDatabase(Connection connection,PackageItem packageItem){

		String sqlString = "SELECT package_id FROM packages WHERE app_name='"+packageItem.getAppName()+"' AND package_name = '"+packageItem.getPackageName()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"package_id");
	}

	public static long getIDfromDatabase(Connection connection,User user){
		String sqlString = "SELECT user_id FROM users WHERE gid='"+user.getGid()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"user_id");
	}

	public static long getIDfromDatabase(Connection connection,Device device){
		String sqlString = "SELECT device_id FROM devices WHERE gid='"+device.getGid()+"'";	
		return getIDfromDatabaseExecute(connection,sqlString,"device_id");	
	}




	public static long insert(Connection connection,boolean systemPackage,PackageItem packageItem, long objectID){
		long id = packageItem.getId();

		if(id==0){
			id = getIDfromDatabase(connection,packageItem);
		}
		if(id==0){
			String sqlString = "INSERT IGNORE INTO packages (app_name,package_name) VALUES " + "  ('"+packageItem.getAppName()+"','"+packageItem.getPackageName()+"')";	
			id = insertAndReturnKey(sqlString,connection);
		}
		packageItem.setId(id);
		if(systemPackage==false){
			String sqlString = "INSERT IGNORE INTO user_packages (up_user_id,up_package_id,up_install_time,up_update_time) VALUES ("+objectID+","+id+","+packageItem.getInstallTime()+","+packageItem.getUpdateTime()+")";	
			execute(sqlString,connection);
		}
		else{
			String sqlString = "INSERT IGNORE INTO device_packages (dpa_device_id,dpa_package_id,dpa_install_time,dpa_update_time) VALUES ("+objectID+","+id+","+packageItem.getInstallTime()+","+packageItem.getUpdateTime()+")";	
			//System.out.println("INSERT: "+sqlString);
			execute(sqlString,connection);
		}
		return id;

	}
	
	public static void insert(Connection connection, CallLogItem callLogItem,long userID){
		long phoneNumberID = callLogItem.getPhoneNumberID();
		if(phoneNumberID==0){
			phoneNumberID = getIDfromDatabase(connection,callLogItem.getPhoneNumber(),DataType.PhoneNumber);
		}
		if(phoneNumberID==0){
			phoneNumberID = insert(connection,callLogItem.getPhoneNumber(),DataType.PhoneNumber);
		}
		callLogItem.setPhoneNumberID(phoneNumberID);
		String sqlString = "INSERT IGNORE INTO user_call_logs (ucl_user_id,ucl_phone_nr_id,type,date,duration) VALUES ("+userID+","+phoneNumberID+","+callLogItem.getCallType()+",'"+callLogItem.getCallDate()+"','"+callLogItem.getCallDuration()+"')";	
		execute(sqlString,connection);
	}

	public static long insert(Connection connection,String stringItem,DataType dataType){
		long id = 0;
		if(dataType == DataType.PhoneNumber){
			String phoneNumber = stringItem;
			String sqlString = "INSERT IGNORE INTO phone_nrs (phone_nr) VALUES ('"+phoneNumber+"')";	
			id = insertAndReturnKey(sqlString,connection);
		}
		return id;
	}

	public static void insert(Connection connection,ContactItem contact,long userID){
		long phoneNumberID = contact.getPhoneNumberID();
		String phoneNumber = contact.getPhoneNumber();
		if(phoneNumberID==0){
			phoneNumberID = getIDfromDatabase(connection,phoneNumber,DataType.PhoneNumber);
		}
		if(phoneNumberID==0){
			phoneNumberID = insert(connection,phoneNumber,DataType.PhoneNumber);
		}
		contact.setPhoneNumberID(phoneNumberID);
		String sqlString = "INSERT IGNORE INTO user_contacts (uc_user_id,uc_phone_nr_id) VALUES ("+userID+","+phoneNumberID+")";	
		execute(sqlString,connection);
	}

	public static long insert(Connection connection,BluetoothItem bluetoothItem,long userID){
		long id = bluetoothItem.getId();
		if(id==0){
			id =getIDfromDatabase(connection,bluetoothItem);
		}
		if(id==0){
			String sqlString = "INSERT IGNORE INTO bluetooth_adapters (mac,name) VALUES ('"+bluetoothItem.getMAC()+"','"+bluetoothItem.getName()+"')";	
			id = insertAndReturnKey(sqlString,connection);
		}
		bluetoothItem.setId(id);
		String sqlString = "INSERT IGNORE INTO bluetooth_connections (bc_user_id,bc_bluetooth_id) VALUES ("+userID+","+id+")";	
		execute(sqlString,connection);

		return id;
	}

	public static long insert(Connection connection, AccountItem accountItem,long userID){
		long id = accountItem.getId();	
		if(id==0){
			id = getIDfromDatabase(connection,accountItem);
		}
		if(id==0){
			String sqlString = "INSERT IGNORE INTO accounts (name,type) VALUES ('"+accountItem.getName()+"','"+accountItem.getType()+"')";
			id = insertAndReturnKey(sqlString,connection);
		}
		accountItem.setId(id);
		String sqlString = "INSERT IGNORE INTO user_accounts (ua_user_id,ua_account_id) VALUES ("+userID+","+id+")";	
		execute(sqlString,connection);
		return id;

	}

	public static long insert(Connection connection,WLANItem wlanItem,long userID){
		long id = wlanItem.getId();
		if(id==0){
			id = getIDfromDatabase(connection,wlanItem);
		}
		if(wlanItem.getType()==WLANItem.WLANType.Configured){
			if(id==0){
				String sqlString = "INSERT IGNORE INTO configured_wlan_networks (ssid) VALUES ('"+wlanItem.getSSID()+"')";	
				id = insertAndReturnKey(sqlString,connection);
			}
			wlanItem.setId(id);
			String sqlString = "INSERT IGNORE INTO configured_wlan_connections (cwc_user_id,cwc_wlan_id) VALUES ("+userID+","+id+")";	
			execute(sqlString,connection);
		}
		else if(wlanItem.getType()==WLANItem.WLANType.Connected){
			if(id==0){
				String sqlString = "INSERT IGNORE INTO wlan_networks (bssid,ssid) VALUES ('"+wlanItem.getBSSID()+"','"+wlanItem.getSSID()+"')";	
				id = insertAndReturnKey(sqlString,connection);
			}
			wlanItem.setId(id);
			String sqlString = "INSERT IGNORE INTO wlan_connections (wc_user_id,wc_wlan_id) VALUES ("+userID+","+id+")";	
			execute(sqlString,connection);
		}
		return id;
	}

	public static long insertDeviceProperties(Connection connection,Device device){
		//System.out.println("insertDeviceProperties!!!!!!!!!!!!!!");
		String sqlString;
		long id = device.getId();
		//System.out.println("device.getId() "+id);
		
		ArrayList<PackageItem> packageList = device.getPackageList();
		if(packageList!=null){
			for (PackageItem packageItem : packageList) {
				insert(connection,true,packageItem,id);
			}
		}
		
		ArrayList<DeviceIDItem> deviceIDList = device.getDeviceIDList();
		if(deviceIDList!=null){
			for (DeviceIDItem deviceIDItem : deviceIDList) {	
				sqlString = "INSERT IGNORE INTO device_properties (dp_device_id,android_id,serial,gsf_id,imei) VALUES  ('"+id+"','"+deviceIDItem.getAndroidID()+"','"+deviceIDItem.getSerialNum()+"','"+deviceIDItem.getGsfAndroidID()+"','"+deviceIDItem.getImeiString()+"')";
				execute(sqlString,connection);
			}
		}
		ArrayList<BluetoothItem> bluetoothList = device.getBluetooth();
		//System.out.println("bluetoothList.size() "+bluetoothList.size());
		if(bluetoothList!=null){
			for (BluetoothItem bluetoothItem : bluetoothList) {
				if(bluetoothItem!=null){
					//System.out.println("insert BLUETOOTH!!!!!!!!!!!!!!");
	
					long bluetoothID = getIDfromDatabase(connection,bluetoothItem);
					if(bluetoothID==0){
						sqlString = "INSERT IGNORE INTO bluetooth_adapters (mac,name) VALUES ('"+bluetoothItem.getMAC()+"','"+bluetoothItem.getName()+"')";	
						//System.out.println(sqlString);
						bluetoothID = insertAndReturnKey(sqlString,connection);
					}
					bluetoothItem.setId(bluetoothID);
					sqlString = "INSERT IGNORE INTO device_bluetooth_adapters (dba_device_id,dba_bluetooth_id) VALUES ("+id+","+bluetoothID+")";	
					//System.out.println(sqlString);
					execute(sqlString,connection);
				}
			}
		}
		ArrayList<WLANItem> wlanList = device.getWLAN();
		if(wlanList!=null){
			for (WLANItem wlanItem : wlanList) {
				if(wlanItem!=null){
					long WLANID = getIDfromDatabase(connection,wlanItem);
					if(WLANID==0){
						sqlString = "INSERT IGNORE INTO wlan_adapters (mac) VALUES ('"+wlanItem.getBSSID()+"')";	
						WLANID = insertAndReturnKey(sqlString,connection);
	
					}
					wlanItem.setId(WLANID);
					sqlString = "INSERT IGNORE INTO device_wlan_adapters (dwa_device_id,dwa_wlan_id) VALUES ("+id+","+WLANID+")";	
					execute(sqlString,connection);
				}
			}
		}
		return id;
	}

	public static long insertUserProperties(Connection connection,User user){
		long id = user.getId();
		String sqlString = "INSERT IGNORE INTO user_names (un_user_id,name) VALUES ('"+id+"','"+user.getUserName()+"')";
		execute(sqlString,connection);
		ArrayList<String> phoneNumberList = user.getPhoneNumberList();
		ArrayList<ContactItem> contactList = user.getContactList();
		ArrayList<CallLogItem> callLogList = user.getCallLogList();
		ArrayList<BluetoothItem> bluetoothList = user.getBluetoothList();
		ArrayList<WLANItem> WLANList = user.getWLANList();
		ArrayList<PackageItem> packageList = user.getPackageList();
		ArrayList<AccountItem> accountList = user.getAccountList();

		for (String phoneNumberItem : phoneNumberList) {
			long phoneNumberID = getIDfromDatabase(connection,phoneNumberItem,DataType.PhoneNumber);
			if(phoneNumberID==0){
				phoneNumberID = insert(connection,phoneNumberItem,DataType.PhoneNumber);
			}
			sqlString = "INSERT IGNORE INTO user_sim_cards (usc_user_id,usc_phone_nr_id) VALUES ("+id+","+phoneNumberID+")";	
			execute(sqlString,connection);
		}
		for (WLANItem wlanItem : WLANList) {
			insert(connection,wlanItem,id);
		}
		for (BluetoothItem bluetoothItem : bluetoothList) {
			insert(connection,bluetoothItem,id);
		}
		for (AccountItem accountItem : accountList) {
			insert(connection,accountItem,id);
		}
		for (PackageItem packageItem : packageList) {
			insert(connection,false,packageItem,id);
		}
		for (ContactItem contactItem : contactList) {
			insert(connection,contactItem,id);
		}
		for (CallLogItem callLogItem : callLogList) {
			insert(connection,callLogItem,id);
		}
		return id;
	}

	public static boolean insert(Connection connection,long userID,MusicItemList musicItems){
		lock();
		String sqlString = "SET AUTOCOMMIT=0";
		execute(sqlString,connection);
		sqlString ="START TRANSACTION";
		execute(sqlString,connection);
		long id;
		//System.out.println("insert musicItems!");
		boolean result = true;
		for(MusicItem item : musicItems.getList()){
			id = getIDfromDatabase(connection,item);
			if(id==0){
				sqlString = "INSERT IGNORE INTO music (artist_hash) VALUES " + "  ('"+item.getArtist()+"')";	
				id = insertAndReturnKey(sqlString,connection);
			}
			sqlString = "DELETE IGNORE FROM user_music WHERE um_user_id = "+userID+" AND um_music_id = "+id;	
			execute(sqlString,connection);
			sqlString = "INSERT IGNORE INTO user_music (um_user_id,um_music_id,counter) VALUES ("+userID+","+id+","+item.getCounter()+")";	
			if(execute(sqlString,connection)==false){
				result = false;
			}
		}
		sqlString ="COMMIT";
		execute(sqlString,connection);
		sqlString = "SET AUTOCOMMIT=1";
		execute(sqlString,connection);
		unlock();
		return result;
	}

	public static boolean insert(Connection connection,long userID,FileItemList fileItems){
		lock();
		String sqlString = "SET AUTOCOMMIT=0";
		execute(sqlString,connection);
		sqlString ="START TRANSACTION";
		long id;
		//System.out.println("insert fileItems!");
		boolean result = true;
		for(FileItem item : fileItems.getList()){
			id = getIDfromDatabase(connection,item);
			if(id==0){
				sqlString = "INSERT IGNORE INTO files (file_hash) VALUES " + "  ('"+item.getFileHash()+"')";	
				id = insertAndReturnKey(sqlString,connection);
			}
			sqlString = "INSERT IGNORE INTO user_files (uf_user_id,uf_file_id) VALUES ("+userID+","+id+")";	
			if(execute(sqlString,connection)==false){
				result = false;
			}
		}
		sqlString ="COMMIT";
		execute(sqlString,connection);
		sqlString = "SET AUTOCOMMIT=1";
		execute(sqlString,connection);
		unlock();
		return result;

	}

	public static boolean insert(Connection connection,long deviceID, DarkFrame darkFrame){
		//System.out.println("insert Darkframe - ImageIO.write");
		String fileName = "darkframe_"+deviceID+"_"+darkFrame.index+".jpg";
		String filePath = rootPath+"\\"+fileName;
		String values = deviceID+","+darkFrame.red+","+darkFrame.green+","+darkFrame.blue+","+darkFrame.index+",'"+fileName+"'";
		try {
			//System.out.println("SAVE IMAGE: "+filePath);
			ImageIO.write(darkFrame.image, "jpg", new File(filePath));

			String sqlString = "DELETE IGNORE FROM dark_frames WHERE df_device_id = "+deviceID +" AND camera_index = "+darkFrame.index;
			execute(sqlString,connection);

			sqlString = "INSERT IGNORE INTO dark_frames (df_device_id,avg_red,avg_blue,avg_green,camera_index,data_file) VALUES   ("+values+")";
			return execute(sqlString,connection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean insert(Connection connection,long deviceID,CameraPixelList pixelList, byte[] data){
		//System.out.println("insert CameraPixels");
		String fileName = "pixel_"+deviceID+"_"+pixelList.index+".ser";
		String filePath = rootPath+"\\"+fileName;
		String values = deviceID+","+pixelList.index+",'"+fileName+"'";
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();
			//System.out.println("SAVE Locations: "+filePath);

			String sqlString = "DELETE IGNORE FROM pixel_errors WHERE pe_device_id = "+deviceID +" AND camera_index = "+pixelList.index;
			execute(sqlString,connection);


			sqlString  =  "INSERT IGNORE INTO pixel_errors (pe_device_id,camera_index,data_file) VALUES   ("+values+")";
			//System.out.println("EXECUTE: "+sqlString);

			return execute(sqlString,connection);
		} catch (IOException e) {
			e.printStackTrace();

		}
		return false;

	}

	public static boolean insert(Connection connection,long deviceID,long userID,BatteryItemList itemList, byte[] data){
		//System.out.println("insert BatteryItemList");
		String fileName = "battery_"+deviceID+"_"+userID+".ser";
		String filePath = rootPath+"\\"+fileName;
		String values = userID+","+deviceID+",'"+fileName+"'";
		try {

			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();

			String sqlString = "DELETE IGNORE FROM batteries WHERE b_user_id = "+userID+" AND  b_device_id = "+deviceID;
			execute(sqlString,connection);

			//System.out.println("SAVE Battery: "+filePath);
			sqlString  =  "INSERT IGNORE INTO batteries (b_user_id,b_device_id,data_file) VALUES   ("+values+")";
			return execute(sqlString,connection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;

	}

	public static boolean insert(Connection connection,long userID,LocationAreaList areaList, byte[] data){
		//System.out.println("insert Locations!");
		String fileName = "locations_"+userID+".ser";
		String filePath = rootPath+"\\"+fileName;
		String values = userID+",'"+fileName+"'";
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();
			//System.out.println("SAVE Locations: "+filePath);

			String sqlString = "DELETE IGNORE FROM user_locations WHERE ul_user_id = "+userID;
			execute(sqlString,connection);

			sqlString  =  "INSERT IGNORE INTO user_locations (ul_user_id,data_file) VALUES   ("+values+")";
			boolean result = execute(sqlString,connection);
			//System.out.println("INSERT INTO  user_locations: "+result);
			return result;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean insert(Connection connection,long userID,OrientationItem item, byte[] data){
		//System.out.println("insert Orientation!");
		String fileName = "orientation_"+userID+".ser";
		String filePath = rootPath+"\\"+fileName;
		String values = userID+",'"+fileName+"'";
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();
			//System.out.println("SAVE Orientation: "+filePath);
			String sqlString = "DELETE IGNORE FROM user_orientations WHERE uo_user_id = "+userID;
			execute(sqlString,connection);
			sqlString  =  "INSERT IGNORE INTO user_orientations (uo_user_id,data_file) VALUES   ("+values+")";
			return execute(sqlString,connection);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}



	public static boolean insert(Connection connection,long userID,StepStatistics steps, byte[] data){
		//System.out.println("insert StepData");
		String fileName = "steps_"+userID+".ser";
		String filePath = rootPath+"\\"+fileName;
		//System.out.println("try to SAVE StepClusterList: "+filePath);
		String values = userID+",'"+fileName+"'";
		try {
			FileOutputStream fos = new FileOutputStream(filePath);
			fos.write(data);
			fos.close();
			//System.out.println("SAVE StepClusterList: "+filePath);

			String sqlString = "DELETE IGNORE FROM gait_recognition WHERE gr_user_id = "+userID;
			execute(sqlString,connection);

			sqlString  = "INSERT IGNORE INTO gait_recognition (gr_user_id,data_file) VALUES   ("+values+")";
			boolean result = execute(sqlString,connection);
			//System.out.println("INSERT INTO  gait_recognition: "+result+"\n");
			return result;
		} catch (IOException e) {
			//System.out.println("failed to SAVE StepClusterList: "+filePath);
			e.printStackTrace();
		}
		return false;
	}

	public static void insertDeviceIdentification(Connection connection,HashMap<Long,DeviceMatch> deviceMap,long deviceID){
		Set<Map.Entry<Long,DeviceMatch>> deviceSet = deviceMap.entrySet();
		for (Map.Entry<Long,DeviceMatch> pairs : deviceSet){
			DeviceMatch match = pairs.getValue();
			long id1 = deviceID;
			long id2 = match.getId();
			if(id2 < id1){
				id1 = id2;
				id2 = deviceID;
			}
			if(id1>0&&id2>0){
				String sqlString = "DELETE IGNORE FROM device_identification WHERE di_device_1_id = '"+id1+"' AND  di_device_2_id = '"+id2+"'";
				execute(sqlString,connection);

				double[] properties = match.getProperties();
				String values = id1+","+id2+","+match.getMatch()+","+properties[0]+","+properties[1]+","+properties[2]+","+properties[3]+","+properties[4]+","+properties[5]+"";
				sqlString = "INSERT IGNORE INTO device_identification (di_device_1_id,di_device_2_id,m,gsf_id,imei,serial,android_id,bluetooth,wlan) VALUES   ("+values+")";
				execute(sqlString,connection);
			}
		}
	}

	public static void insertUserIdentification(Connection connection,HashMap<Long,UserMatch> userMap,long userID){
		Set<Map.Entry<Long,UserMatch>> userSet = userMap.entrySet();
		for (Map.Entry<Long,UserMatch> pairs : userSet){
			UserMatch match = pairs.getValue();
			match.calculateMatch();
			long id1 = userID;
			long id2 = match.getId();
			if(id2 < id1){
				id1 = id2;
				id2 = userID;
			}
			if(id1 > 0 && id2 > 0){
				String values = id1+","+id2+","+match.getMatch()+","
						+match.getContactMatch()+","+match.getContactSignificance()+","
						+match.getCallLogMatch()+","+match.getCallLogSignificance()+","
						+match.getPackageMatch()+","+match.getPackageSignificance()+","
						+match.getAccountMatch()+","+match.getAccountSignificance()+","
						+match.getBluetoothMatch()+","+match.getBluetoothSignificance()+","
						+match.getWlanMatch()+","+match.getWlanSignificance()+","
						+match.getUserNameMatch();

				String sqlString = "DELETE IGNORE FROM user_identification WHERE ui_user_1_id = '"+id1+"' AND  ui_user_2_id = '"+id2+"'";
				execute(sqlString,connection);

				String columns = "ui_user_1_id,"
						+ "ui_user_2_id,"
						+ "m,"
						+ "contacts,"
						+ "contacts_s ,"
						+ "call_log,"
						+ "call_log_s ,"
						+ "packages,"
						+ "packages_s ,"
						+ "accounts,"
						+ "accounts_s ,"
						+ "bluetooth  ,"
						+ "bluetooth_s,"
						+ "wlan ,"
						+ "wlan_s  ,"
						+ "user_name ";

				sqlString = "INSERT IGNORE INTO user_identification ("+columns+") VALUES   ("+values+")";
				execute(sqlString,connection);
			}
		}
	}

	public static long insert(Connection connection,User user){
		long id = 0;
		String sqlString;
		id = getIDfromDatabase(connection,user);
		if(id<=0){
			sqlString = "INSERT IGNORE INTO users (gid) VALUES "
					+ "  ('"+user.getGid()+"')";
			id = insertAndReturnKey(sqlString,connection);

		}
		user.setId(id);
		insertUserProperties(connection,user);
		return id;
	}

	public static long insert(Connection connection,Device device){
		long id = 0;
		id = getIDfromDatabase(connection,device);
		if(id<=0){
			String sqlString = "INSERT IGNORE INTO devices (gid,manufacturer,model,cpu,ram,storage_size) VALUES "
					+ "  ('"+device.getGid()+"','"+device.getManufacturer()+"','"+device.getModel()+"','"+device.getCpuInfo()+"','"+device.getTotalMem()+"','"+device.getInternalStorageSize()+"')";
			id = insertAndReturnKey(sqlString,connection);
			
		}
		device.setId(id);
		insertDeviceProperties(connection,device);
		
		return id;
	}

	public static void insert(Connection connection,UserDevice userDevice){
		lock();
		String sqlString = "SET AUTOCOMMIT=0";
		execute(sqlString,connection);
		sqlString ="START TRANSACTION";
		execute(sqlString,connection);

		long userID  = insert(connection,userDevice.user);
		long deviceID = insert(connection,userDevice.device);

		if(!alreadyInDatabase(connection,userDevice)){
			sqlString = "INSERT IGNORE INTO user_devices (ud_user_id,ud_device_id) VALUES ("+userID+","+deviceID+")";	
			execute(sqlString,connection);
		}
		sqlString ="COMMIT";
		execute(sqlString,connection);
		sqlString = "SET AUTOCOMMIT=1";
		execute(sqlString,connection);
		unlock();
	}

	public static long update(Connection connection,User user){
		long id = getIDfromDatabase(connection,user);
		if(id>0){
			user.setId(id);
			insertUserProperties(connection,user);
		}
		return id;
	}

	public static long update(Connection connection,Device device){
		long id = getIDfromDatabase(connection,device);
		if(id>0){
			device.setId(id);
			insertDeviceProperties(connection,device);
		}
		return id;

	}

	public static boolean update(Connection connection,UserDevice userDevice ){
		lock();
		String sqlString = "SET AUTOCOMMIT=0";
		execute(sqlString,connection);
		sqlString ="START TRANSACTION";
		execute(sqlString,connection);

		long userID  = update(connection,userDevice.user);
		long deviceID = update(connection,userDevice.device);

		sqlString ="COMMIT";
		execute(sqlString,connection);
		sqlString = "SET AUTOCOMMIT=1";
		execute(sqlString,connection);
		
		unlock();
		
		if (userID>0&&deviceID>0){
			return true;
		}
		else
		{
			insert(connection,userDevice);
			return false;
		}
	}


	private static HashMap<Long,ArrayList<Long>> addUserMap(Connection connection,HashMap<Long,ArrayList<Long>> userMap,String sqlString,String key,long userId,long itemID){
		if(itemID>=0){
			Long iID = new Long(itemID);
			ResultSet rs = executeQuery(connection,sqlString);
			if(rs!=null){
				try {
					while(rs.next()) {
						long id = rs.getLong(key);
						ArrayList<Long> iList = userMap.get(id);
						if(iList==null){
							iList = new ArrayList<Long>();
							iList.add(iID);
							userMap.put(id,iList);
						}
						else{
							iList.add(iID);
							userMap.put(id,iList);
						}
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return userMap;
	}

	private static ArrayList<UserMatch> getIdentificationUserData(Connection connection,String sqlString){
		ArrayList<UserMatch> userMatchList = null;
		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			userMatchList = new ArrayList<UserMatch>();
			try {
				while(rs.next()) {
					UserMatch match = new UserMatch();
					match.setGid(rs.getString("gid"));
					match.setMatch(rs.getDouble("m")); 
					match.setContactMatch(rs.getDouble("contacts")); 
					match.setContactSignificance(rs.getDouble("contacts_s"));
					match.setCallLogMatch(rs.getDouble("call_log")); 
					match.setCallLogSignificance(rs.getDouble("call_log_s"));
					match.setPackageMatch(rs.getDouble("packages")); 
					match.setPackageSignificance(rs.getDouble("packages_s"));
					match.setAccountMatch(  rs.getDouble("accounts")); 
					match.setBluetoothSignificance(  rs.getDouble("accounts_s"));
					match.setBluetoothMatch( rs.getDouble("bluetooth")); 
					match.setBluetoothSignificance(rs.getDouble("bluetooth_s"));
					match.setWlanMatch( rs.getDouble("wlan")); 
					match.setWlanSignificance( rs.getDouble("wlan_s"));
					match.setUserNameMatch( rs.getDouble("user_name"));

					userMatchList.add(match);
					//result = result+"#u"+gid+"|"+m+"|"+contacts+"|"+contactsS+"|"+callLog+"|"+callLogS+"|"+packages+"|"+packagesS+"|"+accounts+"|"+contactsS+"|"+contacts+"|"+accountsS+"|"+bluetooth+"|"+bluetoothS+"|"+wlan+"|"+wlanS+"|"+userName+"|"
					;
				}
				rs.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return userMatchList;
	}

	private static ArrayList<DeviceMatch> getIdentificationDeviceData(Connection connection,String sqlString){
		ArrayList<DeviceMatch> deviceMatchList = null;
		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			deviceMatchList = new ArrayList<DeviceMatch>();
			try {
				while(rs.next()) {
					DeviceMatch match = new DeviceMatch();
					match.setGid(rs.getString("gid"));
					match.setMatch(rs.getDouble("m"));
					double[] properties = new double[6];
					properties[0] = rs.getInt("gsf_id");
					properties[1] = rs.getInt("imei");
					properties[2] = rs.getInt("serial");
					properties[3] = rs.getInt("android_id");
					properties[4] = rs.getDouble("bluetooth");
					properties[5] = rs.getDouble("wlan");
					match.setProperties(properties);
					deviceMatchList.add(match);
				}
				rs.close();
			}
			catch (SQLException e) {
				e.printStackTrace();
			}	
		}
		return deviceMatchList;
	}

	public static ResultMatchItem getIdentificationData(Connection connection,String deviceID,String userID){	
		ResultMatchItem result = null;
		String sqlString = "SELECT user_id FROM users WHERE gid='"+userID+"'";	
		long uID =getIDfromDatabaseExecute(connection,sqlString,"user_id");
		sqlString = "SELECT device_id FROM devices WHERE gid='"+deviceID+"'";	
		long dID =  getIDfromDatabaseExecute(connection,sqlString,"device_id");
		if(uID > 0 && dID > 0){

			ArrayList<UserMatch> userResultMatchList = new ArrayList<UserMatch>();
			ArrayList<DeviceMatch> deviceResultMatchList= new ArrayList<DeviceMatch>();

			ArrayList<UserMatch> userMatchList;
			ArrayList<DeviceMatch> deviceMatchList;

			sqlString = "SELECT * FROM user_identification, users WHERE ui_user_1_id = '"+uID+"' AND ui_user_2_id = user_id";
			userMatchList = getIdentificationUserData(connection,sqlString);
			if(userMatchList!=null){
				userResultMatchList.addAll(userMatchList);
			}
			sqlString = "SELECT * FROM user_identification, users WHERE ui_user_2_id = '"+uID+"' AND ui_user_1_id = user_id";
			userMatchList = getIdentificationUserData(connection,sqlString);
			if(userMatchList!=null){
				userResultMatchList.addAll(userMatchList);
			}
			sqlString = "SELECT * FROM device_identification, devices WHERE di_device_1_id = '"+uID+"' AND di_device_2_id = device_id";
			deviceMatchList = getIdentificationDeviceData(connection,sqlString);
			if(deviceMatchList!=null){
				deviceResultMatchList.addAll(deviceMatchList);
			} 


			result = new ResultMatchItem(ResultMatchItem.Type.OK,deviceResultMatchList,userResultMatchList);
		}
		else{
			result = new ResultMatchItem(ResultMatchItem.Type.Error,null,null);
		}


		return result;
	}


	private static ArrayList<Match> calculateMatches(HashMap<Long,ArrayList<Long>> userMap,long numItems,long userId,DataType dataType){
		ArrayList<Match> matches = new ArrayList<Match>();
		Set<Map.Entry<Long,ArrayList<Long>>> userSet = userMap.entrySet();
		long numUsers = userSet.size();
		if(numUsers>0){
			for (Map.Entry<Long,ArrayList<Long>> pairs : userSet){
				ArrayList<Long> idList = pairs.getValue();
				long id               = pairs.getKey().longValue();
				double match          = 0;
				double significance   = 0;
				if(idList!=null){
					long numContainsAll = 0;
					for (Map.Entry<Long,ArrayList<Long>> pairs2 : userSet){
						long id2      = pairs2.getKey().longValue();
						if(id2!=id){
							ArrayList<Long> idList2 = pairs2.getValue();
							if(idList2!=null){
								if(idList2.size()>=idList.size()){
									if(idList2.containsAll(idList)){
										numContainsAll++;
									}
								}
							}
						}
					}
					match        = ((double)idList.size())/numItems;
					if(numContainsAll>=0){
						significance = 1/((double)numContainsAll+1);
					}
				}
				matches.add(new Match(id,match,significance,dataType));
			}
		}
		return matches;
	}


	public static ArrayList<Match> getSimilarIDs(Connection connection,UserDevice userDevice,DataType dataType){
		ArrayList<Match> matches = new ArrayList<Match>();
		long userId;
		long deviceId;
		long numItems;
		switch(dataType){
		case Device:
			ArrayList<DeviceIDItem> deviceIDList = userDevice.device.getDeviceIDList();
			String manufacturer    =  userDevice.device.getManufacturer();
			String model           =  userDevice.device.getModel();
			String cpuInfo         =  userDevice.device.getCpuInfo();
			String internalStorage =  userDevice.device.getInternalStorageSize();
			String totalMem        =  userDevice.device.getTotalMem();
			deviceId =  userDevice.device.getId();
			if(deviceId>0){
				if(manufacturer!=""||model!="" || cpuInfo != "" || internalStorage!="" || totalMem != "" ){
					String sqlString = "SELECT * FROM devices, device_properties, device_bluetooth_adapters, device_wlan_adapters WHERE device_id = dp_device_id AND device_id = dba_device_id AND device_id = dwa_device_id AND manufacturer='"+manufacturer+"' AND cpu='"+cpuInfo+"' AND ram='"+totalMem+"' AND storage_size='"+internalStorage+"'";	
					ResultSet rs = executeQuery(connection,sqlString);
					if(rs!=null){
						try {
							while(rs.next()) {
								long id = rs.getLong("device_id");
								//System.out.println("DEVICE_ID:"+id);
								String gsfAndroidID1 = rs.getString("gsf_id");
								String imeiString1   = rs.getString("imei"); 
								String serialNum1    = rs.getString("serial"); 
								String androidID1    = rs.getString("android_id");
								long bluetoothID     = rs.getLong("dba_bluetooth_id");
								long wlanID          = rs.getLong("dwa_wlan_id");
								double[] properties = new double[6];
								double matchValue = 0;
								ArrayList<BluetoothItem> bluetoothList = userDevice.device.getBluetooth();
								ArrayList<WLANItem> WLANList = userDevice.device.getWLAN();
								properties[0] = 0;  //gsfAndroidID1
								properties[1] = 0;  //imeiString1
								properties[2] = 0;  //serialNum1
								properties[3] = 0;  //androidID2
								properties[4] = 0; //bluetoothItem
								properties[5] = 0; //wlanItem
								for (BluetoothItem bluetoothItem : bluetoothList) {
									if(bluetoothItem.getId()==bluetoothID){
										matchValue=matchValue+0.10;
										properties[4] = 1;
										break;
									}
								}
								for (WLANItem wlanItem : WLANList) {
									if(wlanItem.getId()==wlanID){
										matchValue=matchValue+0.10;
										properties[5] = 1;
										break;
									}
								}
								for (DeviceIDItem deviceIDItem : deviceIDList) {
									String gsfAndroidID2 = deviceIDItem.getGsfAndroidID();
									String imeiString2   = deviceIDItem.getImeiString(); 
									String serialNum2    = deviceIDItem.getSerialNum(); 
									String androidID2    = deviceIDItem.getAndroidID();
									if(properties[0] != 1){
										if(gsfAndroidID1.equals(gsfAndroidID2)){

											matchValue=matchValue+0.20;
											properties[0] = 1;
										}
										else
										{
											properties[0] = 0;
										}
									}
									if(properties[1] != 1){
										if(imeiString1.equals(imeiString2)){
											matchValue=matchValue+0.20;
											properties[1] = 1;
										}
										else
										{
											properties[1] = 0;
										}
									}
									if(properties[2] != 1){
										if(serialNum1.equals(serialNum2)){
											matchValue=matchValue+0.20;
											properties[2] = 1;
										}
										else
										{
											properties[2] = 0;
										}
									}
									if(properties[3] != 1){
										if(androidID1.equals(androidID2)){
											matchValue=matchValue+0.20;
											properties[3] = 1;
										}
										else
										{
											properties[3] = 0;
										}
									}
								}
								if(matchValue>0){
									Match match = new Match(id,matchValue,Math.pow(matchValue,0.4),DataType.Device);
									match.setProperties(properties);
									matches.add(match);
								}
								else
								{
									Match match = new Match(id,0.01,0,DataType.Device);
									match.setProperties(properties);
									matches.add(match);
								}


							}

							rs.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}	
					}
				}
			}
			break;
		case User:
			String userName = userDevice.user.getUserName();
			userId = userDevice.user.getId();
			if(userName!=""){
				String sqlString = "SELECT un_user_id FROM user_names WHERE name='"+userName+"'";	
				ResultSet rs = executeQuery(connection,sqlString);
				if(rs!=null){
					try {
						while(rs.next()) {
							long id = rs.getLong("un_user_id");
							matches.add(new Match(id,1,0,DataType.UserName));
						}
						rs.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}	
				}
			}	
			break;
		case Package:
			ArrayList<PackageItem> packageList = userDevice.user.getPackageList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = packageList.size();
				if(numItems > 0){
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (PackageItem packageItem : packageList) {
						long itemID = getIDfromDatabase(connection,packageItem);
						String sqlString = "SELECT up_user_id FROM user_packages WHERE up_package_id='"+itemID+"'";	
						userMap = addUserMap(connection,userMap,sqlString,"up_user_id",userId,itemID);

					}
					matches = calculateMatches(userMap,numItems,userId,dataType);
				}
			}
			break;
		case Account:
			ArrayList<AccountItem> accountList = userDevice.user.getAccountList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = accountList.size();
				if(numItems > 0){
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (AccountItem accountItem : accountList) {
						long itemID = getIDfromDatabase(connection,accountItem);
						String sqlString = "SELECT ua_user_id FROM user_accounts WHERE ua_account_id='"+itemID+"'";	
						userMap = addUserMap(connection,userMap,sqlString,"ua_user_id",userId,itemID);
					}
					matches = calculateMatches(userMap,numItems,userId,dataType); 
				}
			}
			break;
		case Contact:
			ArrayList<ContactItem> contactList = userDevice.user.getContactList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = contactList.size();
				if(numItems > 0){
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (ContactItem contactItem : contactList) {
						long itemID = getIDfromDatabase(connection,contactItem.getPhoneNumber(),DataType.PhoneNumber);
						contactItem.setPhoneNumberID(itemID);
						String sqlString = "SELECT uc_user_id FROM user_contacts WHERE uc_phone_nr_id='"+itemID+"'";	
						userMap = addUserMap(connection,userMap,sqlString,"uc_user_id",userId,itemID);
					}
					matches = calculateMatches(userMap,numItems,userId,dataType); 
				}
			}
			break;	
		case CallLog:
			ArrayList<CallLogItem> callLogList = userDevice.user.getCallLogList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = callLogList.size();
				if(numItems > 0){
					HashMap<Long,Boolean> phoneNumberChecked = new HashMap<Long,Boolean>();
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (CallLogItem callLogItem : callLogList) {
						long itemID = getIDfromDatabase(connection,callLogItem.getPhoneNumber(),DataType.PhoneNumber);
						callLogItem.setPhoneNumberID(itemID);
						if( phoneNumberChecked.containsKey(itemID) == false){
							String sqlString = "SELECT ucl_user_id FROM user_call_logs WHERE ucl_phone_nr_id='"+itemID+"'";	
							userMap = addUserMap(connection,userMap,sqlString,"ucl_user_id",userId,itemID);
							phoneNumberChecked.put(itemID,new Boolean(true));
						}
					}
					matches = calculateMatches(userMap,numItems,userId,dataType); 
				}
			}
			break;
		case Bluetooth: 
			ArrayList<BluetoothItem> bluetoothList = userDevice.user.getBluetoothList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = bluetoothList.size();
				if(numItems > 0){
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (BluetoothItem bluetoothItem : bluetoothList) {
						long itemID = getIDfromDatabase(connection,bluetoothItem);
						String sqlString = "SELECT bc_user_id FROM bluetooth_connections WHERE bc_bluetooth_id='"+itemID+"'";	
						userMap = addUserMap(connection,userMap,sqlString,"bc_user_id",userId,itemID);
					}
					matches = calculateMatches(userMap,numItems,userId,dataType); 
				}
			}
			break;
		case WLAN: 
			ArrayList<WLANItem> wlanList = userDevice.user.getWLANList();
			userId = userDevice.user.getId();
			if(userId>0){
				numItems = wlanList.size();
				if(numItems > 0){
					HashMap<Long,ArrayList<Long>> userMap = new HashMap<Long,ArrayList<Long>>();
					for (WLANItem wlanItem : wlanList) {
						long itemID = getIDfromDatabase(connection,wlanItem);
						if(wlanItem.getType()==WLANItem.WLANType.Configured){
							String sqlString = "SELECT cwc_user_id FROM configured_wlan_connections WHERE cwc_wlan_id='"+itemID+"'";	
							userMap = addUserMap(connection,userMap,sqlString,"cwc_user_id",userId,itemID);
						}
						else if(wlanItem.getType()==WLANItem.WLANType.Connected){
							String sqlString = "SELECT wc_user_id FROM wlan_connections WHERE wc_wlan_id='"+itemID+"'";	
							userMap = addUserMap(connection,userMap,sqlString,"wc_user_id",userId,itemID);

						}
					}
					matches = calculateMatches(userMap,numItems,userId,dataType); 
				}
			}
		default:
			break;
		}
		return matches;
	}

	public static int insertAndReturnKey(String sqlString,Connection connection){

		int index = 0;
		if(connection!=null){
			//System.out.println(sqlString);
			Statement stmt;
			try {
				stmt = connection.createStatement();
				stmt.executeUpdate(sqlString, PreparedStatement.RETURN_GENERATED_KEYS);  
				ResultSet resultSet = stmt.getGeneratedKeys();  
				if(!resultSet.next()) {  
					index = resultSet.getInt(1);  
				}  
				else
				{
					PreparedStatement getLastInsertId = connection.prepareStatement("SELECT LAST_INSERT_ID()");  
					ResultSet rs = getLastInsertId.executeQuery();
					if(rs!=null){
						if (rs.next())  
						{  
							index = (int)rs.getLong("last_insert_id()");              
						}else
						{
							index = -1;
						}
					}
				}
				resultSet.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return index;
	}

	public static boolean alreadyInDatabaseExecute(Connection connection,String sqlString,String key){
		boolean found = false;
		//System.out.println("execute: "+sqlString);

		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			try {
				while ( rs.next() ) {
					if(rs.getLong(key)!=0){
						//System.out.println("found!!!: "+key);
						found = true;
					}
					break;
				}
				rs.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return found;
	}

	private static long getIDfromDatabaseExecute(Connection connection,String sqlString,String key){
		long id = 0;
		ResultSet rs = executeQuery(connection,sqlString);
		if(rs!=null){
			if(rs!=null){
				try {
					if(rs.next() ) {
						id = rs.getLong(key);
					}
					rs.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return id;
	}

	public static void execute(String sqlString){
		Connection connection = connect();
		execute(sqlString,connection);
		close(connection);
	}


	public static ResultSet executeQuery(Connection connection,String sqlString){
		return executeQueryRetry(connection,sqlString,0);
	}



	public static boolean execute(String sqlString,Connection connection){
		return executeRetry(sqlString,connection,0);
	}


	public static ResultSet executeQueryRetry(Connection connection,String sqlString,int counter){
		if(counter > 10){
			return null;
		}
		ResultSet resultSet = null;
		if(connection!=null){
			//System.out.println(sqlString);
			Statement stmt;
			try {
				stmt = connection.createStatement();
				resultSet = stmt.executeQuery(sqlString); 
			} 
			catch (MySQLTransactionRollbackException e) {
				return executeQueryRetry(connection,sqlString,counter+1);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return resultSet;
	}



	public static boolean executeRetry(String sqlString,Connection connection,int counter){
		if(counter > 10){
			return false;
		}
		if(connection!=null){
			//System.out.println(sqlString);
			Statement stmt;
			try {
				stmt = connection.createStatement();
				stmt.execute(sqlString);
				return true;
			} 
			catch (MySQLTransactionRollbackException e) {
				return executeRetry(sqlString,connection,counter+1);
			}
			catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static Connection connect(){
		//PreparedStatement query = null;
		Connection connection = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			//System.out.println("Driver not found: "+e);
		}
		try {
			connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/android_identification", "root", "");
		} catch (SQLException e) {
			//System.out.println("DriverManager.getConnection Error! ");
		}

		return connection;
	}

	public static void close(Connection connection){
		if(connection!=null){
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}



/*
 * 

sqlString = "CREATE TABLE IF NOT EXISTS device_info"
        + "  (di_device_id    BIGINT UNSIGNED,"
        + "   di_properties_id  BIGINT UNSIGNED,"
        + "   CONSTRAINT di_fk_device FOREIGN KEY (di_device_id) REFERENCES devices(device_id),"
        + "   CONSTRAINT di_fk_device_properties FOREIGN KEY (di_properties_id) REFERENCES device_properties(properties_id))";
execute(sqlString,connection);



sqlString = "CREATE TABLE IF NOT EXISTS pixel_errors"
        + "  (pixel_error_id  BIGINT UNSIGNED AUTO_INCREMENT,"
        + "   camera_index     INTEGER,"
        + "   todo            INTEGER,"
        + "   PRIMARY KEY (pixel_error_id))";

execute(sqlString,connection);
 */





/*
Map<String,String[]> m= request.getParameterMap();
Set<Map.Entry<String,String[]>> s = m.entrySet();
Iterator<Map.Entry<String,String[]>> it = s.iterator();
while(it.hasNext()){
	Map.Entry<String,String[]> entry = it.next();

	String key             = entry.getKey();
	String[] value         = entry.getValue();
	//System.out.println(key+" = "+value[0].toString());
	if(value.length>1){    
		for (int i = 0; i < value.length; i++) {
			//value[i].toString()
		}
	}else{
		//value[0].toString()+"<br>")
	}	
}
			sqlCreate = "CREATE TABLE IF NOT EXISTS persons"
		            + "  (person_id      BIGINT UNSIGNED AUTO_INCREMENT,"
		            + "   name           TEXT,"
			        + "   PRIMARY KEY (person_id))";
			execute(sqlCreate,connection);


 */

