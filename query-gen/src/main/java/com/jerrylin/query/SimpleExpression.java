package com.jerrylin.query;

public class SimpleExpression extends Expression {
	public static final String EXPRESSION = "expression";
	public static final String ALIAS = "alias";
	
	public String getExpression() {
		return attr(EXPRESSION);
	}
	public void setExpression(String expression) {
		attr(EXPRESSION, expression);
	}
	public String getAlias() {
		return attr(ALIAS);
	}
	public void setAlias(String alias) {
		attr(ALIAS, alias);
	}
	@Override
	public String genSql(){
		String alias = getAlias();
		String expression = getExpression();
		if(alias != null && alias.trim() != ""){
			return expression + " " + alias;
		}
		return expression;
	}
	@Override
	public SimpleExpression newInstance(){
		return new SimpleExpression();
	}
}
