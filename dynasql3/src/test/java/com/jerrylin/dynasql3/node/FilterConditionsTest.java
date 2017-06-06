package com.jerrylin.dynasql3.node;

import org.junit.Test;
import static org.junit.Assert.*;

import com.jerrylin.dynasql3.SqlNodeFactory;

public class FilterConditionsTest {
	@Test
	public void andOneCondition(){
		FilterConditions<?> fc = SqlNodeFactory.getSingleton().create(FilterConditions.class);
		fc.and("p.id = 'xxx'");
		String sql = fc.toSql();
		assertEquals("p.id = 'xxx'", sql);
		String sqlf = fc.toSqlf();
		assertEquals("p.id = 'xxx'", sqlf);
	}
	@Test
	public void andTwoConditions(){
		FilterConditions<?> fc = SqlNodeFactory.getSingleton().create(FilterConditions.class);
		fc.and("p.id = 'xxx'");
		fc.and("p.name = 'sss'");
		String sql = fc.toSql();
		assertEquals("p.id = 'xxx' AND p.name = 'sss'", sql);
		String sqlf = fc.toSqlf();
		String expected = 
			"p.id = 'xxx'\n"
		  + " AND p.name = 'sss'";
		assertEquals(expected, sqlf);
	}
	@Test
	public void andAnotherFilterConditions1(){
		FilterConditions<?> fc = SqlNodeFactory.getSingleton().create(FilterConditions.class);
		fc.and("p.id = 'xxx'");
		fc.and(
			grp->grp.and("p.age > 20").or("p.gender = 0")
		);
		String sql = fc.toSql();
		assertEquals("p.id = 'xxx' AND (p.age > 20 OR p.gender = 0)", sql);
		String sqlf = fc.toSqlf();
		String expected = 
			"p.id = 'xxx'\n"
		  + " AND (p.age > 20 OR p.gender = 0)";
		assertEquals(expected, sqlf);
	}
	@Test
	public void andAnotherFilterConditions2(){
		FilterConditions<?> fc = SqlNodeFactory.getSingleton().create(FilterConditions.class);
		fc.and(
			grp->grp.and("p.age > 20").or("p.gender = 0")
		);
		fc.and("p.id = 'xxx'");
		String sql = fc.toSql();
		assertEquals("(p.age > 20 OR p.gender = 0) AND p.id = 'xxx'", sql);
		String sqlf = fc.toSqlf();
		String expected = 
			"(p.age > 20 OR p.gender = 0)\n"
		  + " AND p.id = 'xxx'";
		assertEquals(expected, sqlf);
	}
	@Test
	public void onlyOneFilterConditionsWithin(){
		FilterConditions<?> fc = SqlNodeFactory.getSingleton().create(FilterConditions.class);
		fc.and(
			grp->grp.and("p.age > 20").or("p.gender = 0")
		);
		String sql = fc.toSql();
		assertEquals("p.age > 20 OR p.gender = 0", sql);
		
		String sqlf = fc.toSqlf();
		System.out.println(sqlf);
	}
}
