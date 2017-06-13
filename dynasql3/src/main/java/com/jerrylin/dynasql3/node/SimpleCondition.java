package com.jerrylin.dynasql3.node;

import java.util.LinkedList;

import com.jerrylin.dynasql3.ExpressionParameterizable;
import com.jerrylin.dynasql3.Filterable;
import com.jerrylin.dynasql3.Junctible;
import com.jerrylin.dynasql3.SqlParameter;

public class SimpleCondition extends SqlNode<SimpleCondition>
	implements ExpressionParameterizable<SimpleCondition>, Junctible, Filterable{
	private static final long serialVersionUID = -2660719106155213049L;
	
	private String junction = Junctible.AND;
	private String expression;
	private LinkedList<SqlParameter> params;
	private SqlParameter current;
	
	@Override
	public String getJunction() {
		return junction;
	}
	@Override
	public void setJunction(String junction) {
		this.junction = junction;
	}
	@Override
	public String getExpression() {
		return expression;
	}
	@Override
	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public SimpleCondition copy(){
		SimpleCondition copy = super.copy();
		copy.setJunction(junction);
		copy.setExpression(expression);
		if(params != null){
			LinkedList<SqlParameter> dest = new LinkedList<>(params);
			copy.setParams(dest);
		}
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
	public SimpleCondition setParams(LinkedList<SqlParameter> params) {
		this.params = params;
		return thisType();
	}
	@Override
	public SqlParameter getCurrent() {
		return current;
	}
	@Override
	public SimpleCondition setCurrent(SqlParameter current) {
		this.current = current;
		return thisType();
	}
	@Override
	public String getExprPart() {
		return getExpression();
	}
	@Override
	public void setExprPart(String exprPart) {
		setExpression(exprPart);
	}
}
