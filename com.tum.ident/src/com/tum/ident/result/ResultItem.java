package com.tum.ident.result;

import java.io.Serializable;

public class ResultItem implements Serializable {
	private static final long serialVersionUID = 1L;

	private  Type type;
	private  Object result;

	public enum Type {
		ID, Match, Error, Value
	}

	public ResultItem() {
		this.type = Type.Error;
		this.result = null;
	}

	public ResultItem(Type type, Object result) {
		this.type = type;
		this.result = result;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}



}
