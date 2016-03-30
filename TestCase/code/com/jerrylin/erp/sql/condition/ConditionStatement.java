package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.ISqlNode;

/**
 * representing single condition statement without extra input value
 * ex: p.name IS NOT NULL
 * @author JerryLin
 *
 */
public class ConditionStatement extends SqlCondition {
	private static final long serialVersionUID = -6344870109542752135L;
	
	private String expression;
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}

	@Override
	public String genSql() {
		return getJunction().getSymbol() + " " + expression;
	}
	@Override
	public ISqlNode singleCopy() {
		ConditionStatement s = new ConditionStatement();
		s.id(getId());
		s.setExpression(expression);
		s.groupMark(getGroupMark());
		return s;
	}

}
