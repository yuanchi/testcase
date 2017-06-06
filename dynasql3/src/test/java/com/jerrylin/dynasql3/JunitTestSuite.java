package com.jerrylin.dynasql3;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.jerrylin.dynasql3.node.CustomExpressionTest;
import com.jerrylin.dynasql3.node.FilterConditionsTest;
import com.jerrylin.dynasql3.node.FromTest;
import com.jerrylin.dynasql3.node.JoinExpressionTest;
import com.jerrylin.dynasql3.node.JoinSubqueryTest;
import com.jerrylin.dynasql3.node.RootNodeTest;
import com.jerrylin.dynasql3.node.SelectExpressionTest;
import com.jerrylin.dynasql3.node.SimpleConditionTest;
import com.jerrylin.dynasql3.node.SimpleExpressionTest;
import com.jerrylin.dynasql3.node.SqlNodeTest;
import com.jerrylin.dynasql3.node.SubqueryConditionTest;

@RunWith(Suite.class)

@Suite.SuiteClasses({
	ExpressionParameterizableTest.class,
	SqlNodeFactoryTest.class,
	SqlNodeTest.class,
	SelectExpressionTest.class,
	RootNodeTest.class,
	SimpleExpressionTest.class,
	JoinExpressionTest.class,
	FilterConditionsTest.class,
	JoinSubqueryTest.class,
	FromTest.class,
	SimpleConditionTest.class,
	SubqueryConditionTest.class,
	CustomExpressionTest.class
})
public class JunitTestSuite {}
