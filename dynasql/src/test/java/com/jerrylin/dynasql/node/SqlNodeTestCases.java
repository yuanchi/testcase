package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jerrylin.dynasql.InParameter;

public class SqlNodeTestCases {
	@Test
	public void copy1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("account_id")
				.t("product_cd")
				.t("cust_id")
				.t("avail_balance").getStart()
			.from()
				.t("account").getStart()
			.where()
				.subqueryCond()
					.t("open_emp_id").oper("<>").subquery()
						.select()
							.t("e.emp_id").getStart()
						.from()
							.t("employee").as("e").innerJoin("branch").as("b")
							.on("e.assigned_branch_id = b.branch_id").getStart()
						.where()
							.cond("e.title = 'Head Teller'")
							.and("b.city = 'Woburn'")
			.getRoot()
		;
		String expectedSql = 
			"SELECT account_id,"
			+ "\n  product_cd,"
			+ "\n  cust_id,"
			+ "\n  avail_balance"
			+ "\nFROM account"
			+ "\nWHERE open_emp_id <> (SELECT e.emp_id"
			+ "\n  FROM employee e INNER JOIN branch b"
			+ "\n    ON e.assigned_branch_id = b.branch_id"
			+ "\n  WHERE e.title = 'Head Teller'"
			+ "\n    AND b.city = 'Woburn')";
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		String copySql = copyRoot.genSql();
		assertEquals(copySql, sql);
		assertTrue(root != copyRoot);
		System.out.println(copySql);
	}
	@Test
	public void copy2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("emp_id")
				.t("fname")
				.t("lname").getStart()
			.from()
				.t("employee").getStart()
			.where()
				.cond("lname REGEXP '^[FG]'")
			.getRoot();
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		assertFalse(root == copyRoot);
		assertEquals(sql, copyRoot.genSql());
		System.out.println(sql);
		;
	}
	@Test
	public void copy3(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("account_id")
				.t("product_cd")
				.t("cust_id").getStart()
			.from()
				.t("account").getStart()
			.where()
				.subqueryCond()
					.t("open_branch_id").oper("=").subquery()
						.select()
							.t("branch_id").getStart()
						.from()
							.t("branch").getStart()
						.where()
							.cond("name = 'Woburn Branch'").getStart().closest(Where.class)
				.andSubqueryCond()
					.t("open_emp_id").oper("IN").subquery()
						.select()
							.t("emp_id").getStart()
						.from()
							.t("employee").getStart()
						.where()
							.cond("title = 'Teller'")
							.or("title = 'Head Teller'")
			.getRoot();
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		String copySql = copyRoot.genSql();
		assertFalse(root == copyRoot);
		assertEquals(sql, copySql);
		System.out.println(sql);
	}
	@Test
	public void copy4(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("a.account_id")
				.t("a.product_cd")
				.t("a.cust_id")
				.t("a.avail_balance").getStart()
			.from()
				.t("account").as("a").getStart()
			.where()
				.subqueryCond()
					.oper("EXISTS").subquery()
						.select()
							.t("1").getStart()
						.from()
							.t("transaction").as("t").getStart()
						.where()
							.cond("t.account_id = a.account_id")
							.and("t.txn_date = '2008-09-22'")
			.getRoot();
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		String copySql = copyRoot.genSql();
		assertFalse(root == copyRoot);
		assertEquals(sql, copySql);
		System.out.println(sql);
	}
	@Test
	public void copy5(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("d.dept_id")
				.t("d.name")
				.t("e_cnt.how_many").as("num_employees").getStart()
			.from()
				.t("department").as("d").innerJoin()
				.subquery().as("e_cnt")
					.select()
						.t("dept_id")
						.t("COUNT(*)").as("how_many").getStart()
					.from()
						.t("employee").getStart()
					.groupBy()
						.t("dept_id").getStart().closest(From.class)
				.on("d.dept_id = e_cnt.dept_id")
			.getRoot();
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		String copySql = copyRoot.genSql();
		assertFalse(root == copyRoot);
		assertEquals(sql, copySql);
		System.out.println(sql);
	}
	@Test
	public void copy6(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("d.dept_id")
				.t("d.name")
				.t("e_cnt.how_many").as("num_employees").getStart()
			.from()
				.t("department").as("d").innerJoin()
				.subquery().as("e_cnt")
					.select()
						.t("dept_id")
						.t("COUNT(*)").as("how_many").getStart()
					.from()
						.t("employee").getStart()
					.groupBy()
						.t("dept_id").getStart().closest(From.class)
				.on("d.dept_id = e_cnt.dept_id")
			.getRoot();
		String sql = root.genSql();
		
		From from = root.findFirst(From.class);
		From copyFrom = from.copy(null);
		
		System.out.println(copyFrom.genSql());
	}
	@Test
	public void paramsConfig1(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("*").getStart()
			.from()
				.t("member").as("m").getStart()
			.where()
				.cond("m.name LIKE ?").cId("m_name")
				.and("m.address LIKE ?").cId("m_address")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"SELECT *"
			+ "\nFROM member m"
			+ "\nWHERE m.name LIKE ?"
			+ "\n  AND m.address LIKE ?";
		assertEquals(expectedSql, sql);
		
		SimpleCondition name1 = root.findById("m_name");
		SimpleCondition address1 = root.findById("m_address");
		
		name1.addParam(new InParameter().type(String.class).value("Bob"));
		address1.addParam(new InParameter().type(String.class).value("Taipei"));
		
		SimpleCondition name2 = root.findById("m_name");
		SimpleCondition address2 = root.findById("m_address");
		
		assertEquals(String.class, name2.getParam().getType());
		assertEquals("Bob", name2.getParam().getValue());
		assertEquals(String.class, address2.getParam().getType());
		assertEquals("Taipei", address2.getParam().getValue());
		
		System.out.println(sql);
	}
	@Test
	public void paramsConfig2(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("*").getStart()
			.from()
				.t("member").as("m").getStart()
			.where()
				.cond("m.name IN (?, ?, ?, ?)").cId("m_name")
				.or("m.address LIKE ?").cId("m_address")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"SELECT *"
			+ "\nFROM member m"
			+ "\nWHERE m.name IN (?, ?, ?, ?)"
			+ "\n  OR m.address LIKE ?";
		assertEquals(expectedSql, sql);
		
		SimpleCondition name1 = root.findById("m_name");
		name1.addParam(new InParameter().id("firstName").type(String.class).value("Bob"))
			.addParam(new InParameter().id("secondName").type(String.class).value("Mary"))
			.addParam(new InParameter().id("thirdName").type(String.class).value("John"))
			.addParam(new InParameter().id("forthName").type(String.class).value("Mark"));
		
		InParameter firstName = root.findParamById("firstName");
		InParameter secondName = root.findParamById("secondName");
		InParameter thirdName = root.findParamById("thirdName");
		InParameter forthName = root.findParamById("forthName");
		
		assertEquals("Bob", firstName.getValue());
		assertEquals("Mary", secondName.getValue());
		assertEquals("John", thirdName.getValue());
		assertEquals("Mark", forthName.getValue());
		
		SimpleCondition address1 = root.findById("m_address");
		address1.addParam(new InParameter().id("address").type(String.class).value("Hualiang"));
		
		InParameter address = root.findParamByNodeId("m_address");
		assertEquals("Hualiang", address.getValue());
		
		System.out.println(sql);
	}
}
