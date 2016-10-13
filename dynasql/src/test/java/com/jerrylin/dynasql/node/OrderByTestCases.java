package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class OrderByTestCases {
	@Test
	public void orderBy1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.orderBy()
				.asc("p.name")
				.desc("p.id").getRoot()
			;
		String sql = root.genSql();
		String expectedSql = "ORDER BY p.name ASC, p.id DESC";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void orderBy2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.orderBy()
				.t("p.name ASC")
				.t("p.id DESC").getRoot()
			;
		String sql = root.genSql();
		String expectedSql = "ORDER BY p.name ASC, p.id DESC";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void orderByWithSubquery(){
		SelectExpression root = SelectExpression.initAsRoot()
			.orderBy()
				.subquery()
					.select()
						.t("boss.lname").getStart()
					.from()
						.t("employee").as("boss").getStart()
					.where()
						.cond("boss.emp_name LIKE '%uuu%'")
				.getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"ORDER BY (SELECT boss.lname"
			+ "\n  FROM employee boss"
			+ "\n  WHERE boss.emp_name LIKE '%uuu%')"; // not supporting postfix with keyword ASC or DESC in this case
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
}
