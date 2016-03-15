package com.jerrylin.erp.sql;

public class Asc extends SqlNode {
	private String target;
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public String genSql() {
		String result = target + " ASC"; 
		return result;
	}

}
