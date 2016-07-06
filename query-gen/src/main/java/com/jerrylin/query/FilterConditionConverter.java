package com.jerrylin.query;

import java.io.Serializable;

public interface FilterConditionConverter extends Serializable{
	public static final String VALUE_FEATURE_STRING =				"string";
	public static final String VALUE_FEATURE_STRING_START_WITH =	"startWith";
	public static final String VALUE_FEATURE_STRING_CONTAIN =		"contain";
	public static final String VALUE_FEATURE_STRING_END_WITH =		"endWith";
	public static final String VALUE_FEATURE_STRING_EXACT =			"exact";
	public static final String VALUE_FEATURE_STRING_IGNORE_CASE =	"ignoreCase";
	public static final String VALUE_FEATURE_VALUE_EXPECTED =		"valueExpected";
	
	public FilterCondition convert(FilterCondition fc);
}
