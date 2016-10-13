package com.jerrylin.dynasql;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class AliasTestCases {
	@Test
	public void findAliasByPattern(){
		findAliasByPattern(
			Pattern.compile("(\\w+)\\.\\w+"), 
			"p.name=?",
			"p.country.name=?",
			"DATE(p.birth)='2011-10-09'",
			"DATE(p.birth)=DATE(b.start_date)",
			"STR(p.country.name LIKE STR(b.country.name))",
			"addr.detail = 'JJJJ'");
	}
	private static void findAliasByPattern(Pattern p, String... inputs){
		for(String input : inputs){
			System.out.println("input:" + input);
			Matcher m = p.matcher(input);
			while(m.find()){
				int start = m.start();
				int end = m.end();
				String match = input.substring(start, end);
				String g1 = m.group(1);
				int groupCount = m.groupCount();
				System.out.println("start: " + start + "| end: " + end + "| match: " + match + "| g1: " + g1 + "| groupCount: " + groupCount);
			}
			System.out.println();
		}
	}
}
