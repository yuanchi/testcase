package com.jerrylin.dynasql3;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.node.On;
import com.jerrylin.dynasql3.node.SqlNode;

public interface Joinable<Me extends SqlNode<?>> extends ChildAddible<Me>{
	public static final String TYPE_JOIN = "JOIN";
	public static final String TYPE_INNER_JOIN = "INNER JOIN";
	public static final String TYPE_LEFT_OUTER_JOIN = "LEFT OUTER JOIN";
	public static final String TYPE_RIGHT_OUTER_JOIN = "RIGHT OUTER JOIN";
	public static final String TYPE_CROSS_JOIN = "CROSS JOIN";
	
	public String getJoinType();
	public Me setJoinType(String joinType);
	public LinkedList<SqlNode<?>> getChildren();
	
	default On on(){
		On on = createBy(On.class);
		add(on);
		return on;
	}
	default Me on(String expression){
		On on = on();
		on.and(expression);
		return (Me)this;
	}
	default Me on(List<String> expressions){
		On on = on();
		for(String expression : expressions){
			on.and(expression);
		}
		return (Me)this;
	}
	default Me on(Consumer<On>consumer){
		On on = on();
		on.config(consumer);
		return (Me)this;
	}
	default String toSql(){
		String sql = getChildren().stream().map(c->c.toSql()).collect(Collectors.joining(" "));
		return sql;
	}
	default String toSqlWith(String indent){
		String sql = getChildren().stream().map(c->c.toSqlWith(indent)).collect(Collectors.joining("\n"));
		return sql;
	}
	default Set<String> getOnReferences(){
		Set<String> refs = Collections.emptySet();
		On on = null;
		for(SqlNode<?> c : getChildren()){
			if(On.class.isInstance(c)){
				on = On.class.cast(c);
				break;
			}
		}
		if(on == null){
			return refs;
		}
		refs = new LinkedHashSet<>();
		for(SqlNode<?> c : on.getChildren()){
			if(Expressible.class.isInstance(c)){
				refs.addAll(Expressible.class.cast(c).getTableReferences());
			}
		}
		return refs;
	}
}
