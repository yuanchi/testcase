package com.jerrylin.dynasql3;

import java.util.HashMap;
import java.util.Map;

import com.jerrylin.dynasql3.node.CustomExpression;
import com.jerrylin.dynasql3.node.FilterConditions;
import com.jerrylin.dynasql3.node.From;
import com.jerrylin.dynasql3.node.GroupBy;
import com.jerrylin.dynasql3.node.Having;
import com.jerrylin.dynasql3.node.JoinExpression;
import com.jerrylin.dynasql3.node.JoinSubquery;
import com.jerrylin.dynasql3.node.On;
import com.jerrylin.dynasql3.node.OrderBy;
import com.jerrylin.dynasql3.node.RootNode;
import com.jerrylin.dynasql3.node.Select;
import com.jerrylin.dynasql3.node.SelectExpression;
import com.jerrylin.dynasql3.node.SimpleCondition;
import com.jerrylin.dynasql3.node.SimpleExpression;
import com.jerrylin.dynasql3.node.SqlNode;
import com.jerrylin.dynasql3.node.SubqueryCondition;
import com.jerrylin.dynasql3.node.Where;

public class SqlNodeFactory {
	private static final SqlNodeFactory SINGLETON = new SqlNodeFactory();
	private SqlNodeFactory(){
		factories = new HashMap<>();
		
		register(SqlNode.class, new SqlNodeGenerator()) // this is for test, not necessarily
		.register(SelectExpression.class, new SelectExpressionGenerator())
		.register(RootNode.class, new RootNodeGenerator())
		.register(FilterConditions.class, new FilterConditionsGenerator())
		.register(Select.class, new SelectGenerator())
		.register(From.class, new FromGenerator())
		.register(Where.class, new WhereGenerator())
		.register(SimpleCondition.class, new SimpleConditionGenerator())
		.register(SubqueryCondition.class, new SubqueryConditionGenerator())
		.register(SimpleExpression.class, new SimpleExpressionGenerator())
		.register(OrderBy.class, new OrderByGenerator())
		.register(GroupBy.class, new GroupByGenerator())
		.register(Having.class, new HavingGenerator())
		.register(On.class, new OnGenerator())
		.register(JoinExpression.class, new JoinExpressionGenerator())
		.register(JoinSubquery.class, new JoinSubqueryGenerator())
		.register(CustomExpression.class, new CustomExpressionGenerator())
		;
	}
	private Map<Class<? extends SqlNode<?>>, SqlNodeInstantiable<? extends SqlNode<?>>> factories;
	
	public static SqlNodeFactory getSingleton(){
		return SINGLETON;
	}
	public static SqlNodeFactory createInstance(){
		return new SqlNodeFactory();
	}
	
	public <T extends SqlNode<?>>SqlNodeFactory register(Class<T> key, SqlNodeInstantiable<? extends T> factory){
		if(this == SINGLETON){
			throw new UnsupportedOperationException("SINGLETON can't be changed");
		}
		factories.put(key, factory);
		return this;
	}
	public <T extends SqlNode<?>>T create(Class<T> key){
		SqlNodeInstantiable<? extends SqlNode<?>> factory = factories.get(key);
		if(factory == null){
			return null;
		}
		SqlNode<?> initialized = factory.create();
		initialized.setKeyToInitialized((Class<SqlNode<?>>)key);
		return (T)initialized;
	}
	
	@SuppressWarnings("rawtypes")
	private static class SqlNodeGenerator implements SqlNodeInstantiable<SqlNode> {
		@Override
		public SqlNode<?> create() {
			return new SqlNode<>();
		}
	}
	private static class SelectExpressionGenerator implements SqlNodeInstantiable<SelectExpression> {
		@Override
		public SelectExpression<?> create() {
			return new SelectExpression<>();
		}
	}
	private static class RootNodeGenerator implements SqlNodeInstantiable<RootNode> {
		@Override
		public RootNode create() {
			return new RootNode();
		}
	}
	private static class SelectGenerator implements SqlNodeInstantiable<Select>{
		@Override
		public Select create() {
			return new Select();
		}
	}
	private static class FromGenerator implements SqlNodeInstantiable<From>{
		@Override
		public From create() {
			return new From();
		}
	}
	@SuppressWarnings("rawtypes")
	private static class FilterConditionsGenerator implements SqlNodeInstantiable<FilterConditions>{
		@Override
		public FilterConditions<?> create() {
			return new FilterConditions<>();
		}		
	}
	private static class WhereGenerator implements SqlNodeInstantiable<Where>{
		@Override
		public Where create() {
			return new Where();
		}
	}
	private static class SimpleConditionGenerator implements SqlNodeInstantiable<SimpleCondition>{
		@Override
		public SimpleCondition create() {
			return new SimpleCondition();
		}
	}
	private static class SubqueryConditionGenerator implements SqlNodeInstantiable<SubqueryCondition>{
		@Override
		public SubqueryCondition create() {
			return new SubqueryCondition();
		}
	}
	private static class SimpleExpressionGenerator implements SqlNodeInstantiable<SimpleExpression>{
		@Override
		public SimpleExpression create() {
			return new SimpleExpression();
		}
	}
	private static class OrderByGenerator implements SqlNodeInstantiable<OrderBy>{
		@Override
		public OrderBy create() {
			return new OrderBy();
		}
	}
	private static class GroupByGenerator implements SqlNodeInstantiable<GroupBy>{
		@Override
		public GroupBy create() {
			return new GroupBy();
		}
	}
	private static class HavingGenerator implements SqlNodeInstantiable<Having>{
		@Override
		public Having create() {
			return new Having();
		}
	}
	private static class OnGenerator implements SqlNodeInstantiable<On>{
		@Override
		public On create() {
			return new On();
		}
	}
	private static class JoinExpressionGenerator implements SqlNodeInstantiable<JoinExpression>{
		@Override
		public JoinExpression create() {
			return new JoinExpression();
		}
	}
	private static class JoinSubqueryGenerator implements SqlNodeInstantiable<JoinSubquery>{
		@Override
		public JoinSubquery create() {
			return new JoinSubquery();
		}
	}
	private static class CustomExpressionGenerator implements SqlNodeInstantiable<CustomExpression>{
		@Override
		public CustomExpression create() {
			return new CustomExpression();
		}
	}
}
