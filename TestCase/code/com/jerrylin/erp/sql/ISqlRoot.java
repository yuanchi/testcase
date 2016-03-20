package com.jerrylin.erp.sql;

import java.util.function.Predicate;

public interface ISqlRoot extends ISqlNode {
	public static final boolean EXCLUDED = Boolean.TRUE;
	public static final boolean INCLUDED = Boolean.FALSE;
	
	public Select select();
	public From from();
	public Where where();
	public ISqlRoot joinAlias(String expression, String alias);
	public ISqlRoot joinOn(String expression, String on);
	public OrderBy orderBy();
	public ISqlNode excludeCopy(Predicate<ISqlNode> exclude);
}
