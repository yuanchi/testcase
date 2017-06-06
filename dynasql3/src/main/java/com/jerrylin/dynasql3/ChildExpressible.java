package com.jerrylin.dynasql3;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.node.SimpleExpression;
import com.jerrylin.dynasql3.node.SqlNode;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public interface ChildExpressible<Me extends SqlNode<?>> extends LastChildAliasible<Me>, ChildAddible<Me>{
	/**
	 * t is the abbreviation of target,<br> 
	 * also see the Class {@link #SimpleExpression}.
	 * @param expression
	 * @return
	 */
	default Me t(String expression){
		SimpleExpression child = createBy(SimpleExpression.class);
		child.setExpression(expression);
		return add(child);
	}
	default String toSql(){
		List<String> collect = new ArrayList<>();
		List<SqlNode<?>> children = getChildren();
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> c = children.get(i);
			String r = c.toSql();
			collect.add(r);
		}
		String result = collect.stream().collect(Collectors.joining(", "));
		return result;
	}
	default String toSqlWith(String indent){
		List<String> collect = new ArrayList<>();
		List<SqlNode<?>> children = getChildren();
		String newIndent = " " + indent;
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> c = children.get(i);
			String r = c.toSqlWith(newIndent);
			collect.add(r);
		}
		String result = collect.stream().collect(Collectors.joining(",\n"));
		result = SqlNodeUtil.trimLeading(result);
		return result;
	
	}
}
