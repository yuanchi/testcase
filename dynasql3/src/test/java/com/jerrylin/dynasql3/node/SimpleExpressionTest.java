package com.jerrylin.dynasql3.node;

import static com.jerrylin.dynasql3.util.SearchCondition.expr;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;

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
	@Test
	public void getTableReferences(){
		RootNode root = RootNode.create()
			.select("i.*")
			.fromAs("INFO", "i");
		SimpleExpression se1 = root.findWith(expr("i.*"));
		List<String> refs = se1.getTableReferences();
		assertEquals("i", refs.get(0));
	}
}
