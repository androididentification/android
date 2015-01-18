package com.tum.ident.sim;

import android.content.Context;
import android.telephony.TelephonyManager;
//import android.util.Log;

import com.tum.ident.user.SIMItem;
import com.tum.ident.util.HashGenerator;

public class SIMData {

	public static SIMItem getSIMData(Context context) {
		TelephonyManager phoneManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return new SIMItem(HashGenerator.hash(phoneManager.getSubscriberId()),
				HashGenerator.hash(phoneManager.getSimSerialNumber()),
				HashGenerator.hash(phoneManager.getLine1Number()));
	}

	public static String getPhoneNumber(Context context) {
		TelephonyManager phoneManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return HashGenerator.hash(phoneManager.getLine1Number());
	}

	public static String getCarrierName(Context context) {
		TelephonyManager phoneManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return HashGenerator.hash(phoneManager.getNetworkOperatorName());
	}

}
