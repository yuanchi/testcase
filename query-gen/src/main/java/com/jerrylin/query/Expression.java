package com.jerrylin.query;


public abstract class Expression extends SqlNode {
	public static final String NODE_ROLE = "nodeRole";
	public static final String ROOT = "root";
	
	public static final String TARGET_ROLE = "targetRole";
	public static final String SELECT_TARGET_SELECTABLE = "selectTargetSelectable";
	public static final String FROM_TARGET_SELECTABLE = "fromTargetSelectable";
	public static final String ORDERBY_TARGET_SELECTABLE = "orderByTargetSelectable";
	
	public static final String FILTER_CONDITION_PART = "filterCondPart";
	public static final String FILTER_CONDITION_START = "filterCondStart";
	public static final String FILTER_CONDITION_END = "filterCondEnd";
	public static final String FILTER_SELECT = "filterSelect";
}
