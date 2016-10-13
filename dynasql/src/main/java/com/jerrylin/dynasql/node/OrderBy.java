package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;

public class OrderBy extends TargetExpressible<OrderBy> {
	private static final long serialVersionUID = -454359214696359090L;
	public OrderBy asc(String expression){
		return t(expression + " ASC");
	}
	public OrderBy desc(String expression){
		return t(expression + " DESC");
	}
	@Override
	public String genSql(){
		String result = "";
		LinkedList<SqlNode<?>> children = getChildren();
		if(children.size()>0){
			result = "ORDER BY " + children.stream().map(n->{
				if(!SelectExpression.class.isInstance(n)){
					return n.genSql();
				}
				return genSubquerySql(n);
			}).collect(Collectors.joining(", "));
		}
		return result;
	}
	@Override
	public OrderBy copy(SqlNode<?>parent){
		OrderBy orderBy = new OrderBy();
		if(parent!=null){
			parent.addCopy(orderBy);
		} 
		copyTo(orderBy);
		return orderBy; 
	}
}
