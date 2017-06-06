package com.jerrylin.dynasql3.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SqlNodeUtil {
	public static boolean isNotBlank(String s){
		return s != null && !s.trim().equals("");
	}
	public static boolean isBlank(String s){
		return !isNotBlank(s);
	}
	public static String trimLeading(String input){
		char[] chs = input.toCharArray();
		List<Character> list = new ArrayList<>();
		for(char c : chs){
			list.add(c);
		}
		Iterator<Character> itr = list.iterator();
		while(itr.hasNext() && itr.next() == ' '){
			itr.remove();
		}
		String result = list.stream().map(c->String.valueOf(c)).collect(Collectors.joining());
		return result;
	}
}
