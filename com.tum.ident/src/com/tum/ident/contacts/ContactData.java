package com.tum.ident.contacts;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract.CommonDataKinds.Phone;


//import android.util.Log;
import com.tum.ident.user.ContactItem;
import com.tum.ident.util.HashGenerator;

public class ContactData {
	// private static final String TAG = "ContactData";

	public static ArrayList<ContactItem> getContactData(Context context) {
		ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(Phone.CONTENT_URI,
					null, null, null, null);
			int contactIdIdx = cursor.getColumnIndex(BaseColumns._ID);
			// int nameIdx = cursor.getColumnIndex(Phone.DISPLAY_NAME);
			int phoneNumberIdx = cursor.getColumnIndex(Phone.NUMBER);
			cursor.moveToFirst();
			do {
				contactList.add(new ContactItem(HashGenerator.hash(cursor
						.getString(contactIdIdx)), HashGenerator.hash(cursor
						.getString(phoneNumberIdx))));
			} while (cursor.moveToNext());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}


		try
		{
			String ClsSimPhonename = null; 
			String ClsSimphoneNo = null;

			Uri simUri = Uri.parse("content://icc/adn"); 
			Cursor cursorSim = context.getContentResolver().query(simUri,null,null,null,null);

			while (cursorSim.moveToNext()) 
			{      
				ClsSimPhonename =cursorSim.getString(cursorSim.getColumnIndex("name"));
				ClsSimphoneNo = cursorSim.getString(cursorSim.getColumnIndex("number"));
				ClsSimphoneNo.replaceAll("\\D","");
				ClsSimphoneNo.replaceAll("&", "");
				ClsSimPhonename=ClsSimPhonename.replace("|","");
				contactList.add(new ContactItem(HashGenerator.hash(ClsSimPhonename), HashGenerator.hash(ClsSimphoneNo)));
			}        
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}


		
		return contactList;

	}
}
