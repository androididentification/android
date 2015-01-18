package com.tum.ident.userdevice;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PackageItem implements Serializable {
	private static final long serialVersionUID = 1L;
	private long InstallTime;
	private long UpdateTime;
	private String appName;
	private String packageName;
	private String versionName;
	private long id;

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

	public PackageItem(String appName, String packageName, String versionName,
			long InstallTime, long UpdateTime) {
		this.InstallTime = InstallTime;
		this.UpdateTime = UpdateTime;
		this.appName = appName;
		this.packageName = packageName;
		this.versionName = versionName;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
