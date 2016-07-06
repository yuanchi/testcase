package com.jerrylin.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class SelectExpressionTestCase {
	@Test
	public void simple(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("employee", "e").toRoot();
		System.out.println(root.genSql());
	}
	@Test
	public void innerJoin(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("employee", "e")
				.innerJoin().t("account", "a")
					.on("e.emp_id = a.emp_id").toRoot()
				;
		System.out.println(root.genSql());		
	}
	@Test
	public void simpleLeftOuterJoin(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.t("a.acc_id", "a_id")
				.t("a.name").toRoot()
			.from()
				.t("employee", "e")
				.leftOuterJoin().t("account", "a")
					.on("e.emp_id = a.emp_id").toRoot()
				;
		System.out.println(root.genSql());		
	}
	@Test
	public void joinThreeTargets(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.t("a.acc_id", "a_id")
				.t("a.name").toRoot()
			.from()
				.t("employee", "e")
				.leftOuterJoin().t("account", "a")
					.on("e.emp_id = a.emp_id")
				.rightOuterJoin().t("customer", "cus")
					.on("cus.cus_id = e.emp_id")
				.toRoot()
				;
		System.out.println(root.genSql());		
	}
	@Test
	public void simpleWhere(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.t("a.acc_id", "a_id")
				.t("a.name").toRoot()
			.from()
				.t("employee", "e")
				.leftOuterJoin().t("account", "a")
					.on("e.emp_id = a.emp_id")
				.rightOuterJoin().t("customer", "cus")
					.on("cus.cus_id = e.emp_id")
				.toRoot()
			.where()
				.cond("e.start_date > '2011-11-12'")
				.and()
				.cond("e.start_date <= '2013-01-01'")
				.toRoot()
				;
		System.out.println(root.genSql());			
	}
	@Test
	public void whereAddGroupConditions(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("employee", "e").toRoot()
			.where()
				.newGroup()
					.cond("e.start_date > '2011-11-12'")
					.and()
					.cond("e.start_date <= '2013-01-01'")
					.endGroup()
				.or()
				.newGroup()
					.cond("e.title = 'Header'")
					.and()
					.cond("e.gender = 'F'")
				.toRoot()
				;
		System.out.println(root.genSql());			
	}
	@Test
	public void whereAddNestedGroupConditions(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("employee", "e").toRoot()
			.where()
				.newGroup()
					.cond("e.start_date > '2011-11-12'")
					.and()
						.newGroup()
							.cond("e.title = 'Header'")
							.and()
							.cond("e.gender = 'F'")
				.toRoot()
				;
		System.out.println(root.genSql());			
	}	
	@Test
	public void simpleOrderBy(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.t("a.acc_id", "a_id")
				.t("a.name").toRoot()
			.from()
				.t("employee", "e")
				.leftOuterJoin().t("account", "a")
					.on("e.emp_id = a.emp_id")
				.rightOuterJoin().t("customer", "cus")
					.on("cus.cus_id = e.emp_id")
				.toRoot()
			.where()
				.cond("e.start_date > '2011-11-12'")
				.and()
				.cond("e.start_date <= '2013-01-01'")
				.toRoot()
			.orderBy()
				.t("e.emp_id DESC")
				.t("a.acc_id ASC")
				.toRoot()
				;
		System.out.println(root.genSql());		
	}
	@Test
	public void selectTargetSelectable(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.startSelect("country")
					.select()
						.t("country").toStart()
					.from()
						.t("countryInfo", "ci").toStart()
					.where()
						.cond("ci.name LIKE 'S%'")
						.and()
						.cond("ci.code LIKE '1%'")
			.preSelect() // required if there is yet item added
				.t("e.name", "e_name")	
				.toRoot()
			.from()
				.t("employee", "e")
				.toRoot()
				;
		System.out.println(root.genSql());		
	}
	@Test
	public void filterConditinoTargetSelectable(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("e.emp_id", "e_id")
				.startSelect("country")
					.select()
						.t("country").toStart()
					.from()
						.t("countryInfo", "ci").toStart()
					.where()
						.cond("ci.name LIKE 'S%'")
						.and()
						.cond("ci.code LIKE '1%'")
				.toRoot()
			.from()
				.t("employee", "e")
				.toRoot()
				;
		System.out.println(root.genSql());
	}
	@Test
	public void conditionIdxValuePairs(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("m.fbName", "fbName")
				.t("m.mobile", "mobile").toRoot()
			.from()
				.t("member", "m").toRoot()
			.where()
				.strExact("m.fname LIKE ?", "John")
				.and()
				.strExact("m.lname LIKE ?", "Lin")
				.or()
				.cond("m.mobile >= ?", "0988112331").toRoot()
			;
		Map<Integer, Object> indexed = root.conditionIndexValuePairs();
		
		assertEquals("first condition value should be John", "John", indexed.get(0));
		assertEquals("second condition value should be Lin", "Lin", indexed.get(1));
		assertEquals("third condition value should be 0988112331", "0988112331", indexed.get(2));
		
		System.out.println(root.genSql());
	}
	@Test
	public void conditionIdValuePairs(){
		SelectExpression root1 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.sequence", "sequence")
				.t("p.dest", "dest").toRoot()
			.from()
				.t("parameter", "p").toRoot()
			.where()
				.newGroup()
					.cond("p.createDate >= :c1startDate", "2016-01-01")
					.and()
					.cond("p.createDate <= :c1endDate", "2016-12-31")
					.endGroup()
				.or()
				.newGroup()
					.cond("p.updateDate >= :u1startDate", "2015-01-01")
					.and()
					.cond("p.updateDate <= :u1endDate", "2015-12-31")
					.endGroup().toRoot()
			;
		Map<String, Object> idParams1 = root1.conditionIdValuePairs();
		assertEquals("if id is c1startDate, value should be 2016-01-01", "2016-01-01", idParams1.get("c1startDate"));
		assertEquals("if id is c1endDate, value should be 2016-12-31", "2016-12-31", idParams1.get("c1endDate"));
		assertEquals("if id is u1startDate, value should be 2015-01-01", "2015-01-01", idParams1.get("u1startDate"));
		assertEquals("if id is u1endDate, value should be 2015-12-31", "2015-12-31", idParams1.get("u1endDate"));		
		
		SelectExpression root2 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.sequence", "sequence")
				.t("p.dest", "dest").toRoot()
			.from()
				.t("parameter", "p").toRoot()
			.where()
				.newGroup()
					.cond("p.createDate >= :c2startDate", "2017-01-01")
					.and()
					.cond("p.createDate <= :c2endDate", "2017-12-31")
					.endGroup()
				.or()
				.newGroup()
					.cond("p.updateDate >= :u2startDate", "2018-01-01")
					.and()
					.cond("p.updateDate <= :u2endDate", "2018-12-31")
					.endGroup().toRoot()
						
				;
		Map<String, Object> idParams2 = root2.conditionIdValuePairs();
		assertEquals("if id is c2startDate, value should be 2017-01-01", "2017-01-01", idParams2.get("c2startDate"));
		assertEquals("if id is c2endDate, value should be 2017-12-31", "2017-12-31", idParams2.get("c2endDate"));
		assertEquals("if id is u2startDate, value should be 2018-01-01", "2018-01-01", idParams2.get("u2startDate"));
		assertEquals("if id is u2endDate, value should be 2018-12-31", "2018-12-31", idParams2.get("u2endDate"));
		
		System.out.println(root1.genSql());
		System.out.println(root2.genSql());
	}
	@Test
	public void filterOut(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("g.weapon", "weapon")
				.t("g.armor", "armor")
				.t("g.shield", "shield").toRoot()
			.from()
				.t("game", "g").toRoot()
			.where()
				.cond("g.loginCount > ?", 10)
				.and()
				.cond("g.point > ?", 1000).toRoot()
		;
		root.filterOut(sn->sn.getParent() != null && Where.class == sn.getParent().getClass());
		List<SqlNode> whereChildren = root.findFirstByType(Where.class).getChildren();
		assertEquals(0, whereChildren.size());
		System.out.println(root.genSql());
		
		SelectExpression root1 = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("p.createDate > :startDate", "2011-06-15")
				.and()
				.cond("p.createDate < :endDate", "2015-09-14").toRoot();
			root1.filterOut(sn-> "startDate".equals(sn.attr("id")) || "endDate".equals(sn.attr("id")));
			assertEquals(0, root1.findFirstByType(Where.class).getChildren().size());
			System.out.println(root1.genSql());
		;
	}
	@Test
	public void changeNode(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("u.code", "code")
				.t("u.id", "id")
				.t("u.nickname", "nickname")
				.t("r.name").toRoot()
			.from()
				.t("user", "u")
				.leftOuterJoin()
				.t("role", "r")
				.on("u.id = r.userId").toRoot()
			.where()
				.cond("u.code LIKE ?", "A%")
				.or()
				.cond("u.code LIKE ?", "S%").toRoot()
		;
		root.changeNode(sn->{
			String alias = sn.attr("alias");
			if(StringUtils.isNotBlank(alias)){
				sn.attr("alias", alias + "_");
			}
		});
		List<SqlNode> found = root.find(sn->sn.attr("alias")!=null).getFound(SqlNode.class);
		assertEquals(5, found.size());
		List<String> aliases = found.stream().map(sn->sn.attr("alias")).collect(Collectors.toList());
		assertTrue(aliases.contains("code_"));
		assertFalse(aliases.contains("code"));
		assertTrue(aliases.contains("id_"));
		assertFalse(aliases.contains("id"));
		assertTrue(aliases.contains("nickname_"));
		assertFalse(aliases.contains("nickname"));
		assertTrue(aliases.contains("u_"));
		assertFalse(aliases.contains("u"));
		assertTrue(aliases.contains("r_"));
		assertFalse(aliases.contains("r"));
		
		System.out.println(root.genSql());
	}
	@Test
	public void removeNotRequiredSiblings(){
		SelectExpression r1 = SelectExpression.init()
			.select()
				.t("p.name", "name")
				.startSelect("salesId")
					.select()
						.t("s.id", "sId").toStart()
					.from()
						.t("sales", "s").toStart()
					.where()
						.cond("s.orderDate > ?", "2011-10-10")
						.and()
						.or()
						.and()
						.cond("s.orderDate < ?", "2013-01-01")
						.or().preSelect()
				.toRoot()
			.from()
				.startSelect("p")
					.select()
						.t("name")
						.t("price")
						.t("shippingDate").toStart()
					.from()
						.t("product").toStart()
					.where()
						.or()
						.and()
						.cond("product.code LIKE ?", "AA%")
						.and().preFrom()
				.toRoot()
			.where()
				.cond("p.price > ?", 2000)
				.and()
				.or()
				.and()
				.cond("p.shippingDate > ?", "2011-09-10")
				.or().toRoot()
		;
		r1.removeNotRequiredSiblings();
		System.out.println(r1.genSql());
	}
	@Test
	public void defaultFilterOut(){
		SelectExpression r1 = SelectExpression.init()
			.select()
				.t("c.name", "cName")
				.t("c.code", "cCode").toRoot()
			.from()
				.t("customer", "c")
				.leftOuterJoin()
				.t("order", "o")
				.on("c.cus_id = o.cus_id").toRoot()
			.where()
				.valueExpected("c.birth >= :startBirth")
				.and()
				.valueExpected("c.birth <= :endBirth").toRoot();
		;
		r1.defaultFilterOut();
		System.out.println(r1.genSql());
		FilterCondition fc1 = r1.findById("startBirth");
		assertEquals(null, fc1);
		FilterCondition fc2 = r1.findById("endBirth");
		assertEquals(null, fc2);
		
		SelectExpression r2 = SelectExpression.init()
			.select()
				.t("c.name", "cName")
				.t("c.code", "cCode").toRoot()
			.from()
				.t("customer", "c")
				.leftOuterJoin()
				.t("order", "o")
				.on("c.cus_id = o.cus_id").toRoot()
			.where()
				.valueExpected("c.birth >= :startBirth", "2011-10-10")
				.and()
				.valueExpected("c.birth <= :endBirth", "2015-06-07").toRoot();
		;
		r2.defaultFilterOut();
		System.out.println(r2.genSql());
		FilterCondition fc3 = r2.findById("startBirth");
		assertTrue(fc3 != null); 
		FilterCondition fc4 = r2.findById("endBirth");
		assertTrue(fc4 != null);
		
		SelectExpression r3 = SelectExpression.init()
			.select()
				.t("c.name", "cName")
				.t("c.code", "cCode").toRoot()
			.from()
				.t("customer", "c")
				.leftOuterJoin()
				.t("order", "o")
				.on("c.cus_id = o.cus_id").toRoot()
			.where()
				.valueExpected("c.birth >= :startBirth")
				.and()
				.valueExpected("c.birth <= :endBirth").toRoot();
		;
		FilterCondition fc5 = r3.findById("startBirth");
		fc5.value("2009-01-19");
		FilterCondition fc6 = r3.findById("endBirth");
		fc6.value("2009-08-21");
		r3.defaultFilterOut();
		System.out.println(r3.genSql());
		FilterCondition fc5_ = r3.findById("startBirth");
		assertTrue(fc5_ != null);
		FilterCondition fc6_ = r3.findById("endBirth");
		assertTrue(fc6_ != null);
	}
}
