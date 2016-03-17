package com.jerrylin.erp.sql;

public class Desc extends SqlNode {
	private static final long serialVersionUID = -3118776156512470967L;
	private String target;
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	@Override
	public String genSql() {
		String result = target + " DESC"; 
		return result;
	}
	@Override
	public ISqlNode singleCopy() {
		Desc desc = new Desc();
		desc.id(getId());
		desc.setTarget(target);
		return desc;
	}

}
