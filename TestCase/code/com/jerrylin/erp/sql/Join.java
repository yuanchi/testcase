package com.jerrylin.erp.sql;

import org.apache.commons.lang3.StringUtils;

public class Join extends SqlNode {
	private String expression;
	private String alias;
	private String on;
	
	public Join expression(String expression){
		this.expression = expression;
		return this;
	}
	public Join alias(String alias){
		this.alias = alias;
		return this;
	}
	public Join on(String on){
		this.on = on;
		return this;
	}
	
	public String getExpression() {
		return expression;
	}
	public String getAlias() {
		return alias;
	}
	public String getOn() {
		return on;
	}
	@Override
	public String genSql() {
		String result = expression;
		if(StringUtils.isNotBlank(alias)){
			result += (" AS " + alias); 
		}
		if(StringUtils.isNotBlank(on)){
			result += (" ON " + on);
		}
		return result;
	}

}
