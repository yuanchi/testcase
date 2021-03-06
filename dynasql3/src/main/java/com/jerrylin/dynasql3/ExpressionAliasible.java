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
	default String getTargetSymbol(){
		String alias = getAlias();
		if(SqlNodeUtil.isNotBlank(alias)){
			return alias;
		}
		String expression = getExpression();
		if(SqlNodeUtil.isNotBlank(expression)){
			return expression;
		}
		throw new RuntimeException("target symbol: [alias and expression] NOT FOUND");
	}
	default Me prependTargetSymbol(String prefix){
		String alias = getAlias();
		if(SqlNodeUtil.isNotBlank(alias)){
			setAlias(prefix + alias);
			return (Me)this;
		}
		String expression = getExpression();
		if(SqlNodeUtil.isNotBlank(expression)){
			setExpression(prefix + expression);
			return (Me)this;
		}
		throw new RuntimeException("target symbol: [alias and expression] NOT FOUND");
	}
}
