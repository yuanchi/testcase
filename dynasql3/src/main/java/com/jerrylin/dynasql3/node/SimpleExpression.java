package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.ExpressionAliasible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;
/**
 * it may represent a table name, a field name, or the calculation unit
 * @author JerryLin
 *
 */
public class SimpleExpression extends SqlNode<SimpleExpression> implements ExpressionAliasible<SimpleExpression>{
	private static final long serialVersionUID = 7014778567734552225L;
	private String expression;
	private String alias;
	private String direction;
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
	public String getDirection() {
		return direction;
	}
	/**
	 * convenience method for mark ASC or DESC<br>
	 * it's possible to add the direction on expression,<br>
	 * but it's not recommended
	 * @param direction
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}
	@Override
	public SimpleExpression copy(){
		SimpleExpression copy = super.copy();
		copy.setExpression(expression);
		copy.setAlias(alias);
		copy.setDirection(direction);
		return copy;
	}
	@Override
	public String toSql(){
		return ExpressionAliasible.super.toSql() + (SqlNodeUtil.isBlank(direction) ? "" : " " + direction);
	}
}
