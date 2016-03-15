package com.jerrylin.erp.sql.condition;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.Where;


public class CollectConds extends SqlCondition{
	public static CollectConds getInstance(){
		return new CollectConds();
	}
	public CollectConds addCond(ISqlCondition cond){
		addChild(cond);
		return this;
	}
	public CollectConds collectConds(){
		CollectConds collectConds = CollectConds.getInstance();
		addChild(collectConds);
		return collectConds;
	}
	public CollectConds andCollectConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.AND);
		return conds;
	}
	public CollectConds orCollectConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.OR);
		return conds;		
	}
	public Where getWhere(){
		ISqlNode parent = getParent();
		if(parent instanceof Where){
			return (Where)parent;
		}
		throw new RuntimeException("");
	}
	private SimpleCondition simpleCond(String expression, Class<?> type, Object val){
		SimpleCondition s = new SimpleCondition()
								.expression(expression)
								.type(type)
								.value(val);
		addChild(s);
		return s;
	}
	public CollectConds andSimpleCond(String expression, Class<?> type, Object val){
		SimpleCondition s = simpleCond(expression, type, val);
		s.junction(Junction.AND);
		return this;
	}
	public CollectConds andSimpleCond(String expression, Class<?> type){
		andSimpleCond(expression, type, null);
		return this;
	}
	public CollectConds orSimpleCond(String expression, Class<?> type, Object val){
		SimpleCondition s = simpleCond(expression, type, val);
		s.junction(Junction.OR);
		return this;
	}	
	public CollectConds orSimpleCond(String expression, Class<?> type){
		orSimpleCond(expression, type, null);
		return this;
	}
	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
			.map(ISqlNode::genSql)
			.collect(Collectors.toList());
		String result = StringUtils.join(items, "\n      "); 
		String and = Junction.AND.toString();
		if(result.indexOf(and) == 0){
			result = result.substring(4, result.length());
		}
		String or = Junction.OR.toString();
		if(result.indexOf(or) == 0){
			result = result.substring(3, result.length());
		}
		return getJunction().toString() + " (" + result + ")";
	}	
}
