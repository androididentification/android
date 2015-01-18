package com.tum.ident.music;

import java.io.Serializable;
import java.util.ArrayList;



public class MusicItemList implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<MusicItem>   list     = new ArrayList<MusicItem>();
	
	public void add(MusicItem item){
		list.add(item);
	}

	public ArrayList<MusicItem> getList() {
		return list;
	}

	public void setList(ArrayList<MusicItem> list) {
		this.list = list;
	}
	public int size(){
		return list.size();
	}
}
