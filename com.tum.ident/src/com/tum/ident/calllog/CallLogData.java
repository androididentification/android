package com.tum.ident.calllog;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.tum.ident.user.CallLogItem;
import com.tum.ident.util.HashGenerator;

public class CallLogData {

	public static ArrayList<CallLogItem> getCallLogData(Context context) {
		ArrayList<CallLogItem> callLogList = new ArrayList<CallLogItem>();
		Cursor managedCursor = context.getContentResolver().query(
				CallLog.Calls.CONTENT_URI, null, null, null, null);
		int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
		int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
		int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
		int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

		while (managedCursor.moveToNext()) {
			String phoneNumber = HashGenerator.hash(managedCursor
					.getString(number));
			// Log.v(TAG,"NR in LIST: "+phNumber);
			String callType = managedCursor.getString(type);
			String callDate = managedCursor.getString(date);
			String callDuration = managedCursor.getString(duration);
			callLogList.add(new CallLogItem(HashGenerator.hash(phoneNumber),
					callType, callDate, callDuration));
		}
		return callLogList;
	}

}
