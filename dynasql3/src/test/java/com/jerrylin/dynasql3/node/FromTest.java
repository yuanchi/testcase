package com.jerrylin.dynasql3.node;

import org.junit.Test;
import static org.junit.Assert.*;

public class FromTest {
	@Test
	public void toSql(){
		From from = new From()
			.t("EMPLOYEE").as("emp")
			.leftOuterJoin("INFO").as("i")
			.on("emp.id = i.emp_id");
		
		String sql = from.toSql();
		String expected = 
			"FROM EMPLOYEE AS emp\n"
		  + "LEFT OUTER JOIN INFO AS i ON emp.id = i.emp_id";
		assertEquals(expected, sql);
		
		String sqlf = from.toSqlf();
		expected = 
			"FROM EMPLOYEE AS emp\n"
		  + " LEFT OUTER JOIN INFO AS i\n"
		  + "  ON emp.id = i.emp_id";
		assertEquals(expected, sqlf);

		from.leftOuterJoin("SALARY").as("sal")
			.on("emp.id = sal.emp_id");
		sql = from.toSql();
		expected = 
			"FROM EMPLOYEE AS emp\n"
		  + "LEFT OUTER JOIN INFO AS i ON emp.id = i.emp_id\n"
		  + "LEFT OUTER JOIN SALARY AS sal ON emp.id = sal.emp_id";
		assertEquals(expected, sql);
		
		sqlf = from.toSqlf();
		expected = 
			"FROM EMPLOYEE AS emp\n"
		  + " LEFT OUTER JOIN INFO AS i\n"
		  + "  ON emp.id = i.emp_id\n"
		  + " LEFT OUTER JOIN SALARY AS sal\n"
		  + "  ON emp.id = sal.emp_id";
		assertEquals(expected, sqlf);
	}
	@Test
	public void fromSubquery(){
		From from = new From()
			.subquery(se->
				se.select(s1->s1.t("e.id").as("eid")
					.t("e.code").as("ecode")
					.t("e.guid").as("eguid"))
					.fromAs("EMPLOYEE", "e")).as("emp")
			.leftOuterJoin(se->
				se.select(s2->s2.t("s.max").as("smax")
					.t("s.min").as("smin")
					.t("s.level").as("slevel")
					.t("s.emp_id").as("emp_id"))
					.fromAs("SALARY", "s")).as("sal")
			.on("emp.eid = sal.emp_id");
		
		String sql = from.toSql();
		String expected = 
			"FROM (SELECT e.id AS eid, e.code AS ecode, e.guid AS eguid\n"
		  + "FROM EMPLOYEE AS e) AS emp\n"
		  + "LEFT OUTER JOIN (SELECT s.max AS smax, s.min AS smin, s.level AS slevel, s.emp_id AS emp_id\n"
		  + "FROM SALARY AS s) ON emp.eid = sal.emp_id";
		assertEquals(expected, sql);
		
		String sqlf = from.toSqlf();
		expected = 
			"FROM (SELECT e.id AS eid,\n"
		  + "   e.code AS ecode,\n"
		  + "   e.guid AS eguid\n"
		  + "  FROM EMPLOYEE AS e) AS emp\n"
		  + " LEFT OUTER JOIN (SELECT s.max AS smax,\n"
		  + "    s.min AS smin,\n"
		  + "    s.level AS slevel,\n"
		  + "    s.emp_id AS emp_id\n"
		  + "   FROM SALARY AS s)\n"
		  + "  ON emp.eid = sal.emp_id";
		assertEquals(expected, sqlf);
	}
	@Test
	public void mixFromSubqueryFirst(){
		From from = new From()
			.subquery(se->
				se.select(s->s.t("e.id").as("eid")
					.t("e.code").as("ecode")
					.t("e.guid").as("eguid"))
				.fromAs("EMPLOYEE", "e")
				.where("e.age > :age", "e.gender = :gender")).as("emp")
			.leftOuterJoin("SALARY").as("sal")
			.on("emp.eid = sal.emp_id");
		
		String sql = from.toSql();
		String expected = 
			"FROM (SELECT e.id AS eid, e.code AS ecode, e.guid AS eguid\n"
		  + "FROM EMPLOYEE AS e\n"
		  + "WHERE e.age > :age AND e.gender = :gender) AS emp\n"
		  + "LEFT OUTER JOIN SALARY AS sal ON emp.eid = sal.emp_id";
		assertEquals(expected, sql);
		
		String sqlf = from.toSqlf();
		expected = 
			"FROM (SELECT e.id AS eid,\n"
		  + "   e.code AS ecode,\n"
		  + "   e.guid AS eguid\n"
		  + "  FROM EMPLOYEE AS e\n"
		  + "  WHERE e.age > :age\n"
		  + "   AND e.gender = :gender) AS emp\n"
		  + " LEFT OUTER JOIN SALARY AS sal\n"
		  + "  ON emp.eid = sal.emp_id";
		assertEquals(expected, sqlf);
	}
	@Test
	public void mixFromSubquerySecond(){
		From from = new From()
			.t("SALARY").as("sal")
			.leftOuterJoin(se->
				se.select(s->s.t("e.id").as("eid")
					.t("e.code").as("ecode")
					.t("e.guid").as("eguid"))
				.fromAs("EMPLOYEE", "e")
				.where("e.age > :age", "e.gender = :gender")).as("emp")
			.on("emp.eid = sal.emp_id");
		
		String sql = from.toSql();
		String expected = 
			"FROM SALARY AS sal\n"
		  + "LEFT OUTER JOIN (SELECT e.id AS eid, e.code AS ecode, e.guid AS eguid\n"
		  + "FROM EMPLOYEE AS e\n"
		  + "WHERE e.age > :age AND e.gender = :gender) ON emp.eid = sal.emp_id";
		assertEquals(expected, sql);
		
		String sqlf = from.toSqlf();
		expected = 
			"FROM SALARY AS sal\n"
		  + " LEFT OUTER JOIN (SELECT e.id AS eid,\n"
		  + "    e.code AS ecode,\n"
		  + "    e.guid AS eguid\n"
		  + "   FROM EMPLOYEE AS e\n"
		  + "   WHERE e.age > :age\n"
		  + "    AND e.gender = :gender)\n"
		  + "  ON emp.eid = sal.emp_id";
		assertEquals(expected, sqlf);
	}
}
