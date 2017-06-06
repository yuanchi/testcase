package com.jerrylin.dynasql3;

import org.junit.Test;

import com.jerrylin.dynasql3.node.RootNode;
import com.jerrylin.dynasql3.node.SelectExpression;
import com.jerrylin.dynasql3.node.SqlNode;

import static org.junit.Assert.*;

public class SqlNodeFactoryTest {
	@Test
	public void create(){
		SqlNodeFactory factory = SqlNodeFactory.getSingleton();
		
		SqlNode<?> sn = factory.create(SqlNode.class);
		assertTrue(sn.getClass() == SqlNode.class);
		
		SelectExpression se = factory.create(SelectExpression.class);
		assertTrue(se.getClass() == SelectExpression.class);
		
		RootNode root = factory.create(RootNode.class);
		assertTrue(root.getClass() == RootNode.class);
	}
	private static class AnotherNode extends SelectExpression{}
	private static class AnotherNodeGenerator implements SqlNodeInstantiable<AnotherNode>{
		@Override
		public AnotherNode create() {
			return new AnotherNode();
		}
	}
	@Test(expected=UnsupportedOperationException.class)
	public void registerOnSingleton(){
		SqlNodeFactory factory = SqlNodeFactory.getSingleton();
		try{
			factory.register(AnotherNode.class, new AnotherNodeGenerator());
		}catch(UnsupportedOperationException e){
			assertEquals("SINGLETON can't be changed", e.getMessage());
			throw e;
		}
	}
	@Test
	public void registerOnInstance(){
		SqlNodeFactory factory = SqlNodeFactory.createInstance();
		factory.register(AnotherNode.class, new AnotherNodeGenerator());
		
		AnotherNode an1 = factory.create(AnotherNode.class);
		assertTrue(an1.getClass() == AnotherNode.class);
		
		AnotherNode an2 = factory.create(AnotherNode.class);
		assertTrue(an2.getClass() == AnotherNode.class);
		
		assertFalse(an1 == an2);
	}
	@Test
	public void registerToReplaceOriginal(){
		SqlNodeFactory factory = SqlNodeFactory.createInstance();
		
		SelectExpression se1 = factory.create(SelectExpression.class);
		assertTrue(se1.getClass() == SelectExpression.class);
		
		factory.register(SelectExpression.class, new AnotherNodeGenerator());
		SelectExpression se2 = factory.create(SelectExpression.class);
		assertFalse(se2.getClass() == SelectExpression.class);
		assertTrue(se2.getClass() == AnotherNode.class);
		
	}
}
