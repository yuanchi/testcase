package com.jerrylin.dynasql3.node;

import static com.jerrylin.dynasql3.Joinable.TYPE_CROSS_JOIN;
import static com.jerrylin.dynasql3.Joinable.TYPE_INNER_JOIN;
import static com.jerrylin.dynasql3.Joinable.TYPE_JOIN;
import static com.jerrylin.dynasql3.Joinable.TYPE_LEFT_OUTER_JOIN;
import static com.jerrylin.dynasql3.Joinable.TYPE_RIGHT_OUTER_JOIN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.ChildExpressible;
import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.Joinable;
import com.jerrylin.dynasql3.exception.JoinableNotImplementedException;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class From extends SqlNode<From> implements ChildExpressible<From>,
ChildSubquerible<From> {
	private static final long serialVersionUID = 8967310701432397090L;
	
	public From addJoinExpression(String joinType, String target){
		JoinExpression je = createBy(JoinExpression.class);
		je.setJoinType(joinType);
		je.setExpression(target);
		add(je);
		return thisType();
	}
	private From addJoinSubquery(String joinType, Consumer<SelectExpression<?>> consumer){
		JoinSubquery js = createBy(JoinSubquery.class);
		js.setJoinType(joinType);
		js.subquery(consumer);
		add(js);
		return thisType();
	}
	
	public From join(String target){
		return addJoinExpression(TYPE_JOIN, target);
	}
	public From join(Consumer<SelectExpression<?>> consumer){
		return addJoinSubquery(TYPE_JOIN, consumer);
	}
	
	public From innerJoin(String target){
		return addJoinExpression(TYPE_INNER_JOIN, target);
	}
	public From innerJoin(Consumer<SelectExpression<?>> consumer){
		return addJoinSubquery(TYPE_INNER_JOIN, consumer);
	}
	
	public From leftOuterJoin(String target){
		return addJoinExpression(TYPE_LEFT_OUTER_JOIN, target);
	}
	public From leftOuterJoin(Consumer<SelectExpression<?>> consumer){
		return addJoinSubquery(TYPE_LEFT_OUTER_JOIN, consumer);
	}
	
	public From rightOuterJoin(String target){
		return addJoinExpression(TYPE_RIGHT_OUTER_JOIN, target);
	}
	public From rightOuterJoin(Consumer<SelectExpression<?>> consumer){
		return addJoinSubquery(TYPE_RIGHT_OUTER_JOIN, consumer);
	}
	
	public From crossJoin(String target){
		return addJoinExpression(TYPE_CROSS_JOIN, target);
	}
	public From crossJoin(Consumer<SelectExpression<?>> consumer){
		return addJoinSubquery(TYPE_CROSS_JOIN, consumer);
	}
	
	private Joinable<?> lastChildAsJoinable(){
		SqlNode<?> child = getChildren().getLast();
		if(!Joinable.class.isInstance(child)){
			throw new JoinableNotImplementedException("last child must implement Joinable");
		}
		Joinable<?> joinable = Joinable.class.cast(child);
		return joinable;
	}
	public From on(String...expressions){
		Joinable<?> joinable = lastChildAsJoinable();
		joinable.on(Arrays.asList(expressions));
		return thisType();
	}
	public From on(Consumer<On> consumer){
		Joinable<?> joinable = lastChildAsJoinable();
		joinable.on(consumer);
		return thisType();
	}
	public From as(String alias){
		SqlNode<?> last = getChildren().getLast();
		if(JoinSubquery.class.isInstance(last)){
			JoinSubquery.class.cast(last).as(alias);
		}else{
			ChildSubquerible.super.as(alias);
		}
		return thisType();
	}
	@Override
	public String toSql(){
		List<SqlNode<?>> children = getChildren();
		boolean joinFound = children.stream().anyMatch(c->Joinable.class.isInstance(c));
		String result = "";
		if(joinFound){
			List<String> collect = new ArrayList<>();
			for(int i = 0; i < children.size(); i++){
				SqlNode<?> c = children.get(i);
				String r = c.toSql();
				if(i != 0 && Joinable.class.isInstance(c)){
					Joinable<?> j = Joinable.class.cast(c);
					r = j.getJoinType() + " " + r;
				}
				collect.add(r);
			}
			result = collect.stream().collect(Collectors.joining("\n"));
		}else{
			result = ChildExpressible.super.toSql();
		}
		if(SqlNodeUtil.isNotBlank(result)){
			result = "FROM " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		List<SqlNode<?>> children = getChildren();
		boolean joinFound = children.stream().anyMatch(c->Joinable.class.isInstance(c));
		String result = "";
		String newIndent = " " + indent;
		if(joinFound){
			List<String> collect = new ArrayList<>();
			for(int i = 0; i < children.size(); i++){
				SqlNode<?> c = children.get(i);
				String r = c.toSqlWith(newIndent);
				if(i != 0 && Joinable.class.isInstance(c)){
					Joinable<?> j = Joinable.class.cast(c);
					r = newIndent + j.getJoinType() + " " + r;
				}else{
					r = SqlNodeUtil.trimLeading(r);
				}
				collect.add(r);
			}
			result = collect.stream().collect(Collectors.joining("\n"));
		}else{
			result = ChildExpressible.super.toSqlWith(newIndent);
		}
		if(SqlNodeUtil.isNotBlank(result)){
			String preIndex = getParent() != null ? indent : "";
			result = preIndex + "FROM " + result;
		}
		return result;
	}
}
