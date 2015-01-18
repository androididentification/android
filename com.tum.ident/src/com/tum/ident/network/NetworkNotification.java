package com.tum.ident.network;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;

public class NetworkNotification implements Runnable {

	private static final String TAG = "NetworkNotification";

	private String url = "";

	public void post(String url) {
		this.url = url;
		new Thread(this).start();
	}

	@Override
	public void run() {
		// Log.v(TAG, "post: "+url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		nameValuePairs.add(new BasicNameValuePair("notification", "true"));
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(url);
		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			httpclient.execute(httppost);
			Log.v(TAG, url);
		} catch (UnsupportedEncodingException e) {
			Log.v(TAG, "UnsupportedEncodingException", e);
		} catch (ClientProtocolException e) {
			Log.v(TAG, "ClientProtocolException", e);
		} catch (IOException e) {
			Log.v(TAG, "IOException", e);
		} catch (Exception e) {
			Log.v(TAG, "Exception", e);
		}

	}

}
