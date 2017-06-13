package com.jerrylin.dynasql3.node;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class SimpleConditionTest {
	@Test
	public void toSql(){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression("p.id = '1'");
		String sql = sc.toSql();
		assertEquals("p.id = '1'", sql);
	}
	@Test
	public void getTableReferences(){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression("ert.p.name.b.p.q!=weq.od.qq.pr");
		List<String> refs = sc.getTableReferences();
		assertEquals("ert", refs.get(0));
		assertEquals("weq", refs.get(1));
	}
	@Test
	public void replaceExprWith(){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression("pew.p.name.b.p.q=oop.eeq.qq");
		sc.replaceExprWith("w");
		// expression not changed because of table references are not the same
		assertEquals("pew.p.name.b.p.q=oop.eeq.qq", sc.getExpression());
		
		sc.setExpression("pew.p.name.b.p.q=pew.eeq.qq");
		sc.replaceExprWith("w");
		// expression has been changed
		assertEquals("w.p.name.b.p.q=w.eeq.qq", sc.getExpression());
	}
}
