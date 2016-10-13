package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SelectTestCases {
	@Test
	public void select(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("VERSON()")
				.t("USER()")
				.t("DATABASE()").getRoot()
				;
		String expectedSql = 
			"SELECT VERSON(),"
			+"\n  USER(),"
			+"\n  DATABASE()";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
}
