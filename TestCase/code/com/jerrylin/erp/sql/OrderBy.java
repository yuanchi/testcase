package com.jerrylin.erp.sql;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class OrderBy extends SqlNode {
	private static final long serialVersionUID = -6961582950154807375L;

	public OrderBy asc(String target){
		Asc asc = new Asc();
		asc.setTarget(target);
		getChildren().add(asc);
		return this;
	}
	public OrderBy desc(String target){
		Desc desc = new Desc();
		desc.setTarget(target);
		getChildren().add(desc);
		return this;
	}
	
	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
								.map(ISqlNode::genSql)
								.collect(Collectors.toList());
		String result = StringUtils.join(items, ", ");
		return "ORDER BY " + result;
	}
	@Override
	public ISqlNode singleCopy() {
		OrderBy orderBy = new OrderBy();
		orderBy.id(getId());
		return orderBy;
	}

}
