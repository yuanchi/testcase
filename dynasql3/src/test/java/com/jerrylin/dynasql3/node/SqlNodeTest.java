package com.jerrylin.dynasql3.node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.jerrylin.dynasql3.Aliasible;
import com.jerrylin.dynasql3.Expressible;
import com.jerrylin.dynasql3.SqlNodeFactory;
import com.jerrylin.dynasql3.util.SearchCondition;

public class SqlNodeTest {
	@Test
	public void config(){
		SqlNode<?> root = new SqlNode<>();
		root.config(me->{
			// do nothing
		});
	}
	@Test
	public void topMostFromLevel0(){
		SqlNode<?> root = new SqlNode<>();
		SqlNode<?> topMost = root.topMost();
		assertTrue(root == topMost);
	}
	@Test
	public void topMostFromLevel1(){
		SqlNode<?> root = new SqlNode<>();
		SqlNode<?> level1 = new SqlNode<>();
		root.add(level1);
		SqlNode<?> topMost = level1.topMost();
		assertTrue(root == topMost);
	}
	@Test
	public void topMostFromLevel2(){
		SqlNode<?> root = new SqlNode<>();
		SqlNode<?> level1 = new SqlNode<>();
		root.add(level1);
		SqlNode<?> level2 = new SqlNode<>();
		level1.add(level2);
		SqlNode<?> topMost = level2.topMost();
		assertTrue(root == topMost);
	}
	@Test
	public void rootParentIsSelf(){
		SqlNode<?> root = new SqlNode<>();
		root.setParent(root);
		SqlNode<?> level1 = new SqlNode<>();
		root.add(level1);
		SqlNode<?> level2 = new SqlNode<>();
		level1.add(level2);
		SqlNode<?> topMost = level2.topMost();
		assertTrue(root == topMost);
	}
	private static class SqlNode1 extends SqlNode<SqlNode1>{}
	private static class SqlNode2 extends SqlNode1{}
	private static class SqlNode3 extends SqlNode2{}
	@Test
	public void copy(){
		SqlNodeFactory factory = SqlNodeFactory.createInstance();
		factory.register(SqlNode1.class, ()->new SqlNode1());
		factory.register(SqlNode2.class, ()->new SqlNode2());
		factory.register(SqlNode3.class, ()->new SqlNode3());
		
		SqlNode<?> root = factory.create(SqlNode.class);
		root.setFactory(factory);
		SqlNode1 n1 = factory.create(SqlNode1.class);
		SqlNode1 n2 = factory.create(SqlNode2.class);
		SqlNode1 n3 = factory.create(SqlNode3.class);
		root.add(n1).add(n2).add(n3);
		
		SqlNode<?> copyRoot = root.copy();
		assertFalse(root == copyRoot);
		assertTrue(copyRoot.getClass() == SqlNode.class);
		
		SqlNode<?> copyChild1 = copyRoot.getChildren().get(0);
		assertFalse(n1 == copyChild1);
		assertTrue(copyChild1.getClass() == SqlNode1.class);
		
		SqlNode<?> copyChild2 = copyRoot.getChildren().get(1);
		assertFalse(n2 == copyChild2);
		assertTrue(copyChild2.getClass() == SqlNode2.class);
		
		SqlNode<?> copyChild3 = copyRoot.getChildren().get(2);
		assertFalse(n3 == copyChild3);
		assertTrue(copyChild3.getClass() == SqlNode3.class);		
	}
	@Test
	public void findFirst(){
		SqlNode<?> root = new SqlNode<>();
		
		SqlNode<?> c1 = new SqlNode<>();
		SqlNode<?> c2 = new SqlNode<>();
		SqlNode<?> c3 = new SqlNode<>();
		
		SqlNode<?> c1_1 = new SqlNode1();
		SqlNode<?> c1_2 = new SqlNode<>();
		SqlNode<?> c2_1 = new SqlNode2();
		SqlNode<?> c3_1 = new SqlNode<>();
		
		SqlNode<?> c1_1_1 = new SqlNode1();
		SqlNode<?> c1_1_2 = new SqlNode<>();
		SqlNode<?> c2_1_1 = new SqlNode<>();
		SqlNode<?> c2_1_2 = new SqlNode3();
		
		root.add(c1).add(c2).add(c3);
		c1.add(c1_1).add(c1_2);
		c2.add(c2_1);
		c3.add(c3_1);
		c1_1.add(c1_1_1).add(c1_1_2);
		c2_1.add(c2_1_1).add(c2_1_2);
		
		SqlNode1 f1 = root.findFirst(c->SqlNode1.class.isInstance(c));
		assertTrue(c1_1 == f1);
		
		SqlNode2 f2 = root.findFirst(c->SqlNode2.class.isInstance(c));
		assertTrue(c2_1 == f2);
		
		SqlNode3 f3 = root.findFirst(c->SqlNode3.class.isInstance(c));
		assertTrue(c2_1_2 == f3);
	}
	@Test
	public void findWithId(){
		SqlNode<?> root = new SqlNode<>();
		
		SqlNode<?> c1 = new SqlNode<>().id("c1");
		SqlNode<?> c2 = new SqlNode<>().id("c2");
		SqlNode<?> c3 = new SqlNode<>().id("c3");
		
		SqlNode<?> c1_1 = new SqlNode<>().id("c1_1");
		SqlNode<?> c1_2 = new SqlNode<>().id("c1_2");
		SqlNode<?> c2_1 = new SqlNode2().id("c2_1");
		SqlNode<?> c3_1 = new SqlNode<>().id("c3_1");
		
		SqlNode<?> c1_1_1 = new SqlNode1().id("c1_1_1");
		SqlNode<?> c1_1_2 = new SqlNode<>().id("c1_1_2");
		SqlNode<?> c2_1_1 = new SqlNode<>().id("c2_1_1");
		SqlNode<?> c2_1_2 = new SqlNode3().id("c2_1_2");
		
		root.add(c1).add(c2).add(c3);
		c1.add(c1_1).add(c1_2);
		c2.add(c2_1);
		c3.add(c3_1);
		c1_1.add(c1_1_1).add(c1_1_2);
		c2_1.add(c2_1_1).add(c2_1_2);
		
		SqlNode<?> f1 = root.findWith(SearchCondition.id("c1_1_1"));
		assertTrue(c1_1_1 == f1);
		SqlNode<?> f2 = root.findWith(SearchCondition.id("c3_1"));
		assertTrue(c3_1 == f2);
		SqlNode<?> f3 = root.findWith(SearchCondition.id("c2_1_2"));
		assertTrue(c2_1_2 == f3);
	}
	private static class SqlNode4 extends SqlNode<SqlNode4> implements Expressible{
		private String expression;
		@Override
		public String getExpression() {
			return expression;
		}
		@Override
		public void setExpression(String expression) {
			this.expression = expression;
		}
	}
	@Test
	public void findWithExpr(){
		SqlNode<?> root = new SqlNode<>();
		
		SqlNode<?> c1 = new SqlNode<>().id("c1");
		SqlNode<?> c2 = new SqlNode<>().id("c2");
		SqlNode4 c3 = new SqlNode4().id("c3");
		c3.setExpression("p.id = 'xxx'");
		
		SqlNode<?> c1_1 = new SqlNode<>().id("c1_1");
		SqlNode<?> c1_2 = new SqlNode<>().id("c1_2");
		SqlNode4 c2_1 = new SqlNode4().id("c2_1");
		c2_1.setExpression("p.address");
		SqlNode<?> c3_1 = new SqlNode<>().id("c3_1");
		
		SqlNode4 c1_1_1 = new SqlNode4().id("c1_1_1");
		c1_1_1.setExpression("EMPLOYEE");
		SqlNode<?> c1_1_2 = new SqlNode<>().id("c1_1_2");
		SqlNode<?> c2_1_1 = new SqlNode<>().id("c2_1_1");
		SqlNode<?> c2_1_2 = new SqlNode3().id("c2_1_2");
		
		root.add(c1).add(c2).add(c3);
		c1.add(c1_1).add(c1_2);
		c2.add(c2_1);
		c3.add(c3_1);
		c1_1.add(c1_1_1).add(c1_1_2);
		c2_1.add(c2_1_1).add(c2_1_2);
		
		SqlNode<?> f1 = root.findWith(SearchCondition.expr("p.id = 'xxx'"));
		assertTrue(c3 == f1);
		SqlNode<?> f2 = root.findWith(SearchCondition.expr("p.address"));
		assertTrue(c2_1 == f2);
		SqlNode<?> f3 = root.findWith(SearchCondition.expr("EMPLOYEE"));
		assertTrue(c1_1_1 == f3);
	}
	private static class SqlNode5 extends SqlNode<SqlNode5> implements Aliasible<SqlNode5>{
		private String alias;
		@Override
		public String getAlias() {
			return alias;
		}
		@Override
		public void setAlias(String alias) {
			this.alias = alias;
		}
	}
	@Test
	public void findWithAlias(){
		SqlNode<?> root = new SqlNode<>();
		
		SqlNode<?> c1 = new SqlNode<>().id("c1");
		SqlNode5 c2 = new SqlNode5().id("c2");
		SqlNode<?> c3 = new SqlNode<>().id("c3");
		c2.setAlias("emp");
		
		SqlNode<?> c1_1 = new SqlNode<>().id("c1_1");
		SqlNode<?> c1_2 = new SqlNode<>().id("c1_2");
		SqlNode<?> c2_1 = new SqlNode2().id("c2_1");
		SqlNode5 c3_1 = new SqlNode5().id("c3_1");
		c3_1.setAlias("sal");
		
		SqlNode<?> c1_1_1 = new SqlNode1().id("c1_1_1");
		SqlNode5 c1_1_2 = new SqlNode5().id("c1_1_2");
		SqlNode<?> c2_1_1 = new SqlNode<>().id("c2_1_1");
		SqlNode<?> c2_1_2 = new SqlNode3().id("c2_1_2");
		c1_1_2.setAlias("addr");
		
		root.add(c1).add(c2).add(c3);
		c1.add(c1_1).add(c1_2);
		c2.add(c2_1);
		c3.add(c3_1);
		c1_1.add(c1_1_1).add(c1_1_2);
		c2_1.add(c2_1_1).add(c2_1_2);
		
		SqlNode<?> f1 = root.findWith(SearchCondition.alias("emp"));
		assertTrue(c2 == f1);
		SqlNode<?> f2 = root.findWith(SearchCondition.alias("sal"));
		assertTrue(c3_1 == f2);
		SqlNode<?> f3 = root.findWith(SearchCondition.alias("addr"));
		assertTrue(c1_1_2 == f3);
	}
}
