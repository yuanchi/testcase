package com.jerrylin.dynasql3.util;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.LinkedHashMap;

import org.junit.Test;
import static org.junit.Assert.*;

public class SqlNodeUtilTest {
	@Test
	public void compileParamValsToSql(){
		String sql = "p.name LIKE ?\n"
				+ "AND p.birth > ?\n"
				+ "OR p.birth < ?\n"
				+ "AND p.salary > ?\n"
				+ "OR p.salary < ?\n"
				+ "AND p.male = ?\n"
				+ "OR p.important = ?\n"
				+ "AND p.bonus > ?\n";
		LinkedHashMap<String, Object> params = new LinkedHashMap<>();
		params.put("0", "Bob");
		params.put("1", new Date(System.currentTimeMillis()));
		params.put("2", new Timestamp(System.currentTimeMillis()));
		params.put("3", new Double(1000.234));
		params.put("4", 250.123d);
		params.put("5", true);
		params.put("6", new Boolean(false));
		params.put("7", new BigDecimal("100.23"));

		String compiled = SqlNodeUtil.compileParamValsToSql(sql, params);
		String expected = "p.name LIKE 'Bob'\n"
				+ "AND p.birth > '2017-06-14'\n"
				+ "OR p.birth < '2017-06-14 10:43:51.094'\n"
				+ "AND p.salary > 1000.234\n"
				+ "OR p.salary < 250.123\n"
				+ "AND p.male = 1\n"
				+ "OR p.important = 0\n"
				+ "AND p.bonus > 100.23\n";
		assertEquals(expected, compiled);
	}
}
