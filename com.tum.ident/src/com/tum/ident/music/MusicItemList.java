package com.tum.ident.music;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.tum.ident.util.Util;

public class MusicItemList implements Serializable {
	private static final long serialVersionUID = 1L;
	private ArrayList<MusicItem> list = new ArrayList<MusicItem>();

	public void add(MusicItem item) {
		list.add(item);
	}

	public ArrayList<MusicItem> getList() {
		return list;
	}

	public void setList(ArrayList<MusicItem> list) {
		this.list = list;
	}

	public int size() {
		return list.size();
	}

	public String getMusicString() {
		return Util.toStringFilterNewLine(list);
	}

	public void sort() {
		// Sorting
		Collections.sort(list, new Comparator<MusicItem>() {
			@Override
			public int compare(MusicItem item1, MusicItem item2) {
				int sgn = (int) Math.signum(item2.getCounter()
						- item1.getCounter());
				return sgn;
			}
		});
	}

}
