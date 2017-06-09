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
		System.out.println(sql);
	}
	@Test
	public void getTableReference(){
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
		
//		assertEquals("w.p.name.b.p.q", sc.getExpression());
	}
}
