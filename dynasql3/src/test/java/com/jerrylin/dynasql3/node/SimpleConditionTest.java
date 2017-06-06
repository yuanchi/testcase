package com.jerrylin.dynasql3.node;

import org.junit.Test;

public class SimpleConditionTest {
	@Test
	public void toSql(){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression("p.id = '1'");
		String sql = sc.toSql();
		System.out.println(sql);
	}
}
