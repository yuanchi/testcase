package com.jerrylin.erp.sql;

import java.util.Map;
import java.util.function.Consumer;
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
	public ISqlRoot excludeCopy();
	public ISqlRoot excludeCopy(Predicate<ISqlNode> exclude);
	public ISqlRoot transformCopy();
	public ISqlRoot transformCopy(Consumer<ISqlNode> transform);
	public Map<String, Object> getCondIdValuePairs();
}
