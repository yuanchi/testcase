package com.jerrylin.dynasql3.node;

import org.junit.Test;
import static org.junit.Assert.*;

public class SubqueryConditionTest {
	@Test
	public void toSql(){
		SubqueryCondition sc = new SubqueryCondition();
		sc.setPrefix("p.id IN");
		sc.subquery(se->
			se.select("acc.id")
				.fromAs("ACCOUNT", "acc")
				.where("acc.id = :aId")
				.where(w->
					w.or("acc.id IN", sse->
						sse.select("d.id")
							.fromAs("DATA", "d"))
					.and("acc.age > :age")));
		String sql = sc.toSql();
		String expected = 
			"p.id IN (SELECT acc.id\n"
		  + "FROM ACCOUNT AS acc\n"
		  + "WHERE acc.id = :aId OR acc.id IN (SELECT d.id\n"
		  + "FROM DATA AS d) AND acc.age > :age)";
		assertEquals(expected, sql);
		
		String sqlf = sc.toSqlf();
		expected = 
			"p.id IN (SELECT acc.id\n"
		  + " FROM ACCOUNT AS acc\n"
		  + " WHERE acc.id = :aId\n"
		  + "  OR acc.id IN (SELECT d.id\n"
		  + "   FROM DATA AS d)\n"
		  + "  AND acc.age > :age)";
		assertEquals(expected, sqlf);
	}
}
