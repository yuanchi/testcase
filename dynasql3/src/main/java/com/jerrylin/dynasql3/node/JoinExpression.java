package com.jerrylin.dynasql3.node;

import java.util.List;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.ExpressionAliasible;
import com.jerrylin.dynasql3.Joinable;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class JoinExpression extends SqlNode<JoinExpression> implements ExpressionAliasible<JoinExpression>, Joinable<JoinExpression>{
	private static final long serialVersionUID = 6285947476788777271L;
	
	private String expression;
	private String alias;
	private String joinType;
	
	@Override
	public String getExpression() {
		return expression;
	}
	@Override
	public void setExpression(String expression) {
		this.expression = expression;
	}
	@Override
	public String getAlias() {
		return alias;
	}
	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public String getJoinType() {
		return joinType;
	}
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}

	@Override
	public JoinExpression copy(){
		JoinExpression copy = super.copy();
		copy.setAlias(alias);
		copy.setExpression(expression);
		copy.setJoinType(joinType);
		return copy;
	}
	@Override
	public String toSql(){
		String sql = ExpressionAliasible.super.toSql();
		String r = Joinable.super.toSql();
		if(SqlNodeUtil.isNotBlank(r)){
			sql = sql + " " + r;
		}
		return sql;
	}
	@Override
	public String toSqlWith(String indent){
		String sql = ExpressionAliasible.super.toSql();
		String onIndent = " " + indent;
		String r = Joinable.super.toSqlWith(onIndent);
		if(SqlNodeUtil.isNotBlank(r)){
			sql = sql + "\n" + r;
		}
		return sql;
	}
}
