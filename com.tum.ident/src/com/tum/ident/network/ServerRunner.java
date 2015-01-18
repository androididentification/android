package com.tum.ident.network;

import java.io.IOException;

public class ServerRunner {
	public static <T> void run(Class<T> serverClass) {
		try {
			executeInstance((NanoHTTPD) serverClass.newInstance());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void executeInstance(NanoHTTPD server) {
		try {
			server.start();
		} catch (IOException ioe) {
			System.err.println("Couldn't start server:\n" + ioe);
			System.exit(-1);
		}

	}

	public static void stopInstance(NanoHTTPD server) {
		server.stop();
	}
}
