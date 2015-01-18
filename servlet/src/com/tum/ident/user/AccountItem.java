package com.tum.ident.user;

import java.io.Serializable;


public class AccountItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private String type;
	private String name;

	transient private long id;
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}
	public String getName() {
		return name;
	}

	public AccountItem(String type,String name) {
		this.type = type;
		this.name = name;
	}
	
	
}
