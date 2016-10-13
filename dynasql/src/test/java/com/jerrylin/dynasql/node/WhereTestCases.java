package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;

import java.sql.Date;
import java.time.LocalDate;

import org.junit.Test;

public class WhereTestCases {
	@Test
	public void where(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.cond("cus.name = 'Bob'")
				.and("cus.birth > '1998-10-01'").getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"WHERE cus.name = 'Bob'"
			+ "\n  AND cus.birth > '1998-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereWithValue1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.cond("cus.name = ?")
				.and("cus.birth > ?").getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"WHERE cus.name = ?"
			+ "\n  AND cus.birth > ?";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereWithValue2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.cond("cus.name = :name")
				.and("cus.birth > :birth").getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"WHERE cus.name = :name"
			+ "\n  AND cus.birth > :birth";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereWithConditionId(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.cond("cus.name = :name").cId("cus_name")
				.and("cus.birth > :birth").cId("cus_birth").getRoot()
			;
		SimpleCondition sc1 = root.findById("cus_name");
		assertEquals("cus.name = :name", sc1.getExpression());
		
		SimpleCondition sc2 = root.findById("cus_birth");
		assertEquals("cus.birth > :birth", sc2.getExpression());
		
		String sql = root.genSql();
		String expectedSql = 
			"WHERE cus.name = :name"
			+ "\n  AND cus.birth > :birth";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereWithGroupConditions1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.cond("cus.name = :name")
				.andDelimiter()
					.cond("cus.age > :age")
					.or("cus.cost > :cost").closest(Where.class)
				.and("cus.birth > :birth").getRoot()
			;
		
		String sql = root.genSql();
		String expectedSql = 
			"WHERE cus.name = :name"
			+ "\n  AND (cus.age > :age OR cus.cost > :cost)"
			+ "\n  AND cus.birth > :birth"
			;
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereWithGroupConditions2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.delimiter()
					.cond("cus.age > :age")
					.and("cus.cost > :cost").closest(Where.class)
				.orDelimiter()
					.cond("cus.name LIKE :name")
					.and("cus.birth > :birth").getRoot()
			;
		
		String sql = root.genSql();
		String expectedSql = 
			"WHERE (cus.age > :age AND cus.cost > :cost)"
			+ "\n  OR (cus.name LIKE :name AND cus.birth > :birth)"
			;
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereSubquery1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.subqueryCond()
					.t("p.id").oper("IN")
					.subquery()
						.select()
							.t("cus.cus_id").getStart()
						.from()
							.t("customer").as("cus")
			.getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"WHERE p.id IN (SELECT cus.cus_id"
			+ "\n  FROM customer cus)";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void whereSubquery2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.where()
				.subqueryCond()
					.t("p.id IN")
					.subquery()
						.select()
							.t("cus.cus_id").getStart()
						.from()
							.t("customer").as("cus")
			.getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"WHERE p.id IN (SELECT cus.cus_id"
			+ "\n  FROM customer cus)";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}	
}
