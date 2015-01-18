package com.tum.ident.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.tum.ident.IdentificationListener;
import com.tum.ident.IdentificationService;

public class Broadcast extends BroadcastReceiver implements
		IdentificationListener {
	@Override
	public void onReceive(Context context, Intent intent) {
		IdentificationService.setListener(this);
		Intent startintent = new Intent(context, IdentificationService.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startService(startintent);
	}

	@Override
	public void onReceiveIds(String deviceID, String userID) {
	}

	@Override
	public void onReceiveUpdate() {
	}

	@Override
	public void onReceiveDataSubmitted(boolean result) {
	}
}