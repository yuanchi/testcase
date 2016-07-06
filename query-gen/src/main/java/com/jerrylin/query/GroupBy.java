package com.jerrylin.query;

import org.apache.commons.lang3.StringUtils;

public class GroupBy extends SqlNode {
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String groupBy = "GROUP BY " + StringUtils.join(map, ", ");
			return groupBy;
		}
		return "";
	}
	@Override
	public GroupBy newInstance(){
		return new GroupBy();
	}
	public GroupBy t(String expression){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(expression);
		addChildren(se);
		return this;
	}
}
