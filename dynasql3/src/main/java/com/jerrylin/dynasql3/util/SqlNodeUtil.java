package com.jerrylin.dynasql3.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.Timestamp;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	/**
	 * this method only supports question mark parameters 
	 * @param sql
	 * @param params: it should be fixed order, suggesting using LinkedHashMap
	 * @return
	 */
	public static String compileParamValsToSql(String sql, LinkedHashMap<String, Object> params){
		for(Map.Entry<String, Object> p : params.entrySet()){
			Object v = p.getValue();
			String compiled = null;
			if(String.class.isInstance(v)
			|| Date.class.isInstance(v)
			|| java.util.Date.class.isInstance(v)
			|| Timestamp.class.isInstance(v)){
				compiled = "'"+ v +"'";
			}else if(Number.class.isInstance(v)){
				compiled = ""+ v;
			}else if(Boolean.class.isInstance(v)){
				compiled = "" + ((Boolean)v ? 1 : 0); 
			}
			sql = sql.replaceFirst("\\?", compiled);
		}
		return sql;
	}
}
