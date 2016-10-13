package com.jerrylin.dynasql.node;

import com.jerrylin.dynasql.Aliasible;
import com.jerrylin.dynasql.Expressible;

public class SimpleExpression extends SqlNode<SimpleExpression> implements Aliasible<SimpleExpression>, Expressible{
	private static final long serialVersionUID = -7027561095101630249L;
	private String alias;
	private String expression;
	public String getExpression(){
		return expression;
	}
	public void setExpression(String expression){
		this.expression = expression;
	}
	public String getAlias(){
		return alias;
	}
	public void setAlias(String alias){
		this.alias = alias;
	}
	public SimpleExpression as(String alias){
		setAlias(alias);
		return this;
	}
	@Override
	public String genSql() {
		if(alias==null){
			return expression;
		}else{
			return expression + " " + alias; 
		}
	}
	@Override
	public SimpleExpression copy(SqlNode<?>parent){
		SimpleExpression se = new SimpleExpression();
		se.setAlias(alias);
		se.setExpression(expression);
		if(parent!=null){
			parent.addCopy(se);
		} 
		copyTo(se);
		return se;
	}
}
