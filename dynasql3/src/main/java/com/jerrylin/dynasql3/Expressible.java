package com.jerrylin.dynasql3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Expressible {
	static final String REGEX = "(\\w+)\\.[\\w\\.]+";
	public String getExpression();
	public void setExpression(String expression);
	default List<String> getTableReferences(){
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(getExpression());
		List<String> refs = new ArrayList<>();
		while(m.find()){
			String g1 = m.group(1);
			refs.add(g1);
		}
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
	/**
	 * if all table references are the same, replace all them with new one
	 * @param tableReference
	 */
	default boolean replaceExprWith(String tableReference){
		if(!withSameTableRef()){
			return false;
		}
		Pattern p = Pattern.compile(REGEX);
		Matcher m = p.matcher(getExpression());
		// ref. https://stackoverflow.com/questions/38296673/replace-group-1-of-java-regex-with-out-replacing-the-entire-regex
		StringBuffer newExpr = new StringBuffer();
		while(m.find()){
			String g1 = m.group(1);
			m.appendReplacement(newExpr, m.group(0).replaceFirst(Pattern.quote(g1), tableReference));
		}
		m.appendTail(newExpr);
		setExpression(newExpr.toString());
		return true;
	}
}
