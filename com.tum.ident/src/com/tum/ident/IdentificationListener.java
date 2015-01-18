package com.tum.ident;

public interface IdentificationListener {

	public void onReceiveIds(String deviceID, String userID);

	public void onReceiveUpdate();

	public void onReceiveDataSubmitted(boolean result);
}
