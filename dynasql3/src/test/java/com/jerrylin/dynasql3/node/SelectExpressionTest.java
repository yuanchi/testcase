package com.jerrylin.dynasql3.node;

import static com.jerrylin.dynasql3.util.SearchCondition.*;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.jerrylin.dynasql3.SqlNodeFactory;

public class SelectExpressionTest {
	/**
	 * the purpose of this test case is to observe its returning type
	 */
	@Test
	public void config(){
		SelectExpression<?> se = new SelectExpression<>();
		se.config(me->{
			// do nothing
		});
	}
	@Test
	public void select(){
		SelectExpression<?> se = new SelectExpression<>();
		Select select1 = se.select();
		Select select2 = se.select();
		assertTrue(select1 == select2);
	}
	@Test
	public void selectWithExpressions(){
		SelectExpression<?> se = new SelectExpression<>()
			.select("p.id", "p.name", "emp.salary", "emp.address");
		
		SimpleExpression se1 = se.findWith(expr("p.id")); // expr() comes from com.jerrylin.dynasql3.util.SearchCondition
		SimpleExpression se2 = se.findWith(expr("p.name"));
		SimpleExpression se3 = se.findWith(expr("emp.salary"));
		SimpleExpression se4 = se.findWith(expr("emp.address"));
		
		List<SqlNode<?>> selectChildren = se.select().getChildren();
		
		assertTrue(selectChildren.get(0) == se1);
		assertTrue(selectChildren.get(1) == se2);
		assertTrue(selectChildren.get(2) == se3);
		assertTrue(selectChildren.get(3) == se4);
		
		String sql = se.toSql();
		assertEquals("SELECT p.id, p.name, emp.salary, emp.address", sql);
		String sqlf = se.toSqlf();
		String expected = 
			"SELECT p.id,\n"
		  + " p.name,\n"
		  + " emp.salary,\n"
		  + " emp.address";
		assertEquals(expected, sqlf);
	}
	@Test
	public void selectWithConfig(){
		SelectExpression<?> se = new SelectExpression<>()
			.select(s->
				s.t("p.id").as("id")
				.t("p.name").as("name")
				.t("emp.salary").as("sal")
				.t("emp.address").as("addr"));
		
		SimpleExpression se1 = se.findWith(alias("id")); // alias() comes from com.jerrylin.dynasql3.util.SearchCondition
		SimpleExpression se2 = se.findWith(alias("name"));
		SimpleExpression se3 = se.findWith(alias("sal"));
		SimpleExpression se4 = se.findWith(alias("addr"));
		
		List<SqlNode<?>> selectChildren = se.select().getChildren();
		
		assertTrue(selectChildren.get(0) == se1);
		assertTrue(selectChildren.get(1) == se2);
		assertTrue(selectChildren.get(2) == se3);
		assertTrue(selectChildren.get(3) == se4);
		
		String sql = se.toSql();
		assertEquals("SELECT p.id AS id, p.name AS name, emp.salary AS sal, emp.address AS addr", sql);
		String sqlf = se.toSqlf();
		String expected = 
			"SELECT p.id AS id,\n"
		  + " p.name AS name,\n"
		  + " emp.salary AS sal,\n"
		  + " emp.address AS addr";
		assertEquals(expected, sqlf);
	}
	@Test
	public void from(){
		SelectExpression<?> se = new SelectExpression<>();
		From from1 = se.from();
		From from2 = se.from();
		assertTrue(from1 == from2);
	}
	@Test
	public void fromWithExpressions(){
		SelectExpression<?> se = new SelectExpression<>()
			.from("EMPLOYEE", "ACCOUNT", "ADDRESS");
		
		SimpleExpression se1 = se.findWith(expr("EMPLOYEE"));
		SimpleExpression se2 = se.findWith(expr("ACCOUNT"));
		SimpleExpression se3 = se.findWith(expr("ADDRESS"));
		
		List<SqlNode<?>> fromChildren = se.from().getChildren();
		
		assertTrue(fromChildren.get(0) == se1);
		assertTrue(fromChildren.get(1) == se2);
		assertTrue(fromChildren.get(2) == se3);
		
		String sql = se.toSql();
		assertEquals("FROM EMPLOYEE, ACCOUNT, ADDRESS", sql);
	}
	@Test
	public void fromWithConfig(){
		SelectExpression<?> se = new SelectExpression<>()
			.from(f->
				f.t("EMPLOYEE").as("emp")
				.t("ACCOUNT").as("acc")
				.t("ADDRESS").as("add"));
		
		SimpleExpression se1 = se.findWith(alias("emp"));
		SimpleExpression se2 = se.findWith(alias("acc"));
		SimpleExpression se3 = se.findWith(alias("add"));
		
		List<SqlNode<?>> fromChildren = se.from().getChildren();
		
		assertTrue(fromChildren.get(0) == se1);
		assertTrue(fromChildren.get(1) == se2);
		assertTrue(fromChildren.get(2) == se3);
		
		String sql = se.toSql();
		assertEquals("FROM EMPLOYEE AS emp, ACCOUNT AS acc, ADDRESS AS add", sql);
		System.out.println(sql);
	}
	@Test
	public void fromAs(){
		SelectExpression se = new SelectExpression()
			.fromAs("EMPLOYEE", "emp");
		String sql = se.toSql();
		assertEquals("FROM EMPLOYEE AS emp", sql);
		
		se.fromAs("ACCOUNT", "acc");
		sql = se.toSql();
		assertEquals("FROM EMPLOYEE AS emp, ACCOUNT AS acc", sql);
	}
	@Test
	public void where(){
		SelectExpression<?> se = new SelectExpression();
		Where where1 = se.where();
		Where where2 = se.where();
		assertTrue(where1 == where2);
	}
	@Test
	public void whereWithConditions(){
		SelectExpression<?> se = new SelectExpression()
			.where("p.name LIKE '%yyy%'", "p.gender = 'F'", "p.age > 20");
		
		SimpleCondition sc1 = se.findWith(expr("p.name LIKE '%yyy%'"));
		SimpleCondition sc2 = se.findWith(expr("p.gender = 'F'"));
		SimpleCondition sc3 = se.findWith(expr("p.age > 20"));
		
		List<SqlNode<?>> whereChildren = se.where().getChildren();
		assertTrue(whereChildren.get(0) == sc1);
		assertTrue(whereChildren.get(1) == sc2);
		assertTrue(whereChildren.get(2) == sc3);
		
		String sql = se.toSql();
		String expected = "WHERE p.name LIKE '%yyy%' AND p.gender = 'F' AND p.age > 20";
		assertEquals(expected, sql);
	}
	@Test
	public void whereWithConfig(){
		SelectExpression<?> se = new SelectExpression<>()
			.where(w->
				w.and("p.name LIKE '%yyy%'")
					.or("p.gender = 'F'")
					.or("p.age > 20")
					.or(fc->
						fc.and("p.gender = 'M'")
							.and("p.salary < 1000")));
		
		SimpleCondition sc1 = se.findWith(expr("p.name LIKE '%yyy%'"));
		SimpleCondition sc2 = se.findWith(expr("p.gender = 'F'"));
		SimpleCondition sc3 = se.findWith(expr("p.age > 20"));
		SimpleCondition sc4 = se.findWith(expr("p.gender = 'M'"));
		SimpleCondition sc5 = se.findWith(expr("p.salary < 1000"));
		
		List<SqlNode<?>> whereChildren = se.where().getChildren();
		assertTrue(whereChildren.get(0) == sc1);
		assertTrue(whereChildren.get(1) == sc2);
		assertTrue(whereChildren.get(2) == sc3);
		
		FilterConditions<?> fc = se.findFirst(c->c.getClass() == FilterConditions.class);
		List<SqlNode<?>> fcChildren = fc.getChildren();
		assertTrue(fcChildren.get(0) == sc4);
		assertTrue(fcChildren.get(1) == sc5);
		
		String sql = se.toSql();
		String expected = "WHERE p.name LIKE '%yyy%' OR p.gender = 'F' OR p.age > 20 OR (p.gender = 'M' AND p.salary < 1000)";
		assertEquals(expected, sql);
	}
	@Test
	public void copy(){
		SelectExpression se = SqlNodeFactory.getSingleton().create(SelectExpression.class)
			.select("m.id", "m.name", "m.contribute")
			.fromAs("MEMBER", "m")
			.where("m.vip = 1")
			.orderBy("m.income DESC");
		SelectExpression copy = se.copy();
		
		assertFalse(se == copy);
		assertFalse(se.findWith(expr("m.id")) == copy.findWith(expr("m.id")));
		assertFalse(se.findWith(expr("m.name")) == copy.findWith(expr("m.name")));
		assertFalse(se.findWith(expr("m.contribute")) == copy.findWith(expr("m.contribute")));
		assertFalse(se.findWith(expr("MEMBER")) == copy.findWith(expr("MEMBER")));
		assertFalse(se.findWith(expr("m.vip = 1")) == copy.findWith(expr("m.vip = 1")));
		assertFalse(se.findWith(expr("m.income DESC")) == copy.findWith(expr("m.income DESC")));
		
		String expected = 
			"SELECT m.id, m.name, m.contribute\n"
			+ "FROM MEMBER AS m\n"
			+ "WHERE m.vip = 1\n"
			+ "ORDER BY m.income DESC";
		assertEquals(expected, se.toSql());
		assertEquals(expected, copy.toSql());
	}
	@Test
	public void sort(){
		SelectExpression se = new SelectExpression()
			.having("emp.level > 1")
			.where("emp.salary > 2000")
			.select("COUNT(*)", "emp.level")
			.orderBy("emp.level DESC")
			.groupBy("emp.level")
			.fromAs("EMPLOYEE", "emp");
//		se.sort(); // sort() has been invoked when invoking toSql() within SelectExpression instance
		String sql = se.toSql();
		String expected = "SELECT COUNT(*), emp.level\n"
				+ "FROM EMPLOYEE AS emp\n"
				+ "WHERE emp.salary > 2000\n"
				+ "ORDER BY emp.level DESC\n"
				+ "GROUP BY emp.level\n"
				+ "HAVING emp.level > 1";
		assertEquals(expected, sql);
		System.out.println(sql);
	}
}
