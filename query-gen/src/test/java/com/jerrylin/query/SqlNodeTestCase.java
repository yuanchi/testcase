package com.jerrylin.query;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

public class SqlNodeTestCase {
	@Test
	public void findFirstByType(){
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
		System.out.println("===================");
		Where where = root.findFirstByType(Where.class);
		System.out.println(where.genSql());
	}
	
	private static void copyAssertTemplate(SqlNode root, SqlNode copy){
//		System.out.println(root.genSql());
//		System.out.println(copy.genSql());
		
		assertEquals("genSql is the same", root.genSql(), copy.genSql());
		assertNotSame("root and copy should be different object", root, copy);
		assertFalse("root's attributes object is different from copy's", root.getAttributes() == copy.getAttributes());
		assertTrue("root's attributes content is the same as copy's", root.getAttributes().equals(copy.getAttributes()));
	}
	
	@Test
	public void copyFilterConditinoTargetSelectable(){
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
		SelectExpression copy = root.copy();
		copyAssertTemplate(root, copy);
	}	
	@Test
	public void copySelectTargetSelectable(){
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
		SelectExpression copy = root.copy();
		copyAssertTemplate(root, copy);
	}
	@Test
	public void copySimpleOrderBy(){
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
		SelectExpression copy = root.copy();
		copyAssertTemplate(root, copy);	
	}
	@Test
	public void copyWhereAddGroupConditions(){
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
				.or().newGroup()
					.cond("e.title = 'Header'")
					.and()
					.cond("e.gender = 'F'")
				.toRoot()
		;
		SelectExpression copy = root.copy();
		copyAssertTemplate(root, copy);			
	}
	@Test
	public void transform(){
		SelectExpression root = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("parameter", "p").toRoot()
			.where()
				.cond("p.fname LIKE :f1", "Susan")
				.or()
				.cond("p.fname LIKE :f2", "John")
				.or()
				.cond("p.fname LIKE :f3", "Bob").toRoot()
		;
		root.findByType(ConditionValue.class).transform(sn->{
			ConditionValue cv = ConditionValue.class.cast(sn);
			cv.setVal("_" + cv.getVal() + "_");
		});
		FilterCondition fc1 = root.findById("f1");
		assertEquals("id f1 value should be _Susan_", "_Susan_", fc1.value().getVal());
		FilterCondition fc2 = root.findById("f2");
		assertEquals("id f2 value should be _John_", "_John_", fc2.value().getVal());
		FilterCondition fc3 = root.findById("f3");
		assertEquals("id f3 value should be _Bob_", "_Bob_", fc3.value().getVal());
	}
	@Test
	public void copyAndTransform(){
		SelectExpression rootOri = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond()
					.id("seq", FilterCondition.class)
					.strContain("p.sequence > ?", "10").closest(Where.class)
				.and()
				.cond()
					.id("price", FilterCondition.class)
					.strContain("p.price > ?", "1000").closest(Where.class)
				.and()
				.cond()
					.id("memberPrice", FilterCondition.class)
					.strContain("p.memberPrice > ?", "2000").toRoot()
		;
		SelectExpression rootCopy = rootOri.copy();
		rootCopy.findByType(ConditionValue.class).transform(sn->{
			ConditionValue cv = ConditionValue.class.cast(sn);
			cv.setVal(Integer.class.cast(cv.getVal()) + 200);
		});
		
		FilterCondition fc1 = rootOri.findById("seq");
		FilterCondition fc2 = rootOri.findById("price");
		FilterCondition fc3 = rootOri.findById("memberPrice");
		
		FilterCondition fc4 = rootCopy.findById("seq");
		FilterCondition fc5 = rootCopy.findById("price");
		FilterCondition fc6 = rootCopy.findById("memberPrice");
		
		assertEquals("rootOri's genSql should be the same as rootCopy's", rootOri.genSql(), rootCopy.genSql());
		System.out.println(rootOri.genSql());
		
		assertEquals("rootOri id seq value should be 10", Integer.class.cast(10), Integer.class.cast(fc1.value().getVal()));
		assertEquals("rootOri id price value should be 1000", Integer.class.cast(1000), Integer.class.cast(fc2.value().getVal()));
		assertEquals("rootOri id memberPrice value should be 2000", Integer.class.cast(2000), Integer.class.cast(fc3.value().getVal()));
		
		assertEquals("rootCopy id seq value should be 210", Integer.class.cast(210), Integer.class.cast(fc4.value().getVal()));
		assertEquals("rootCopy id price value should be 1200", Integer.class.cast(1200), Integer.class.cast(fc5.value().getVal()));
		assertEquals("rootCopy id memberPrice value should be 2200", Integer.class.cast(2200), Integer.class.cast(fc6.value().getVal()));
		
	}
	@Test
	public void testIntegerCast(){
		int i = 10;
		System.out.println(Integer.class.cast(i));
	}
	@Test
	public void testChildrenExisted(){
		SelectExpression root1 = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("p.code LIKE ?", "AAA%")
				.and()
				.cond("p.price > ?", 1000).toRoot();
		;
		assertTrue(root1.childrenExisted(GroupConditions.class));
		assertTrue(root1.childrenExisted(Where.class));
		
		SelectExpression root2 = SelectExpression.init()
			.select()
				.t("g.weapon", "weapon")
				.t("g.resource", "resource").toRoot()
			.from()
				.t("game", "g").toRoot()
			.where()
				.cond("g.point > :point", 1000)
				.or()
				.cond("g.loginTime < :loginTime", "2011-10-11")
				.and()
				.newGroup()
					.cond("g.title LIKE :title", "warrior")
					.and()
					.cond("g.level > :level", 100)
					.endGroup().toRoot()
		;
		Where where = root2.findChildExact(Where.class);
		assertTrue(where.childrenExisted(GroupConditions.class, FilterCondition.class));
	}
	@Test
	public void testChildrenOperatorIndex(){
		SelectExpression r1 = SelectExpression.init()
			.select()
				.t("*").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("p.name LIKE ?", "Bob")
				.and()
				.cond("p.code LIK ?", "ABC")
				.or()
				.cond("p.price > ?", 2000).toRoot()
		;
		List<int[]> idx1 = GroupConditions.childrenOperatorIndex(r1.findChildExact(Where.class));
		idx1.forEach(i->{
			System.out.println(Arrays.toString(i));
		});
		assertEquals(2, idx1.size());
		assertEquals(1, idx1.get(0)[0]);
		assertEquals(1, idx1.get(0)[1]);
		assertEquals(3, idx1.get(1)[0]);
		assertEquals(3, idx1.get(1)[1]);
		
		SelectExpression r2 = SelectExpression.init()
				.select()
					.t("*").toRoot()
				.from()
					.t("product", "p").toRoot()
				.where()
					.and()
					.and()
					.or()
					.cond("p.name LIKE ?", "Bob")
					.and()
					.cond("p.code LIK ?", "ABC")
					.or()
					.cond("p.price > ?", 2000).toRoot()
		;
		List<int[]> idx2 = GroupConditions.childrenOperatorIndex(r2.findChildExact(Where.class));
		idx2.forEach(i->{
			System.out.println(Arrays.toString(i));
		});
		assertEquals(3, idx2.size());
		assertEquals(0, idx2.get(0)[0]);
		assertEquals(2, idx2.get(0)[1]);
		assertEquals(4, idx2.get(1)[0]);
		assertEquals(4, idx2.get(1)[1]);
		assertEquals(6, idx2.get(2)[0]);
		assertEquals(6, idx2.get(2)[1]);
		
		SelectExpression r3 = SelectExpression.init()
				.select()
					.t("*").toRoot()
				.from()
					.t("product", "p").toRoot()
				.where()
					.or()
					.cond("p.name LIKE ?", "Bob")
					.and()
					.and()
					.and()
					.cond("p.code LIK ?", "ABC")
					.cond("p.price > ?", 2000)
					.or()
					.and().toRoot()
		;
		List<int[]> idx3 = GroupConditions.childrenOperatorIndex(r3.findChildExact(Where.class));
		idx3.forEach(i->{
			System.out.println(Arrays.toString(i));
		});
		assertEquals(3, idx3.size());
		assertEquals(0, idx3.get(0)[0]);
		assertEquals(0, idx3.get(0)[1]);
		assertEquals(2, idx3.get(1)[0]);
		assertEquals(4, idx3.get(1)[1]);
		assertEquals(7, idx3.get(2)[0]);
		assertEquals(8, idx3.get(2)[1]);
	}
	@Test
	public void testRemoveAssociatedSiblings(){
		SelectExpression r1 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.price", "price").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("p.name LIKE ?", "S%")
				.and()
				.or()// expected keeping this item
				.cond("p.fname LIKE ?", "IO%")
				.and()
				.and().toRoot()
		;
		Where where1 = r1.findChildExact(Where.class);
		r1.removeNotRequiredSiblings();
		System.out.println(r1.genSql());
		
		assertTrue(where1.getChildren().get(0) instanceof FilterCondition);
		assertTrue(where1.getChildren().get(1) instanceof Operator);
		assertTrue(((Operator)where1.getChildren().get(1)).getSymbol().equals("OR"));
		assertTrue(where1.getChildren().get(2) instanceof FilterCondition);
		
		SelectExpression r2 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.price", "price").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.or()
				.and()
				.cond("p.name LIKE ?", "S%")
				.and()
				.or() // expected remaining this item
				.cond("p.fname LIKE ?", "IO%")
				.and()
				.newGroup()
					.and()
					.and()
					.endGroup()
				.and().toRoot()
		;
		Where where2 = r2.findChildExact(Where.class);
		r2.removeNotRequiredSiblings();
		System.out.println(r2.genSql());
			
		assertTrue(where2.getChildren().get(0) instanceof FilterCondition);
		assertTrue(where2.getChildren().get(1) instanceof Operator);
		assertTrue(((Operator)where2.getChildren().get(1)).getSymbol().equals("OR"));
		assertTrue(where2.getChildren().get(2) instanceof FilterCondition);
		SelectExpression r3 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.price", "price").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.and()
				.newGroup()
					.and()
					.cond("p.hint LIKE ?", "xxxxx")
					.and()
					.endGroup()
				.and().toRoot()
		;
		Where where3 = r3.findChildExact(Where.class);
		r3.removeNotRequiredSiblings();
		System.out.println(r3.genSql());
				
		assertTrue(where3.getChildren().get(0) instanceof GroupConditions);
		
		SelectExpression r4 = SelectExpression.init()
			.select()
				.t("p.code", "code")
				.t("p.price", "price").toRoot()
			.from()
				.t("product", "p").toRoot()
			.where()
				.cond("p.fname LIKE ?", "IO%")
				.and()
				.newGroup()
					.and()
					.and()
					.cond()
						.start("p.id")
						.operator("IN")
						.endWithSelect()
							.select()
								.t("p1.id", "id").toStart()
							.from()
								.t("product", "p1").toStart()
							.where()
								.and()
								.or()
								.cond("p1.createDate > ?", "2012-01-01")
								.and()
								.or()
						.toStart().closest(GroupConditions.class)
				.endGroup()
				.and().toRoot()
		;
		Where where4 = r4.findChildExact(Where.class);
		r4.removeNotRequiredSiblings();
		System.out.println(r4.genSql());
	}
}
