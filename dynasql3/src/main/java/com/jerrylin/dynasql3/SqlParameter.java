package com.jerrylin.dynasql3;

public class SqlParameter {
	private Object val;
	private String name;
	public Object getVal(){
		return val;
	}
	public SqlParameter setVal(Object val){
		this.val = val;
		return this;
	}
	public String getName() {
		return name;
	}
	public SqlParameter setName(String name) {
		this.name = name;
		return this;
	}
}
