package com.jerrylin.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class WhereTestCase {
	@Test
	public void findConditionParts(){
		String target = "p.name = :pName";
		
		String pat = "(\\S+)\\s+(\\S+)\\s+(\\S+)";
		Pattern p = Pattern.compile(pat);
		Matcher m = p.matcher(target);
		while(m.find()){
			String start = m.group(1);
			String operator = m.group(2);
			String end = m.group(3);
			System.out.println("start: " + start + ", operator: " + operator + ", end: " + end);
		}
	}
}
