package com.jerrylin.dynasql3;


public interface Junctible {
	public static final String AND = "AND";
	public static final String OR = "OR";
	public String getJunction();
	public void setJunction(String junction);
}
