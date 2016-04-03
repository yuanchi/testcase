package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.ISqlNode;

public class SimpleCondition extends SqlCondition {
	private static final long serialVersionUID = 4978680262162278817L;
	private String propertyName;
	private String operator;
	private Object value;
	private Class<?> type;
	private String instruction;
	
	public SimpleCondition propertyName(String propertyName){
		this.propertyName = propertyName;
		return this;
	}
	public SimpleCondition operator(String operator){
		this.operator = operator;
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
	public SimpleCondition instruction(String instruction){
		this.instruction = instruction;
		return this;
	}
	public String getPropertyName() {
		return propertyName;
	}
	public String getOperator() {
		return operator;
	}
	public Object getValue() {
		return value;
	}
	public Class<?> getType() {
		return type;
	}
	public String getInstruction(){
		return instruction;
	}
	@Override
	public String genSql() {
		Junction junction = getJunction();
		return junction.toString() + " " + (propertyName + " " + operator + " :" + getId());
	}
	@Override
	public ISqlNode singleCopy() {
		SimpleCondition c = new SimpleCondition();
		c.id(getId());
		c.propertyName(propertyName)
		 .operator(operator)
		 .type(type)
		 .value(value)
		 .instruction(instruction)
		 .groupMark(getGroupMark())
		 .junction(getJunction())
		 ;
		return c;
	}
	
}
