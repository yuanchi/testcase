package com.jerrylin.dynasql.node;

import com.jerrylin.dynasql.Dependable;
import com.jerrylin.dynasql.Expressible;

public class SimpleCondition extends SqlNode<SimpleCondition> implements FilterCondition, Expressible, Dependable {
	private static final long serialVersionUID = 4076124384067943527L;
	
	private String expression;
	private String dependencyMark;
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	@Override
	public String genSql(){
		return expression;
	}
	@Override
	public SimpleCondition copy(SqlNode<?>parent){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression(expression);
		if(parent!=null){
			parent.addCopy(sc);
		} 
		copyTo(sc);
		return sc;
	}
	@Override
	public void setDependencyMark(String dm) {
		this.dependencyMark = dm;
	}
	@Override
	public String getDependencyMark() {
		return dependencyMark;
	}
}
