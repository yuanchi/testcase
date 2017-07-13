package com.jerrylin.dynasql3.modify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlUpdate extends SqlModify implements SqlBatchable{
	private String pk;
	public SqlUpdate pk(String pk){
		this.pk = pk;
		return this;
	}
	public String pk(){
		return pk;
	}
	public static SqlUpdate init(String table, String pk, int count, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pk)
			.table(table)
			.columns(Arrays.asList(columns))
			.count(count);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, String pk, List<? extends Collection<?>> paramValues, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pk)
			.table(table)
			.columns(Arrays.asList(columns))
			.paramValues(paramValues);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, String pk, List<LinkedHashMap<String, Object>> params){
		List<? extends Collection<?>> paramValues = params.stream().map(p->p.values()).collect(Collectors.toList());
		Set<String> cols = params.iterator().next().keySet();
		SqlUpdate sqlUpdate = init(table, pk, paramValues, cols.toArray(new String[cols.size()]));
		return sqlUpdate;
	}
	/**
	 * this method has be tested via MariaDB 10.0
	 * @param count
	 * @return
	 */
	public String preparedBatchSql(int count){
		List<String> columns = columns();
		String table = table();
		int columnCount = columns.size();
		String alias = table.substring(0, 1);
		String updateAlias = "updates";
		String updateTmp = "SELECT " + columns.stream().map(c-> "? AS " + c).collect(Collectors.joining(", "));
		if(count == 1){
			updateTmp = "(" + updateTmp +") AS " + updateAlias;
		}
		if(count > 1){
			String placeholders = 
				"SELECT " + Collections.nCopies(columnCount, "?").stream().collect(Collectors.joining(","));
			String placeSets = Collections.nCopies(count-1, placeholders).stream().collect(Collectors.joining(" UNION\n "));
			updateTmp = "(" + updateTmp + " UNION\n " + placeSets +") AS " + updateAlias;
		}
		String setters = columns.stream().filter(c->!c.equals(pk)).map(c->alias+"."+c+" = updates."+c).collect(Collectors.joining(",\n "));
		String condition = alias + "." + pk + " = " + updateAlias + "." + pk;
		String sql = "UPDATE " + table + " AS " + alias + ", " + updateTmp + "\n"
				+ "SET " + setters + "\n"
				+ "WHERE " + condition;
		return sql; 
	}
}
