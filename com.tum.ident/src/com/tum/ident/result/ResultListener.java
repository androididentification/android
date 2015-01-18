package com.tum.ident.result;

public interface ResultListener {
	public boolean onReceive(ResultItem resultItem);

	public void onReceive(byte[] data);

	public void onReceive(String result);
}
