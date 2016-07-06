package com.jerrylin.query;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class From extends TargetSelectable {
	static final String PK = "pk";
	public From(){
		super(Expression.FROM_TARGET_SELECTABLE);
	}
	@Override
	public From t(String expression, String alias){
		return (From)super.t(expression, alias);
	}
	@Override
	public From t(String expression){
		return (From)super.t(expression);
	}
	public From join(String desc){
		Join join = new Join();
		join.setDesc(desc);
		addChildren(join);
		return this;
	}
	public From innerJoin(){
		return join("INNER JOIN");
	}
	public From leftOuterJoin(){
		return join("LEFT OUTER JOIN");
	}
	public From rightOuterJoin(){
		return join("RIGHT OUTER JOIN");
	}
	public From crossJoin(){
		return join("CROSS JOIN");
	}
	public From on(String desc){
		JoinCondition jc = new JoinCondition();
		jc.setDesc(desc);
		addChildren(jc);
		return this;
	}
	public From pk(String pk){
		List<SqlNode> results = getChildren().stream().filter(sn-> sn instanceof SimpleExpression).collect(Collectors.toList());
		if(!results.isEmpty()){
			results.get(results.size()-1).attr(PK, pk);
		}
		return this;
	}
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->{
			String sql = sn.genSql();
			if(!sql.contains("JOIN")){
				return sql;
			}
			return "\n" + baseIndent + sql;
		}).toArray();
		if(map != null && map.length > 0){
			String from = "FROM " + StringUtils.join(map, " ");
			return from;
		}
		return "";
	}
	@Override
	public From newInstance(){
		return new From();
	}
}
