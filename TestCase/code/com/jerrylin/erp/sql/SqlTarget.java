package com.jerrylin.erp.sql;

import org.apache.commons.lang3.StringUtils;

public class SqlTarget extends SqlNode {
	private static final long serialVersionUID = -6053902581602785992L;
	private String target;
	private String alias;
	
	public SqlTarget target(String target){
		this.target = target;
		return this;
	}
	public SqlTarget alias(String alias){
		this.alias = alias;
		return this;
	}
	
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	
	public static SqlTarget getInstance(){
		return new SqlTarget();
	}
	@Override
	public String genSql() {
		String result = target;
		if(StringUtils.isNotBlank(alias)){
			result += " AS " + alias;
		}
		return result;
	}
	@Override
	public ISqlNode singleCopy() {
		SqlTarget t = new SqlTarget();
		t.id(getId());
		t.alias(alias)
		.target(target);
		return t;
	}
}
