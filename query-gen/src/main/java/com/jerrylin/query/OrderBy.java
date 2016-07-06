package com.jerrylin.query;

import org.apache.commons.lang3.StringUtils;

public class OrderBy extends TargetSelectable {
	public OrderBy(){
		super(Expression.ORDERBY_TARGET_SELECTABLE);
	}
	@Override
	public OrderBy t(String expression, String alias){
		return (OrderBy)super.t(expression, alias);
	}
	@Override
	public OrderBy t(String expression){
		return (OrderBy)super.t(expression);
	}
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String orderBy = "ORDER BY " + StringUtils.join(map, ", ");
			return orderBy;
		}
		return "";
	}
	@Override
	public OrderBy newInstance(){
		return new OrderBy();
	}
}
