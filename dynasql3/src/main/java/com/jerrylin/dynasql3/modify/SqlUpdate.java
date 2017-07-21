package com.jerrylin.dynasql3.modify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.Pk;

@SuppressWarnings("unchecked")
public class SqlUpdate extends SqlModify<SqlUpdate> implements SqlBatchable{
	private Pk pk;
	public SqlUpdate pk(String pkColumn){
		Pk uniKey = Pk.init(pkColumn);
		this.pk = uniKey;
		return this;
	}
	public SqlUpdate pk(String column1, String column2){
		Pk uniKey = Pk.init(column1, column2);
		this.pk = uniKey;
		return this;
	}
	public SqlUpdate pk(List<String> columns){
		Pk uniKey = Pk.init(columns);
		this.pk = uniKey;
		return this;
	}
	public Pk pk(){
		return pk;
	}
	public static SqlUpdate init(String table, String pkColumn, int count, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pkColumn)
			.table(table)
			.columns(Arrays.asList(columns))
			.count(count);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, List<String> pkColumns, int count, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pkColumns)
			.table(table)
			.columns(Arrays.asList(columns))
			.count(count);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, String pkColumn, List<? extends Collection<?>> paramValues, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pkColumn)
			.table(table)
			.columns(Arrays.asList(columns))
			.paramValues(paramValues);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, List<String> pkColumns, List<? extends Collection<?>> paramValues, String... columns){
		SqlUpdate sqlUpdate = new SqlUpdate();
		sqlUpdate.pk(pkColumns)
			.table(table)
			.columns(Arrays.asList(columns))
			.paramValues(paramValues);
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, String pkColumn, List<LinkedHashMap<String, Object>> params){
		List<? extends Collection<?>> paramValues = params.stream().map(p->p.values()).collect(Collectors.toList());
		Set<String> cols = params.iterator().next().keySet();
		SqlUpdate sqlUpdate = init(table, pkColumn, paramValues, cols.toArray(new String[cols.size()]));
		return sqlUpdate;
	}
	public static SqlUpdate init(String table, List<String> pkColumns, List<LinkedHashMap<String, Object>> params){
		List<? extends Collection<?>> paramValues = params.stream().map(p->p.values()).collect(Collectors.toList());
		Set<String> cols = params.iterator().next().keySet();
		SqlUpdate sqlUpdate = init(table, pkColumns, paramValues, cols.toArray(new String[cols.size()]));
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
		List<String> pkColumns = pk.getCompositeColumns();
		String setters = columns.stream().filter(c->!pkColumns.contains(c)).map(c->alias+"."+c+" = updates."+c).collect(Collectors.joining(",\n "));
		String condition = pkColumns.stream().map(c->(alias + "." + c + " = " + updateAlias + "." + c)).collect(Collectors.joining(" AND "));
		String sql = "UPDATE " + table + " AS " + alias + ", " + updateTmp + "\n"
				+ "SET " + setters + "\n"
				+ "WHERE " + condition;
		return sql; 
	}
}
