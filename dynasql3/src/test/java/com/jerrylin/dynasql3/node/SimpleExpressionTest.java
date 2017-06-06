package com.jerrylin.dynasql3.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.jerrylin.dynasql3.SqlNodeFactory;

public class SimpleExpressionTest {
	@Test
	public void copy(){
		SimpleExpression se = SqlNodeFactory.getSingleton().create(SimpleExpression.class);
		se.setAlias("emp");
		se.setExpression("EMPLOYEE");
		
		SimpleExpression copy = se.copy();
		assertFalse(se == copy);
		assertEquals(se.getAlias(), copy.getAlias());
		assertEquals(se.getExpression(), copy.getExpression());
	}
	@Test
	public void toSqlWithoutAlias(){
		SimpleExpression se = SqlNodeFactory.getSingleton().create(SimpleExpression.class);
		se.setExpression("EMPLOYEE");
		String sql = se.toSql();
		assertEquals("EMPLOYEE", sql);
	}
	@Test
	public void toSqlWithAlias(){
		SimpleExpression se = SqlNodeFactory.getSingleton().create(SimpleExpression.class);
		se.setAlias("emp");
		se.setExpression("EMPLOYEE");
		String sql = se.toSql();
		assertEquals("EMPLOYEE AS emp", sql);
	}
}
