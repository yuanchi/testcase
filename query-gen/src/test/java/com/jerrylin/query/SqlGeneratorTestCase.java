package com.jerrylin.query;
import org.junit.Test;
import static org.junit.Assert.*;
public class SqlGeneratorTestCase {
	@Test
	public void defaultPk(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("user", "u").toRoot()
		;
		SqlGenerator sg = root.getSqlGenerator();
		assertEquals(SqlGenerator.DEFAULT_PK, sg.getPk());
		System.out.println(sg.sql());
	}
	@Test
	public void customizedPk(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("c.cName", "cName")
				.t("c.eName", "eName")
				.t("c.address", "addess").toRoot()
			.from()
				.t("customer", "c")
				.pk("cusId")
				.leftOuterJoin()
				.t("account", "a")
				.on("c.cusId = a.cusId")
				.pk("accId").toRoot()
			.where()
				.cond()
					.id("cTitle", FilterCondition.class)
					.strContain("c.title = ?", "VIP").closest(Where.class)
				.and()
				.cond()
					.id("aBalance", FilterCondition.class)
					.strContain("a.balance >= ?", "20000").toRoot()
		;
		SqlGenerator sg = root.getSqlGenerator();
		assertEquals("cusId", sg.getPk());
		System.out.println(sg.sql());
	}
	@Test
	public void sql(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("p.stock", "stock")
				.t("p.qr", "qr").toRoot()
			.from()
				.t("product", "p")
				.pk("productId").toRoot()
		;
		String sql = root.getSqlGenerator().sql();
		System.out.println(sql);
		String expected = 
			"SELECT p.stock stock,"
			+ "\n  p.qr qr"
			+ "\nFROM product p"
			+ "\nORDER BY p.productId DESC";
		assertEquals(expected, sql);
	}
}
