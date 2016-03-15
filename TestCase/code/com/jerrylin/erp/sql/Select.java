package com.jerrylin.erp.sql;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class Select extends SqlNode {
	public Select target(String target, String alias){
		SqlTarget t = SqlTarget.getInstance()
					.target(target)
					.alias(alias);
		addChild(t);
		return this;	
	}

	@Override
	public String genSql() {
		List<String> items = 
			getChildren().stream()
			.map(ISqlNode::genSql)
			.collect(Collectors.toList());
		return "SELECT " + StringUtils.join(items, ", ");
	}
}
