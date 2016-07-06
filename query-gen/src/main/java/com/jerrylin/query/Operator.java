package com.jerrylin.query;

public class Operator extends SqlNode {
	public static final String SYMBOL = "symbol";
	public String getSymbol() {
		return attr(SYMBOL);
	}
	public void setSymbol(String symbol) {
		attr(SYMBOL, symbol);
	}
	@Override
	public String genSql(){
		return getSymbol();
	}
	@Override
	public Operator newInstance(){
		return new Operator();
	}
}
