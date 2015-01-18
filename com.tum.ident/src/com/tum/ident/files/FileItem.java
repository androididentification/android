package com.tum.ident.files;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class FileItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String hash;
	private String folderType;

	public FileItem(String hash, String folderType) {
		this.hash = hash;
		this.folderType = folderType;
	}

	public String getFileHash() {
		return hash;
	}

	public void setFileHash(String hash) {
		this.hash = hash;
	}

	public String getFolderType() {
		return folderType;
	}

	public void setFolderType(String folderType) {
		this.folderType = folderType;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
