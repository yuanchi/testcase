package com.jerrylin.query;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class SqlGenerator {
	public static final String DEFAULT_PK = "id";
	
	private String alias;
	private String pk;
	private String id;
	private String select;
	private String from;
	private String where;
	private String orderBy;
	private SelectExpression se;
	
	public SqlGenerator(SelectExpression se){
		SelectExpression copy = se.copy();
		
		Select selectNode = copy.findChildExact(Select.class);
		From fromNode = copy.findChildExact(From.class);
		Where whereNode = copy.findChildExact(Where.class);
		OrderBy orderByNode = copy.findChildExact(OrderBy.class);
		
		this.from = genSqlDefaultEmpty(fromNode);
		
		SimpleExpression t = fromNode.findChildExact(SimpleExpression.class);
		this.alias = t.attr(SimpleExpression.ALIAS);
		this.pk = t.attr(From.PK) != null ? t.attr(From.PK) : DEFAULT_PK;
		this.id = alias + "." + pk;
		
		this.select = genSqlDefaultEmpty(selectNode);
		this.where = genSqlDefaultEmpty(whereNode);
		this.orderBy = orderByDefault(orderByNode);
		
		this.se = copy;
	}
	private String genSqlDefaultEmpty(SqlNode node){
		if(node == null){
			return "";
		}
		return node.genSql();
	}
	public String orderByDefault(OrderBy orderBy){
		String sql = genSqlDefaultEmpty(orderBy);
		String dir = "DESC";
		if(StringUtils.isBlank(sql)){
			sql = "ORDER BY " + id + " " + dir;  
		}
		if(!sql.contains(id + " ")){
			sql += ", " + id + " " + dir;
		}
		return sql;
	}	
	public static String addLineBreakIfNotBlank(String...statements){
		List<String> all = Arrays.asList(statements);
		List<String> filtered = all.stream().filter(s->StringUtils.isNotBlank(s)).collect(Collectors.toList());
		String result = StringUtils.join(filtered, "\n");
		return result;
	}
	public String getAlias() {
		return alias;
	}
	public String getPk() {
		return pk;
	}
	public String getId() {
		return id;
	}
	public String getSelect() {
		return select;
	}
	public String getFrom() {
		return from;
	}
	public String getWhere() {
		return where;
	}
	public String getOrderBy() {
		return orderBy;
	}
	public SelectExpression getSe() {
		return se;
	}
	public Map<String, Object> idParams(){
		return se.conditionIdValuePairs();
	}
	public Map<Integer, Object> indexedParams(){
		return se.conditionIndexValuePairs();
	}
	public String sql(){
		return addLineBreakIfNotBlank(select, from, where, orderBy);
	}
	public String selectDistinct(String target){
		String newSelect = "SELECT DISTINCT " + target; 
		return addLineBreakIfNotBlank(newSelect, from, where, orderBy);
	}
	public String selectDistinctAlias(){
		return selectDistinct(alias);
	}
	public String selectDistinctId(){
		return selectDistinct(id);
	}
}
