package com.jerrylin.dynasql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Expressible {
	static final Pattern FIND_ALIAS = Pattern.compile("(\\w+)\\.\\w+");
	static List<String> findRelatedAliases(String input){
		List<String> aliases = new ArrayList<>();
		Matcher m = FIND_ALIAS.matcher(input);
		while(m.find()){
			String g1 = m.group(1);
			if(!aliases.contains(g1)){// avoiding duplicate
				aliases.add(g1);
			}
		}
		return aliases;
	}
	/**
	 * alias here means first name
	 * @return
	 */
	default List<String> findRelatedAliases(){
		String expression = getExpression();
		if(expression==null){
			return Collections.emptyList();
		}
		return findRelatedAliases(expression);
	}
	public String getExpression();
	public void setExpression(String expression);
}
