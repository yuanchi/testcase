package com.jerrylin.erp.sql;

public class Asc extends SqlNode {
	private static final long serialVersionUID = 6523145621377887457L;
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
	@Override
	public ISqlNode singleCopy() {
		Asc asc = new Asc();
		asc.id(getId());
		asc.setTarget(target);
		return asc;
	}

}
