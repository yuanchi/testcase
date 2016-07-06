package com.jerrylin.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Date;

import org.junit.Test;

public class FilterConditionTestCase {
	@Test
	public void defaultIgnoreValueNodeWhenGenSql(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.startSelect("country")
					.select()
						.t("country").toStart()
					.from()
						.t("countryInfo", "ci").toStart()
					.where()
						.strStartWith("ci.name LIKE :ci_name", "Jo")
						.and()
						.strIgnoreCase("ci.code LIKE :ci_cide", "Venus")
				.toRoot()
			.from()
				.t("employee", "e")
				.toRoot()
				;
		System.out.println(root.genSql());
	}
	@Test
	public void addValueNodeWhenGenSql(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.startSelect("country")
					.select()
						.t("country").toStart()
					.from()
						.t("countryInfo", "ci").toStart()
					.where()
						.strStartWith("ci.name LIKE ", "'Jo'")
						.and()
						.strIgnoreCase("ci.code LIKE 'Venus'")
				.toRoot()
			.from()
				.t("employee", "e")
				.toRoot()
				;
		FilterCondition fc = root.findFirstByType(FilterCondition.class);
		fc.setValueAddedWhenGenSql(true);
		String sql = root.genSql();
		System.out.println(sql);
		assertTrue("Value 'Jo' should be included", sql.contains("ci.name LIKE 'Jo'"));
	}
	@Test
	public void defaultAddIdIfThirdPartStartsWithColon(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.startSelect("country")
					.select()
						.t("country").toStart()
					.from()
						.t("countryInfo", "ci").toStart()
					.where()
						.strStartWith("ci.name LIKE :ci_name", "Jo")
						.and()
						.strIgnoreCase("ci.code LIKE :ci_code")
				.toRoot()
			.from()
				.t("employee", "e")
				.toRoot()
				;
		FilterCondition fc1 = root.findById("ci_name");
		FilterCondition fc2 = root.findById("ci_code");
		
		assertFalse("fc1 Object is different from fc2", fc1 == fc2);
		assertEquals("Ids should be the ci_name", fc1.attr("id"), "ci_name");
		assertEquals("Ids should be the ci_code", fc2.attr("id"), "ci_code");
	}
	@Test
	public void setFilterConditionId(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("p.id", "p_id")
				.t("p.name", "p_name").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond()
					.id("p_start_date", FilterCondition.class)
					.content("p.start_date >= ?", new Date(System.currentTimeMillis()))
					.closest(Where.class)
				.cond()
					.id("p_end_date", FilterCondition.class)
					.content("p.end_date <= ?", new Date(System.currentTimeMillis()))
				.toRoot()
		;
		FilterCondition fc1 = root.findById("p_start_date");
		FilterCondition fc2 = root.findById("p_end_date");
		
		assertEquals("fc1 id is p_start_date", fc1.attr("id"), "p_start_date");
		assertEquals("fc2 id is p_end_date", fc2.attr("id"), "p_end_date");
		
		System.out.println(root.genSql());
	}
	@Test
	public void setFilterConditionIdInGroupConditions(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("s.mobile", "s_mobile")
				.t("s.orderDate", "s_orderDate").toRoot()
			.from()
				.t("sales", "s").toRoot()
			.where()
				.cond()
					.id("start_order_date", FilterCondition.class)
					.strContain("s.orderDate >= ?", "2011-11-12")
					.closest(Where.class)
				.and()
				.cond()
					.id("end_order_date", FilterCondition.class)
					.strContain("s.orderDate <= ?", "2013-11-12")
					.closest(Where.class)
				.and()
				.newGroup()
					.cond()
						.id("s_owner", FilterCondition.class)
						.strStartWith("s.owner LIKE ?", "Sh")
						.closest(GroupConditions.class)
					.and()
					.cond()
						.id("s_address", FilterCondition.class)
						.strEndWith("s.address LIKE ?", "xxx")
						.closest(GroupConditions.class)
						.endGroup()
				.and()
				.strIgnoreCase("s.lastName LIKE ?", "Yuiosd")
				.toRoot()	
		;
		
		FilterCondition owner = root.findById("s_owner");
		FilterCondition address = root.findById("s_address");
		FilterCondition endOrderDate = root.findById("end_order_date");
		
		assertEquals("owner id is s_owner", owner.attr("id"), "s_owner");
		assertEquals("address id is s_address", address.attr("id"), "s_address");
		assertEquals("endOrderDate id is end_order_date", endOrderDate.attr("id"), "end_order_date");
		
		System.out.println(root.genSql());
		
	}
	@Test
	public void conditionStartPartWithFunction(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("UPPER(p.name) LIKE ?", "JOHN").toRoot()
		;
		SimpleExpression se = root.findFirstByType(FilterCondition.class).findFirstByType(SimpleExpression.class);
		assertEquals("UPPER(p.name)", se.getExpression());
		System.out.println(root.genSql());
	}
	@Test
	public void valueExpected(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("customer", "c").toRoot()
			.where()
				.cond()
					.id("code", FilterCondition.class)
					.content("c.code LIKE ?")
					.valueExpected().toRoot()
		;
		System.out.println(root.genSql());
		FilterCondition fc = root.findById("code"); 
		assertTrue(fc.getValueFeatures().isValueExpected());
	}
}
