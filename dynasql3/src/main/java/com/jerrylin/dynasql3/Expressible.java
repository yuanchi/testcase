package com.jerrylin.dynasql3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Expressible {
	static final String TABLE_REFERENCE = "(\\w+)\\.[\\w\\.\\*]+";
	static final Pattern FIND_TABLE_REFERENCE = Pattern.compile(TABLE_REFERENCE);
	public String getExpression();
	public void setExpression(String expression);
	static List<String> findTableReferences(String expression){
		Matcher m = FIND_TABLE_REFERENCE.matcher(expression);
		List<String> refs = new ArrayList<>();
		while(m.find()){
			String g1 = m.group(1);
			refs.add(g1);
		}
		return refs;
	
	}
	default List<String> getTableReferences(){
		List<String> refs = findTableReferences(getExpression());
		return refs;
	}
	default boolean withSameTableRef(){
		List<String> refs = getTableReferences();
		if(refs.isEmpty()){
			return false;
		}
		String compared = refs.get(0);
		return refs.size() == Collections.frequency(refs, compared);
	}
	public static String getNewExprFrom(String expr, String tableReference){
		Matcher m = FIND_TABLE_REFERENCE.matcher(expr);
		// ref. https://stackoverflow.com/questions/38296673/replace-group-1-of-java-regex-with-out-replacing-the-entire-regex
		StringBuffer newExpr = new StringBuffer();
		while(m.find()){
			String g1 = m.group(1);
			m.appendReplacement(newExpr, m.group(0).replaceFirst(Pattern.quote(g1), tableReference));
		}
		m.appendTail(newExpr);
		return newExpr.toString();
	}
	/**
	 * if all table references are the same, replace all them with new one and update the expression
	 * @param tableReference
	 */
	default boolean replaceExprWith(String tableReference){
		if(!withSameTableRef()){
			return false;
		}
		String newExpr = getNewExprFrom(getExpression(), tableReference);
		setExpression(newExpr);
		return true;
	}
	default void prependTableReferences(String prefix){
		String expr = getExpression();
		Matcher m = FIND_TABLE_REFERENCE.matcher(expr);
		StringBuffer newExpr = new StringBuffer();
		while(m.find()){
			String g1 = m.group(1);
			m.appendReplacement(newExpr, m.group(0).replaceFirst(Pattern.quote(g1), prefix + g1));
		}
		m.appendTail(newExpr);
		setExpression(newExpr.toString());
	}
}
