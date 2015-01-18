package com.tum.ident.userdevice;

import java.io.Serializable;



public class PackageItem implements Serializable {
	private static final long serialVersionUID = 1L;
	long InstallTime;
	long UpdateTime;
	String appName;
	String packageName;
	String versionName;
	public long id;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getInstallTime() {
		return InstallTime;
	}

	public long getUpdateTime() {
		return UpdateTime;
	}

	public String getAppName() {
		return appName;
	}

	public String getPackageName() {
		return packageName;
	}

	public String getVersionName() {
		return versionName;
	}
	public PackageItem(String appName,String packageName,String versionName,long InstallTime,long UpdateTime) {
		this.InstallTime = InstallTime;
		this.UpdateTime = UpdateTime;
		this.appName = appName;
		this.packageName = packageName;
		this.versionName = versionName;
	}

	

}
