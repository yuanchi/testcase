package com.jerrylin.dynasql3.node;

import org.junit.Test;
import static org.junit.Assert.*;

public class JoinSubqueryTest {
	@Test
	public void toSql(){
		JoinSubquery js = new JoinSubquery();
		js.subquery(se->
			se.select(s->
				s.t("p.code").as("code")
				.t("p.name").as("name")
				.t("p.age").as("age"))
			.fromAs("PEOPLE", "p")
			.where("p.age > :age")).as("peo")
			.on("peo.age > 20");
		
		String sql = js.toSql();
		String expected = 
			"(SELECT p.code AS code, p.name AS name, p.age AS age\n"
		  + "FROM PEOPLE AS p\n"
		  + "WHERE p.age > :age) AS peo ON peo.age > 20";
		assertEquals(expected, sql);
		
		String sqlf = js.toSqlf();
		expected = 
			"(SELECT p.code AS code,\n"
		  + "  p.name AS name,\n"
		  + "  p.age AS age\n"
		  + "FROM PEOPLE AS p\n"
		  + " WHERE p.age > :age) AS peo\n"
		  + " ON peo.age > 20";
		assertEquals(expected, sqlf);
	}
}
