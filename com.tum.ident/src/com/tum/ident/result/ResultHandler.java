package com.tum.ident.result;

import android.util.Log;

import com.tum.ident.IdentificationListener;

public class ResultHandler implements ResultListener {

	private final String TAG = "ResultHandler";
	private IdentificationListener listener = null;
	private IdentificationListener internalListener = null;

	public ResultHandler(IdentificationListener listener,
			IdentificationListener internalListener) {
		this.listener = listener;
		this.internalListener = internalListener;
	}

	public boolean getReturnValue(ResultItem resultItem) {
		boolean result = false;
		if (resultItem != null) {
			if (resultItem.getType() == ResultItem.Type.Error) {
				result = false;
			} else if (resultItem.getType() == ResultItem.Type.ID) {
				ResultIDItem resultIDItem = (ResultIDItem) resultItem.getResult();
				Log.v(TAG, "resultIDItem.getType() = " + resultIDItem.getType());
				if (resultIDItem.getType() != ResultIDItem.Type.Error) {
					Log.v(TAG, "resultIDItem.getDeviceID() = "
							+ resultIDItem.getDeviceID());
					Log.v(TAG, "resultIDItem.getUserID() = " + resultIDItem.getUserID());
					if (resultIDItem.getDeviceID() != null
							&& resultIDItem.getUserID() != null) {
						result = true;
					}
				}
			} else if (resultItem.getType() == ResultItem.Type.Match) {
				ResultMatchItem resultMatchItem = (ResultMatchItem) resultItem.getResult();
				if (resultMatchItem.getType() != ResultMatchItem.Type.Error) {
					result = true;
				}
			} else if (resultItem.getType() == ResultItem.Type.Value) {
				ResultValueItem resultValueItem = (ResultValueItem) resultItem.getResult();
				if (resultValueItem.getResult() != ResultValueItem.Type.Error) {
					result = true;
				} else {
					Log.v("DEBUG",
							"resultValueItem.getResult() == ResultValueItem.Type.Error");
				}
			}
		}
		Log.v(TAG, "result = " + result);

		return result;
	}

	@Override
	public boolean onReceive(ResultItem resultItem) {
		Log.v(TAG, "ResultHandler.onReceive!!");
		boolean result = false;
		if (resultItem != null) {
			if (resultItem.getType() == ResultItem.Type.Error) {
				result = false;

			} else if (resultItem.getType() == ResultItem.Type.ID) {
				ResultIDItem resultIDItem = (ResultIDItem) resultItem.getResult();
				if (resultIDItem.getType() != ResultIDItem.Type.Error) {
					Log.v(TAG, "internalListener -> resultIDItem.getDeviceID() = "
							+ resultIDItem.getDeviceID());
					Log.v(TAG, "internalListener -> resultIDItem.getUserID() = "
							+ resultIDItem.getUserID());
					internalListener.onReceiveIds(resultIDItem.getDeviceID(),
							resultIDItem.getUserID());
					if (listener != null) {
						listener.onReceiveIds(resultIDItem.getDeviceID(),
								resultIDItem.getUserID());
					}
					result = true;
				}
			} else if (resultItem.getType() == ResultItem.Type.Match) {
				ResultMatchItem resultMatchItem = (ResultMatchItem) resultItem.getResult();
				if (resultMatchItem.getType() != ResultMatchItem.Type.Error) {
					result = true;
				}

			} else if (resultItem.getType() == ResultItem.Type.Value) {
				ResultValueItem resultValueItem = (ResultValueItem) resultItem.getResult();
				if (resultValueItem.getResult() != ResultValueItem.Type.Error) {
					result = true;
				}

			}
		}
		return result;
	}

	@Override
	public void onReceive(byte[] data) {

	}

	@Override
	public void onReceive(String result) {

	}
}
