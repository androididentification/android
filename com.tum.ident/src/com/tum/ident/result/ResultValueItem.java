package com.tum.ident.result;

import java.io.Serializable;

public class ResultValueItem implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum Type {
		OK, Error
	}
	
	private Type result;

	public ResultValueItem(Type result) {
		this.result = result;
	}

	public ResultValueItem(boolean ok) {
		if (ok) {
			this.result = Type.OK;
		} else {
			this.result = Type.Error;
		}
	}

	public Type getResult() {
		return result;
	}

	public void setResult(Type result) {
		this.result = result;
	}

	

}
