package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class GroupBy extends SqlNode<GroupBy> {
	private static final long serialVersionUID = -296609469640283947L;
	
	public GroupBy t(String expression){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(expression);
		add(se);
		return this;
	} 
	@Override
	public String genSql() {
		String result = "";
		LinkedList<SqlNode<?>> children = getChildren();
		if(children.size()!=0){
			result = "GROUP BY " +  children.stream()
				.map(n->n.genSql())
				.collect(Collectors.joining(", "));
		}
		return result;
	}
	@Override
	public GroupBy copy(SqlNode<?>parent){
		GroupBy groupBy = new GroupBy();
		if(parent!=null){
			parent.addCopy(groupBy);
		}
		copyTo(groupBy);
		return groupBy;
	}
}
