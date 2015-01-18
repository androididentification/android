package com.tum.ident.realtime;

import java.util.Map;

import android.util.Log;

import com.tum.ident.network.NanoHTTPD;

/**
 * An example of subclassing NanoHTTPD to make a custom HTTP server.
 */
public class RealtimeServer extends NanoHTTPD {
	private final static String TAG = "AndroidServer";

	public RealtimeServer() {
		super(1025);

	}

	@Override
	public Response serve(IHTTPSession session) {
		Method method = session.getMethod();
		String uri = session.getUri();

		String[] parts = uri.split("\\/");
		Log.v(TAG, method + "  -> '" + uri + "' ");
		Log.v(TAG, "parts-len: " + parts.length);
		Log.v(TAG, "parts[0]: " + parts[0]);
		Log.v(TAG, "parts[1]: " + parts[1]);
		if (parts.length == 3) {
			Log.v(TAG, "parts: " + parts[1] + ", " + parts[2]);
		}
		RealtimeData.setMode(parts[1]);
		if (parts[2].equals("1")) {
			RealtimeData.send();
		}

		String msg = "";
		Map<String, String> parms = session.getHeaders();
		String ip = parms.get("http-client-ip");
		RealtimeData.setServerIP(ip);


		return new NanoHTTPD.Response(msg);
	}
}
