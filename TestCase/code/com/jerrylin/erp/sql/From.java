package com.jerrylin.erp.sql;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;


public class From extends SqlNode {
	private static final long serialVersionUID = -8928595846523142228L;

	public From target(String target, String alias){
		SqlTarget t = SqlTarget.getInstance()
					.target(target)
					.alias(alias);
		addChild(t);
		return this;
	}

	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
						.map(n->n.genSql().replace("AS ", ""))
						.collect(Collectors.toList());
		return "FROM " + StringUtils.join(items, ",\n");
	}

	@Override
	public ISqlNode singleCopy() {
		From from = new From();
		from.id(getId());
		return from;
	}
}
