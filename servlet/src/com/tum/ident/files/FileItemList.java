package com.tum.ident.files;

import java.io.Serializable;
import java.util.ArrayList;



public class FileItemList implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<FileItem>   list     = new ArrayList<FileItem>();
	
	public void add(FileItem item){
		list.add(item);
	}
	
	public ArrayList<FileItem> getList() {
		return list;
	}

	public void setList(ArrayList<FileItem> list) {
		this.list = list;
	}
	
	
}
