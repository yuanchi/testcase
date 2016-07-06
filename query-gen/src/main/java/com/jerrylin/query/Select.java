package com.jerrylin.query;

import org.apache.commons.lang3.StringUtils;

public class Select extends TargetSelectable {
	public Select(){
		super(Expression.SELECT_TARGET_SELECTABLE);
	}
	@Override
	public Select t(String expression, String alias){
		return (Select)super.t(expression, alias);
	}
	@Override
	public Select t(String expression){
		return (Select)super.t(expression);
	}
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String select = "SELECT " + StringUtils.join(map, ",\n" + toStart().getIndent());
			return select;
		}
		return "";
	}
	@Override
	public Select newInstance(){
		return new Select();
	}
}
