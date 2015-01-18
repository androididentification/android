package com.tum.ident.accounts;

import java.util.ArrayList;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;

import com.tum.ident.user.AccountItem;
import com.tum.ident.util.HashGenerator;

public class AccountData {

	public static ArrayList<AccountItem> getAccountData(Context context) {
		ArrayList<AccountItem> accountList = new ArrayList<AccountItem>();
		AccountManager manager = (AccountManager) context
				.getSystemService(android.content.Context.ACCOUNT_SERVICE);
		Account[] accounts = manager.getAccounts();
		if (accounts != null) {
			if (accounts.length > 0) {
				for (Account account : accounts) {
					accountList.add(new AccountItem(HashGenerator
							.hash(account.type), account.name));
				}
			}
		}
		return accountList;
	}

}
