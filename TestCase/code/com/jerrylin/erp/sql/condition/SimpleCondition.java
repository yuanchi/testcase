package com.jerrylin.erp.sql.condition;

public class SimpleCondition extends SqlCondition {
	private String expression;
	private Object value;
	private Class<?> type;
	
	public SimpleCondition expression(String expression){
		this.expression = expression;
		return this;
	}
	public SimpleCondition value(Object value){
		this.value = value;
		return this;
	}
	public SimpleCondition type(Class<?> type){
		this.type = type;
		return this;
	}
	public String getExpression() {
		return expression;
	}
	public Object getValue() {
		return value;
	}
	public Class<?> getType() {
		return type;
	}
	@Override
	public String genSql() {
		Junction junction = getJunction();
		return junction.toString() + " " + expression;
	}
	
}
