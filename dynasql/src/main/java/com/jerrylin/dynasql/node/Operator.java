package com.jerrylin.dynasql.node;

import com.jerrylin.dynasql.Dependable;

public class Operator extends SqlNode<Operator> implements Dependable{
	private static final long serialVersionUID = -1650336875102204152L;
	private String symbol;
	private String dependencyMark;
	public String getSymbol(){
		return symbol;
	}
	public void setSymbol(String symbol){
		this.symbol = symbol;
	}
	@Override
	public String genSql(){
		return symbol;
	}
	@Override
	public Operator copy(SqlNode<?>parent){
		Operator operator = new Operator();
		operator.setSymbol(symbol);
		if(parent!=null){
			parent.addCopy(operator);
		} 
		copyTo(operator);
		return operator;
	}
	@Override
	public void setDependencyMark(String dm) {
		this.dependencyMark = dm;
	}
	@Override
	public String getDependencyMark() {
		return dependencyMark;
	}
}
