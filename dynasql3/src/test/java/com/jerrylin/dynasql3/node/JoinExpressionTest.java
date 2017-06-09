package com.jerrylin.dynasql3.node;

import static com.jerrylin.dynasql3.util.SearchCondition.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jerrylin.dynasql3.SqlNodeFactory;

public class JoinExpressionTest {
	@Test
	public void copy(){
		JoinExpression je = SqlNodeFactory.getSingleton().create(JoinExpression.class);
		je.setJoinType("JOIN");
		je.setExpression("EMPLOYEE");
		je.setAlias("emp");
		
		JoinExpression copy = je.copy();
		assertFalse(copy == je);
		assertEquals(je.getJoinType(), copy.getJoinType());
		assertEquals(je.getExpression(), copy.getExpression());
		assertEquals(je.getAlias(), copy.getAlias());
	}
	@Test
	public void toSqlWithoutAlias(){
		JoinExpression je = SqlNodeFactory.getSingleton().create(JoinExpression.class);
		je.setJoinType("JOIN");
		je.setExpression("EMPLOYEE");
		String sql = je.toSql();
		assertEquals("EMPLOYEE", sql);
	}
	@Test
	public void toSqlWithAlias(){
		JoinExpression je = SqlNodeFactory.getSingleton().create(JoinExpression.class);
		je.setJoinType("JOIN");
		je.setExpression("EMPLOYEE");
		je.setAlias("emp");
		String sql = je.toSql();
		assertEquals("EMPLOYEE AS emp", sql);
	}
	@Test
	public void toSqlWithOn1(){
		JoinExpression je = SqlNodeFactory.getSingleton().create(JoinExpression.class);
		je.setJoinType("JOIN");
		je.setExpression("EMPLOYEE");
		je.setAlias("emp");
		je.on("xxx.emp_id = emp.id");
		String sql = je.toSql();
		assertEquals("EMPLOYEE AS emp ON xxx.emp_id = emp.id", sql);
		String sqlf = je.toSqlf();
		assertEquals(sql, sqlf);
	}
	@Test
	public void replaceWith(){
		RootNode root = RootNode.create()
			.from(f->
				f.t("EMPLOYEE").as("e")
				.leftOuterJoin("SALARY").as("s")
				.on("e.id = s.emp_id"))
			.where("s.min > :salary_min");
		String sqlf = root.toSqlf();
		String expected = 
			"FROM EMPLOYEE AS e\n"
			+ " LEFT OUTER JOIN SALARY AS s\n"
			+ "  ON e.id = s.emp_id\n"
			+ "WHERE s.min > :salary_min";
		assertEquals(expected, sqlf);
		
		JoinExpression je = root.findWith(alias("s"));
		je.changedWith(s->
			s.select("*")
			.from(je.getExpression()));
		
		sqlf = root.toSqlf();
		expected = 
			"FROM EMPLOYEE AS e\n"
			+ " LEFT OUTER JOIN (SELECT *\n"
			+ "   FROM SALARY) AS s\n"
			+ "  ON e.id = s.emp_id\n"
			+ "WHERE s.min > :salary_min";
		assertEquals(expected, sqlf);
		
		SqlNode<?> found = root.findWith(alias("s"));
		assertFalse(found == je);
	}
}
