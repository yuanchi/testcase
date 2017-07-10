package com.jerrylin.dynasql3.node;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.Junctible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class FilterConditions<T extends FilterConditions<?>> extends SqlNode<T> implements
		Junctible {
	private static final long serialVersionUID = 4179936170286621054L;
	
	private String junction;
	@Override
	public String getJunction() {
		return junction;
	}
	@Override
	public void setJunction(String junction) {
		this.junction = junction;
	}

	private <S extends SqlNode<?> & Junctible>T addAnd(S junctible){
		junctible.setJunction(AND);
		return thisType();
	}
	private <S extends SqlNode<?> & Junctible>T addOr(S junctible){
		junctible.setJunction(OR);
		return thisType();
	}
	
	public T and(SimpleCondition sc){
		addAnd(sc);
		return add(sc);
	}
	public T and(String expression){
		SimpleCondition sc = createBy(SimpleCondition.class);
		sc.setExpression(expression);
		return and(sc);
	}
	public T and(FilterConditions<?> conds){
		addAnd(conds);
		return add(conds);
	}
	public T and(Consumer<FilterConditions<?>> consumer){
		FilterConditions<?> fc = createBy(FilterConditions.class);
		and(fc);
		consumer.accept(fc);
		return thisType();
	}
	public T and(String prefix, Consumer<SelectExpression<?>> consumer){
		SubqueryCondition sc = createBy(SubqueryCondition.class);
		addAnd(sc);
		sc.setPrefix(prefix);
		sc.subquery(consumer);
		return add(sc);
	}
	
	public T or(SimpleCondition sc){
		addOr(sc);
		return add(sc);
	}
	public T or(String expression){
		SimpleCondition sc = createBy(SimpleCondition.class);
		sc.setExpression(expression);
		return or(sc);
	}
	public T or(FilterConditions<?> conds){
		addOr(conds);
		return add(conds);
	}
	public T or(Consumer<FilterConditions<?>> consumer){
		FilterConditions<?> fc = createBy(FilterConditions.class);
		or(fc);
		consumer.accept(fc);
		return thisType();
	}
	public T or(String prefix, Consumer<SelectExpression<?>> consumer){
		SubqueryCondition sc = createBy(SubqueryCondition.class);
		addOr(sc);
		sc.setPrefix(prefix);
		sc.subquery(consumer);
		return add(sc);
	}
	
	@Override
	public T copy(){
		T t = super.copy();
		t.setJunction(junction);
		return t;
	}
	@Override
	public String toSql(){
		List<String> collect = new ArrayList<>();
		List<SqlNode<?>> children = getChildren();
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> c = children.get(i);
			String r = c.toSql();
			if(FilterConditions.class.isInstance(c)){
				if(children.size() > 1 && c.getChildren().size() > 1){
					r = "("+ r +")";
				}
			}
			if(Junctible.class.isInstance(c)){
				Junctible j = Junctible.class.cast(c);
				if(i != 0 && SqlNodeUtil.isNotBlank(j.getJunction())){
					r = (j.getJunction() + " " + r);
				}
			}
			collect.add(r);
		}
		String result = collect.stream().collect(Collectors.joining(" "));
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		List<String> collect = new ArrayList<>();
		List<SqlNode<?>> children = getChildren();
		String newIndent = " " + indent;
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> c = children.get(i);
			String r = c.toSqlWith(newIndent);
			if(FilterConditions.class.isInstance(c)){
				if(children.size() > 1 && c.getChildren().size() > 1){
					r = "("+ r +")";
				}
			}
			if(Junctible.class.isInstance(c)){
				Junctible junct = Junctible.class.cast(c);
				if(i != 0 && SqlNodeUtil.isNotBlank(junct.getJunction())){
					r = (newIndent + junct.getJunction() + " " + SqlNodeUtil.trimLeading(r));
				}
			}
			collect.add(r);
		}
		String result = collect.stream().collect(Collectors.joining("\n"));
		if(SqlNodeUtil.isNotBlank(result)){
			result = indent + result;
		}
		return result;
	}
}
