package com.jerrylin.dynasql3.node;

import java.util.Arrays;

import org.junit.Test;

import static org.junit.Assert.*;

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
}
