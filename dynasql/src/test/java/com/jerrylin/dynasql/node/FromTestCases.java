package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.jerrylin.dynasql.Expressible;

public class FromTestCases {
	@Test
	public void general(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("customer")
				.t("account").getRoot()
				;
		String expectedSql = "FROM customer, account";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void subquery(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.subquery().as("e")
					.select()
						.t("emp_id")
						.t("fname")
						.t("lname")
						.t("start_date")
						.t("title").getStart()
					.from()
						.t("employee").getRoot()
		;
		String expectedSql = 
			"FROM (SELECT emp_id,"
			+ "\n    fname,"
			+ "\n    lname,"
			+ "\n    start_date,"
			+ "\n    title"
			+ "\n  FROM employee) e";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void join(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.join("cus.account").as("acc").getRoot()
				;		
		String expectedSql = "FROM Customer cus JOIN cus.account acc";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void leftOuterJoin(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Member").as("mem")
				.leftOuterJoin("mem.address").as("add").getRoot()
				;		
		String expectedSql = "FROM Member mem LEFT OUTER JOIN mem.address add";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void rightOuterJoin(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Member").as("mem")
				.rightOuterJoin("mem.address").as("add").getRoot()
				;		
		String expectedSql = "FROM Member mem RIGHT OUTER JOIN mem.address add";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void innerJoin(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Member").as("mem")
				.innerJoin("mem.address").as("add").getRoot()
				;		
		String expectedSql = "FROM Member mem INNER JOIN mem.address add";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void multiJoin(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.join("cus.account").as("acc")
				.join("acc.info").as("acc_info")
				.join("acc_info.tel").as("tel").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus JOIN cus.account acc"
			+"\n  JOIN acc.info acc_info"
			+"\n  JOIN acc_info.tel tel";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void multiLeftOuterJoin(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.leftOuterJoin("cus.account").as("acc")
				.leftOuterJoin("acc.info").as("acc_info")
				.leftOuterJoin("acc_info.tel").as("tel").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus LEFT OUTER JOIN cus.account acc"
			+"\n  LEFT OUTER JOIN acc.info acc_info"
			+"\n  LEFT OUTER JOIN acc_info.tel tel";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void joinOn(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.join("Account").as("acc")
				.on("cus.acc_id = acc.id").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus JOIN Account acc"
			+ "\n  ON cus.acc_id = acc.id";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void leftOuterJoinOn(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.leftOuterJoin("Account").as("acc")
				.on("cus.acc_id = acc.id").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus LEFT OUTER JOIN Account acc"
			+ "\n  ON cus.acc_id = acc.id";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void multiJoinOn(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.join("Account").as("acc")
				.on("cus.acc_id = acc.id")
				.join("Balance").as("bal")
				.on("acc.bal_id = bal.id").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus JOIN Account acc"
			+ "\n  ON cus.acc_id = acc.id"
			+ "\n  JOIN Balance bal"
			+ "\n  ON acc.bal_id = bal.id";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void multiLeftOuterJoinOn(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Customer").as("cus")
				.leftOuterJoin("Account").as("acc")
				.on("cus.acc_id = acc.id")
				.leftOuterJoin("Balance").as("bal")
				.on("acc.bal_id = bal.id").getRoot()
				;		
		String expectedSql = 
			"FROM Customer cus LEFT OUTER JOIN Account acc"
			+ "\n  ON cus.acc_id = acc.id"
			+ "\n  LEFT OUTER JOIN Balance bal"
			+ "\n  ON acc.bal_id = bal.id";
		String sql = root.genSql();
		assertEquals(expectedSql, sql);
		System.out.println(sql);		
	}
	@Test
	public void nonEqualJoin1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()// self inner join
				.t("employee").as("e1")
				.innerJoin("employee").as("e2")
				.on("e1.emp_id < e2.emp_id")
			.getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"FROM employee e1 INNER JOIN employee e2"
			+ "\n  ON e1.emp_id < e2.emp_id";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}	
	@Test
	public void nonEqualJoin2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("employee").as("e")
				.innerJoin("product").as("p")
				.on()
					.cond("e.start_date >= p.date_offered")
					.and("e.start_date <= p.date_retired")
			.getRoot()
			;
		String sql = root.genSql();
		String expectedSql = 
			"FROM employee e INNER JOIN product p"
			+ "\n  ON e.start_date >= p.date_offered"
			+ "\n  AND e.start_date <= p.date_retired";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void referencedOrExcluded(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Account").as("acc").referencedOrExcluded()
				.leftOuterJoin("Customer").as("cus")
				.on("acc.cus_id = cus.id").referencedOrExcluded()
				.leftOuterJoin("Address").as("addr")
				.on("cus.addr_id = addr.id")
			.getRoot()
			;
		List<String> sbr = root.findFirst(From.class).getShouldBeReferencedAliases();
		assertTrue(sbr.contains("acc"));
		assertTrue(sbr.contains("cus"));
		assertFalse(sbr.contains("addr"));
		String sql = root.genSql();
		String expectedSql = 
			"FROM Account acc LEFT OUTER JOIN Customer cus"
			+ "\n  ON acc.cus_id = cus.id"
			+ "\n  LEFT OUTER JOIN Address addr"
			+ "\n  ON cus.addr_id = addr.id";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExprRefAlias(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc.name")
				.t("acc.code")
				.t("addr.detail").getStart()
			.from()
				.t("Account").as("acc").referencedOrExcluded()
				.leftOuterJoin("Customer").as("cus").referencedOrExcluded() // alias cus should be but is not referenced 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr")
				.on("cus.addr_id = addr.id").getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
				.and("addr.record_time > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();
		
		Map<String, List<Expressible>> sr = root.findFirst(From.class).findExprRefAlias();
//		sr.forEach((alias, expressions)->{
//			System.out.println("alias: " + alias);
//			expressions.stream().forEach(e->{
//				System.out.println(e.getExpression());
//			});
//			System.out.println();
//		});
		// key is alias which should be and is referenced
		assertEquals("acc", sr.keySet().iterator().next());
		// values are expressions referencing the marked aliases
		assertEquals("acc.name", sr.get("acc").get(0).getExpression());
		assertEquals("acc.code", sr.get("acc").get(1).getExpression());
		assertEquals("acc.name LIKE '%df%'", sr.get("acc").get(2).getExpression());
		assertEquals("acc.birth > '1990-10-01'", sr.get("acc").get(3).getExpression());
		String expectedSql = 
			"SELECT acc.name,"
			+ "\n  acc.code,"
			+ "\n  addr.detail"
			+"\nFROM Account acc LEFT OUTER JOIN Customer cus"
			+ "\n  ON acc.cus_id = cus.id"
			+ "\n  LEFT OUTER JOIN Address addr"
			+ "\n  ON cus.addr_id = addr.id"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'"
			+ "\n  AND addr.record_time > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExprRefAliasWithinFrom1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc.name")
				.t("acc.code")
				.t("addr.detail").getStart()
			.from()
				.t("Account").as("acc").referencedOrExcluded()
				.leftOuterJoin("Customer").as("cus").referencedOrExcluded() // alias cus should be but is not referenced 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr").referencedOrExcluded()
				.on("cus.addr_id = addr.id").getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
				.and("addr.record_time > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();
		
		Map<String, List<Expressible>> sr = root.findFirst(From.class).findExprRefAliasWithinFrom();
		sr.forEach((alias, expressions)->{
			System.out.println("alias: " + alias);
			expressions.stream().forEach(e->{
				System.out.println(e.getExpression());
			});
			System.out.println();
		});
		// key is alias which should be and is referenced
		Iterator<String> aliases = sr.keySet().iterator();
		assertEquals("acc", aliases.next());
		assertEquals("cus", aliases.next());
		// values are expressions referencing the marked aliases
		assertEquals("acc.cus_id = cus.id", sr.get("acc").get(0).getExpression());
		assertEquals("acc.cus_id = cus.id", sr.get("cus").get(0).getExpression());
		assertEquals("cus.addr_id = addr.id", sr.get("cus").get(1).getExpression());
		String expectedSql = 
			"SELECT acc.name,"
			+ "\n  acc.code,"
			+ "\n  addr.detail"
			+"\nFROM Account acc LEFT OUTER JOIN Customer cus"
			+ "\n  ON acc.cus_id = cus.id"
			+ "\n  LEFT OUTER JOIN Address addr"
			+ "\n  ON cus.addr_id = addr.id"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'"
			+ "\n  AND addr.record_time > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExprRefAliasWithinFrom2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.innerJoin("cat.mate").as("mate")
				.leftOuterJoin("mate.kittens").as("kitten").getStart()
			.getRoot();
		Map<String, List<Expressible>> results = root.findFirst(From.class).findExprRefAliasWithinFrom();
		results.forEach((a, es)->{
			System.out.println("alias: " + a);
			es.forEach(e->{
				System.out.println("expression: " + e.getExpression());
			});
		});
		String sql = root.genSql();
		System.out.println(sql);
	}
	@Test
	public void findExcludedAlias(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc.name")
				.t("acc.code")
				.t("addr.detail").getStart()
			.from()
				.t("Account").as("acc")
				.leftOuterJoin("Customer").as("cus") // alias cus is not referenced 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr")
				.on("acc.addr_id = addr.id")
				.innerJoin("Branch").as("bra") // alias bra is not referenced
				.on("acc.branch_id = bra.id").getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
				.and("addr.record_time > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();		
		From from = root.findFirst(From.class);
		List<String> excludedAliases = from.findExcludedAlias();
		List<String> expectedAliases = Arrays.asList("cus", "bra");
		assertEquals(expectedAliases, excludedAliases);

		String expectedSql = 
			"SELECT acc.name,"
			+ "\n  acc.code,"
			+ "\n  addr.detail"
			+ "\nFROM Account acc LEFT OUTER JOIN Customer cus"
			+ "\n  ON acc.cus_id = cus.id"
			+ "\n  LEFT OUTER JOIN Address addr"
			+ "\n  ON acc.addr_id = addr.id"
			+ "\n  INNER JOIN Branch bra"
			+ "\n  ON acc.branch_id = bra.id"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'"
			+ "\n  AND addr.record_time > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExcludedAliasGenSql1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc.name")
				.t("acc.code")
				.t("addr.detail").getStart()
			.from()
				.t("Account").as("acc")
				.leftOuterJoin("Customer").as("cus").referencedOrExcluded() // alias cus is not referenced and should be removed 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr")
				.on("acc.addr_id = addr.id")
				.innerJoin("Branch").as("bra").referencedOrExcluded() // alias bra is not referenced and should be removed
				.on("acc.branch_id = bra.id").getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
				.and("addr.record_time > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();		
		From from = root.findFirst(From.class);
		List<String> excludedAliases = from.findExcludedAlias();
		List<String> expectedAliases = Arrays.asList("cus", "bra");
		assertEquals(expectedAliases, excludedAliases);

		String expectedSql = 
			"SELECT acc.name,"
			+ "\n  acc.code,"
			+ "\n  addr.detail"
			+ "\nFROM Account acc LEFT OUTER JOIN Address addr"
			+ "\n  ON acc.addr_id = addr.id"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'"
			+ "\n  AND addr.record_time > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExcludedAliasGenSql2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc.name")
				.t("acc.code")
				.getStart()
			.from()
				.t("Account").as("acc")
				.leftOuterJoin("Customer").as("cus").referencedOrExcluded() // not referenced and should be removed 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr").referencedOrExcluded() // not referenced and should be removed
				.on("acc.addr_id = addr.id")
				.innerJoin("Branch").as("bra").referencedOrExcluded() // not referenced and should be removed
				.on("addr.branch_id = bra.id")
				.getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();		
		From from = root.findFirst(From.class);
		List<String> excludedAliases = from.findExcludedAlias();
		List<String> expectedAliases = Arrays.asList("cus", "bra", "addr");
		assertEquals(expectedAliases, excludedAliases);

		String expectedSql = 
			"SELECT acc.name,"
			+ "\n  acc.code"
			+ "\nFROM Account acc"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void findExcludedAliasGenSql3(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("*")
				.getStart()
			.from()
				.t("Account").as("acc")
				.leftOuterJoin("Customer").as("cus").referencedOrExcluded() // not referenced and should be removed 
				.on("acc.cus_id = cus.id")
				.leftOuterJoin("Address").as("addr").referencedOrExcluded() // not referenced and should be removed
				.on("acc.addr_id = addr.id")
				.innerJoin("Branch").as("bra").referencedOrExcluded() // not referenced and should be removed
				.on("addr.branch_id = bra.id")
				.getStart()
			.where()
				.cond("acc.name LIKE '%df%'")
				.and("acc.birth > '1990-10-01'")
			.getRoot()
			;
		String sql = root.genSql();		
		From from = root.findFirst(From.class);
		List<String> excludedAliases = from.findExcludedAlias();
		List<String> expectedAliases = Collections.emptyList();
		assertEquals(expectedAliases, excludedAliases);

		String expectedSql = 
			"SELECT *"
			+ "\nFROM Account acc LEFT OUTER JOIN Customer cus"
			+ "\n  ON acc.cus_id = cus.id"
			+ "\n  LEFT OUTER JOIN Address addr"
			+ "\n  ON acc.addr_id = addr.id"
			+ "\n  INNER JOIN Branch bra"
			+ "\n  ON addr.branch_id = bra.id"
			+ "\nWHERE acc.name LIKE '%df%'"
			+ "\n  AND acc.birth > '1990-10-01'";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}	
	@Test
	public void hqlInnerJoinReferencedOrExcluded1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.innerJoin("cat.mate").as("mate").referencedOrExcluded()
			.getRoot();
		String sql = root.genSql();
		String expectedSql = "FROM Cat cat";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void hqlInnerJoinReferencedOrExcluded2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.innerJoin("cat.mate").as("mate").referencedOrExcluded()
				.innerJoin("mate.kittens").as("kitten").referencedOrExcluded()
			.getRoot();
		String sql = root.genSql();
		String expectedSql = "FROM Cat cat";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void hqlInnerJoinReferencedOrExcluded3(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.innerJoin("cat.mate").as("mate").referencedOrExcluded()
				.innerJoin("mate.kittens").as("kitten").referencedOrExcluded().getStart()
			.where()
				.cond("kitten.name LIKE ?")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"FROM Cat cat INNER JOIN cat.mate mate"
			+ "\n  INNER JOIN mate.kittens kitten"
			+ "\nWHERE kitten.name LIKE ?";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void hqlInnerJoinReferencedOrExcluded4(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.innerJoin("cat.mate").as("mate").referencedOrExcluded()
				.innerJoin("cat.kittens").as("kitten").referencedOrExcluded().getStart()
			.where()
				.cond("kitten.name LIKE ?")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"FROM Cat cat INNER JOIN cat.kittens kitten"
			+ "\nWHERE kitten.name LIKE ?";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}	
	@Test
	public void hqlInnerJoinReferencedOrExcluded5(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.leftOuterJoin("cat.mate.kittens").as("kitten").referencedOrExcluded()
				.getStart()
			.where()
				.cond("kitten.name LIKE ?")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"FROM Cat cat LEFT OUTER JOIN cat.mate.kittens kitten"
			+ "\nWHERE kitten.name LIKE ?";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
	@Test
	public void hqlJoinFetch(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("Cat").as("cat")
				.addJoin("INNER JOIN FETCH").t("cat.mate")
				.addJoin("LEFT JOIN FETCH").t("cat.kittens")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"FROM Cat cat INNER JOIN FETCH cat.mate"
			+ "\n  LEFT JOIN FETCH cat.kittens";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
}
