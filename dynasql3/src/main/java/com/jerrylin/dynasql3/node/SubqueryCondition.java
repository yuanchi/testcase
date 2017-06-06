package com.jerrylin.dynasql3.node;

import java.util.stream.Collectors;

import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.Junctible;

public class SubqueryCondition extends SqlNode<SubqueryCondition> implements
		ChildSubquerible<SubqueryCondition>, Junctible{
	private static final long serialVersionUID = 2616409484368209956L;
	
	private String prefix;
	private String junction;
	public String getPrefix() {
		return prefix;
	}
	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}
	@Override
	public String getJunction() {
		return junction;
	}
	@Override
	public void setJunction(String junction) {
		this.junction = junction;
	}
	
	@Override
	public SubqueryCondition copy(){
		SubqueryCondition copy = super.copy();
		copy.setPrefix(prefix);
		copy.setJunction(junction);
		return copy;
	}
	@Override
	public String toSql(){
		// there should be only one child here, whose type is SelectExpression
		String result = getChildren().stream().map(c->c.toSql()).collect(Collectors.joining());
		result = prefix + " " + result;
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String newIndent = " " + indent;
		// there should be only one child here, whose type is SelectExpression
		String result = getChildren().stream().map(c->c.toSqlWith(newIndent)).collect(Collectors.joining());
		result = indent + prefix + " " + result;
		return result;
	}
}
