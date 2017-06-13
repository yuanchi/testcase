package com.jerrylin.dynasql3;

import java.util.LinkedList;

import com.jerrylin.dynasql3.node.SelectExpression;
import com.jerrylin.dynasql3.node.SqlNode;

public interface SingleChildSubquerible {
	public LinkedList<SqlNode<?>> getChildren();
	default String getSubqueryAlias(){
		SelectExpression<?> subquery = getSubquery();
		if(subquery != null){
			subquery.getAlias();
		}
		return null;
	}
	default SelectExpression<?> getSubquery(){
		for(SqlNode<?> c : getChildren()){
			if(SelectExpression.class.isInstance(c)){
				return SelectExpression.class.cast(c);
			}
		}
		return null;
	}
}
