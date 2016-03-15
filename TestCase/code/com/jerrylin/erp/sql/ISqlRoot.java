package com.jerrylin.erp.sql;

public interface ISqlRoot extends ISqlNode {
	public Select select();
	public From from();
	public Where where();
	public ISqlRoot joinAlias(String expression, String alias);
	public ISqlRoot joinOn(String expression, String on);
	public OrderBy orderBy();
}
