package com.tum.ident.music;

import java.io.Serializable;

public class MusicItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String artist;
	private long counter;
	
	public MusicItem( String artist) {
		this.artist = artist;
		this.setCounter(1);
	}
	public String getArtist() {
		return artist;
	}
	
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public long getCounter() {
		return counter;
	}
	public void setCounter(long counter) {
		this.counter = counter;
	}
	
	
	
}
