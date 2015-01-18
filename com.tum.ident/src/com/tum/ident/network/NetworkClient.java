package com.tum.ident.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import com.tum.ident.R;

public class NetworkClient {

	public enum DataType {
		String, Byte, Multipart;
	}

	private static final String TAG = "NetworkClient";

	private HttpClient mHttpsClient;
	private HttpClient mHttpClient;

	private static Context context;

	private int timeout = 30000;

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		mHttpsClient = null;
		mHttpClient = null;
	}

	public static void setContext(Context c) {
		context = c;
	}

	public static boolean networkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo wifiNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifiNetwork != null && wifiNetwork.isConnected()) {
			return true;
		}

		NetworkInfo mobileNetwork = cm
				.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if (mobileNetwork != null && mobileNetwork.isConnected()) {
			return true;
		}

		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		if (cm != null) {
			if (activeNetwork != null && activeNetwork.isConnected()) {
				return true;
			}
		}

		return false;
	}

	public List<NameValuePair> createPostParameters(Bundle resultData) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		for (String key : resultData.keySet()) {
			nameValuePairs.add(new BasicNameValuePair(key, resultData.get(key)
					.toString()));
		}
		return nameValuePairs;
	}

	private void createHttpClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		mHttpClient = new DefaultHttpClient(params);
	}

	private void createHttpsClient() {
		HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "utf-8");
		params.setBooleanParameter("http.protocol.expect-continue", false);
		SchemeRegistry registry = new SchemeRegistry();
		registry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), 80));
		SSLSocketFactory socketFactory = null;
		try {
			KeyStore trusted = KeyStore.getInstance("BKS");
			InputStream in = context.getResources().openRawResource(
					R.raw.keystore);
			try {
				trusted.load(in, "222222".toCharArray());
			} finally {
				in.close();
			}
			HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
			// HostnameVerifier hostnameVerifier =
			// org.apache.http.conn.ssl.SSLSocketFactory.STRICT_HOSTNAME_VERIFIER;
			socketFactory = new SSLSocketFactory(trusted);
			socketFactory
					.setHostnameVerifier((X509HostnameVerifier) hostnameVerifier);

		} catch (Exception e) {
			throw new AssertionError(e);
		}
		if (socketFactory != null) {
			registry.register(new Scheme("https", socketFactory, 443));
			ClientConnectionManager manager = new ThreadSafeClientConnManager(
					params, registry);
			mHttpsClient = new DefaultHttpClient(manager, params);
		}
	}

	public byte[] post(String url, HttpEntity postEntity,
			DataType responseType, boolean useKeyStore) {
		byte[] result = null;
		BufferedReader in = null;
		HttpClient client = null;

		try {
			if (useKeyStore == false) {
				if (mHttpClient == null) {
					createHttpClient();
				}
				client = mHttpClient;
				Log.v(TAG, "mHttpClient.execute(request) - NO SSL");
			} else {
				if (mHttpsClient == null) {
					createHttpsClient();
				}
				client = mHttpsClient;
				Log.v(TAG, "mHttpsClient.execute(request) - SSL");
			}
			if (client != null) {
				HttpPost request = new HttpPost(url);
				request.setEntity(postEntity);
				HttpResponse response = client.execute(request);
				if (response != null) {
					if (responseType == DataType.String) {
						in = new BufferedReader(new InputStreamReader(response
								.getEntity().getContent()));
						StringBuffer sb = new StringBuffer("");
						String line = "";
						String NL = System.getProperty("line.separator");
						while ((line = in.readLine()) != null) {
							sb.append(line + NL);
						}
						in.close();
						result = sb.toString().getBytes();
					} else {
						result = EntityUtils.toByteArray(response.getEntity());
					}
				}
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		Log.v(TAG, "result: " + result);
		return result;
	}

	public byte[] postMultipartData(String url, MultipartEntityBuilder builder,
			DataType responseType, boolean useKeyStore) {
		HttpEntity entity = builder.build();
		return post(url, entity, responseType, useKeyStore);
	}

	public byte[] postByteData(String url, byte[] byteParams,
			DataType responseType, boolean useKeyStore) {
		HttpEntity postEntity = new ByteArrayEntity(byteParams);
		return post(url, postEntity, responseType, useKeyStore);
	}

	public byte[] postBundleData(String url, Bundle resultData,
			DataType responseType, boolean useKeyStore) {
		List<NameValuePair> nameValuePairs = createPostParameters(resultData);
		HttpEntity postEntity = null;
		try {
			postEntity = new UrlEncodedFormEntity(nameValuePairs);

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		if (postEntity != null) {
			return post(url, postEntity, responseType, useKeyStore);
		} else {
			return new byte[0];
		}
	}

}
