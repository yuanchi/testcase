package com.jerrylin.dynasql3.node;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CustomExpressionTest {
	@Test
	public void mysqlPaging(){
		RootNode root = RootNode.create()
			.select("a.id", "a.name")
			.fromAs("ACCOUNT", "a")
			.where(w->
				w.and("a.open > ?")
					.or("a.open < ?"))
			.orderByWith("a.name", "DESC");
		
		CustomExpression ce = root.getTopMostFactory().create(CustomExpression.class);
		ce.setExpression("LIMIT ? OFFSET ?");
		
		root.add(ce);
		
		String sql = root.toSql();
		String expected = 
			"SELECT a.id, a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > ? OR a.open < ?\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT ? OFFSET ?";
		assertEquals(expected, sql);
		
		String sqlf = root.toSqlf();
		expected = 
			"SELECT a.id,\n"
		  + " a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > ?\n"
		  + " OR a.open < ?\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT ? OFFSET ?";
		assertEquals(expected, sqlf);
	}
}
