package com.tum.ident.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import android.content.Context;

import com.tum.ident.accounts.AccountData;
import com.tum.ident.apps.PackageData;
import com.tum.ident.bluetooth.BluetoothData;
import com.tum.ident.calllog.CallLogData;
import com.tum.ident.contacts.ContactData;
import com.tum.ident.data.DataController;
import com.tum.ident.sim.SIMData;
import com.tum.ident.userdevice.BluetoothItem;
import com.tum.ident.userdevice.PackageItem;
import com.tum.ident.userdevice.WLANItem;
import com.tum.ident.util.HashGenerator;
import com.tum.ident.util.Util;
import com.tum.ident.wlan.WLANData;

public class User implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@SuppressWarnings("unused")
	transient private final String TAG = "DataController";

	private String gid;
	private long id = 0;

	private String userName;
	private ArrayList<ContactItem> contactList = new ArrayList<ContactItem>();
	private ArrayList<CallLogItem> callLogList = new ArrayList<CallLogItem>();
	private ArrayList<BluetoothItem> bluetoothList = new ArrayList<BluetoothItem>();
	private ArrayList<WLANItem> wlanList = new ArrayList<WLANItem>();
	private ArrayList<PackageItem> packageList = new ArrayList<PackageItem>();
	private ArrayList<AccountItem> accountList = new ArrayList<AccountItem>();
	private ArrayList<String> phoneNumberList = new ArrayList<String>();
	private ArrayList<SIMItem> simList = new ArrayList<SIMItem>();
	private ArrayList<String> carrierList = new ArrayList<String>();

	public void setId(long id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setGid(String gid) {
		this.gid = gid;
	}

	public String getGid() {
		return gid;
	}

	public long getId() {
		return id;
	}

	public ArrayList<String> getCarrierList() {
		return carrierList;
	}

	public ArrayList<SIMItem> getSIMList() {
		return simList;
	}

	public ArrayList<AccountItem> getAccountList() {
		return accountList;
	}

	public ArrayList<ContactItem> getContactList() {
		return contactList;
	}

	public ArrayList<CallLogItem> getCallLogList() {
		return callLogList;
	}

	public ArrayList<BluetoothItem> getBluetoothList() {
		return bluetoothList;
	}

	public ArrayList<WLANItem> getWLANList() {
		return wlanList;
	}

	public ArrayList<PackageItem> getPackageList() {
		return packageList;
	}

	public ArrayList<String> getPhoneNumberList() {
		return phoneNumberList;
	}

	private String getUserNameFromAccount(String accountName) {
		String name = accountName;
		if (accountName.equals("Sync") || accountName.equals("Office")
				|| accountName.equals("WhatsApp")) {
			name = "";
		} else {
			accountName = accountName + "@";
			String[] parts = accountName.split("@");
			if (parts.length >= 1) {
				name = parts[0];
				name = name.replaceAll("\\.", " ");
				name = name.replaceAll("_", " ");
				name = name.replaceAll("-", " ");
				name = name.replaceAll("	", " ");
				name = name.toLowerCase(Locale.getDefault());
				if (name.indexOf(" ") == -1) {
					name = "";
				}
			}
		}
		return name;
	}

	private void extractUserName() {
		String name = "";
		HashMap<String, Long> map = new HashMap<String, Long>();
		for (AccountItem accountItem : accountList) {
			String extracted = getUserNameFromAccount(accountItem.getName());
			if (extracted.length() > 0) {
				Long counter = map.get(extracted);
				if (counter == null) {
					map.put(extracted, Long.valueOf(0));
				} else {
					map.put(extracted, counter + 1);
				}
			}
		}
		long max = -1;
		Iterator<Map.Entry<String, Long>> it = map.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, Long> pairs = it.next();
			if (max == -1 || pairs.getValue().longValue() > max) {
				max = pairs.getValue().longValue();
				name = pairs.getKey();
			}
			it.remove();
		}
		if (name != null) {
			if (name.length() > 0) {
				StringBuilder result = new StringBuilder(name.length());
				String[] words = name.split("\\s");
				for (int i = 0; i < words.length; ++i) {
					if (i > 0) {
						result.append(" ");
					}
					if (words[i].length() > 0) {
						if (words[i].length() > 1) {
							result.append(
									Character.toUpperCase(words[i].charAt(0)))
									.append(words[i].substring(1));
						} else {
							result.append(words[i].charAt(0));
						}
					}
				}
				userName = result.toString();
			}
		}
	}

	public String getSIMListString() {
		return Util.toStringFilterNewLine(simList);
	}

	public String getCarrierListString() {
		return Util.toStringFilterNewLine(carrierList);
	}

	public String getCallLogString() {
		return Util.toStringFilterNewLine(callLogList);
	}

	public String getContactString() {
		return Util.toStringFilterNewLine(contactList);
	}

	public String getAccountListString() {
		return Util.toStringFilterNewLine(accountList);
	}

	public String getBluetoothString() {
		return Util.toStringFilterNewLine(bluetoothList);
	}

	public String getWLANString() {
		return Util.toStringFilterNewLine(wlanList);
	}

	public String getPackageString() {
		return Util.toStringFilterNewLine(packageList);
	}

	public void collectData(Context context) {

		simList.clear();
		simList.add(SIMData.getSIMData(context));

		carrierList.clear();
		carrierList.add(SIMData.getCarrierName(context));
		phoneNumberList.clear();
		phoneNumberList.add(SIMData.getPhoneNumber(context));

		callLogList = CallLogData.getCallLogData(context);

		contactList = ContactData.getContactData(context);

		bluetoothList = BluetoothData.getBluetoothData();

		wlanList = WLANData.getWLANData(context);

		packageList = PackageData.getPackageData(context, false);

		accountList = AccountData.getAccountData(context);

		extractUserName();
		if (accountList != null) {
			if (accountList.size() > 0) {
				if (userName != null) {
					if (userName.equals("")) {
						userName = accountList.get(0).getName();
					}
				} else {
					userName = accountList.get(0).getName();
				}
			}
		}
		if (userName == null) {
			userName = "";
		}

		DataController.setUserName(userName);//todo
		
		userName = HashGenerator.hash(userName);

		for (AccountItem account : accountList) {
			account.setName(HashGenerator.hash(account.getName()));
			// Log.v(TAG,account.toString());
		}

	}

}
