package com.jerrylin.dynasql3;

import com.jerrylin.dynasql3.node.SqlNode;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public interface ExpressionAliasible<Me extends SqlNode<?>> extends Aliasible<Me>, Expressible {
	default String toSql(){
		String alias = getAlias();
		String expression = getExpression();
		if(SqlNodeUtil.isBlank(alias)){
			return expression;
		}
		if(expression.contains(" ") || expression.contains("\n")){
			expression = "("+expression+")";
		}
		return expression + " AS " + alias;
	}
}
