package com.jerrylin.dynasql3.node;

import java.util.LinkedList;

import com.jerrylin.dynasql3.Expressible;
import com.jerrylin.dynasql3.ExpressionParameterizable;
import com.jerrylin.dynasql3.SqlParameter;

public class CustomExpression extends SqlNode<CustomExpression> implements
		Expressible, ExpressionParameterizable<CustomExpression> {
	private static final long serialVersionUID = 4694236728812613804L;
	
	private String expression;
	private LinkedList<SqlParameter> params;
	private SqlParameter current;
	
	@Override
	public String getExpression() {
		return expression;
	}
	@Override
	public void setExpression(String expression) {
		this.expression = expression;
	}
	@Override
	public CustomExpression copy(){
		CustomExpression copy = super.copy();
		copy.setExpression(expression);
		return copy;
	}
	@Override
	public String toSql(){
		return expression;
	}
	@Override
	public LinkedList<SqlParameter> getParams() {
		return params;
	}
	@Override
	public CustomExpression setParams(LinkedList<SqlParameter> params) {
		this.params = params;
		return thisType();
	}
	@Override
	public SqlParameter getCurrent() {
		return current;
	}
	@Override
	public CustomExpression setCurrent(SqlParameter current) {
		this.current = current;
		return null;
	}
}
