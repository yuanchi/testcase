package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.ChildExpressible;
import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class OrderBy extends SqlNode<OrderBy> implements
ChildExpressible<OrderBy>, ChildSubquerible<OrderBy> {
	private static final long serialVersionUID = -6845934433064344189L;
	@Override
	public String toSql(){
		String result = ChildExpressible.super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "ORDER BY " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String result = ChildExpressible.super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			String preIndent = getParent() != null ? indent : "";
			result = preIndent + "ORDER BY " + result;
		}
		return result;
	}
	public OrderBy direct(String direction){
		SqlNode<?> c = getChildren().getLast();
		if(SimpleExpression.class.isInstance(c)){
			SimpleExpression.class.cast(c).setDirection(direction);
		}
		return thisType();
	}
	public OrderBy asc(){
		return direct("ASC");
	}
	public OrderBy desc(){
		return direct("DESC");
	}
	public OrderBy clearAllDirections(){
		for(SqlNode<?> c : getChildren()){
			if(SimpleExpression.class.isInstance(c)){
				SimpleExpression.class.cast(c).setDirection(null);
			}
		}
		return thisType();
	}
}
