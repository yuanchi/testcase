package com.jerrylin.dynasql3.modify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class SqlInsert extends SqlModify<SqlInsert> implements SqlBatchable{
	public static SqlInsert init(String table, int count, String... columns){
		SqlInsert sqlInsert = new SqlInsert();
		sqlInsert.table(table)
			.columns(Arrays.asList(columns))
			.count(count);
		return sqlInsert;
	}
	public static SqlInsert init(String table, List<? extends Collection<?>> paramValues, String... columns){
		SqlInsert sqlInsert = new SqlInsert();
		sqlInsert.table(table)
			.columns(Arrays.asList(columns))
			.paramValues(paramValues);
		return sqlInsert;
	}
	public static SqlInsert init(String table, List<LinkedHashMap<String, Object>> params){
		List<? extends Collection<?>> paramValues = params.stream().map(p->p.values()).collect(Collectors.toList());
		Set<String> cols = params.iterator().next().keySet();
		SqlInsert sqlInsert = init(table, paramValues, cols.toArray(new String[cols.size()]));
		return sqlInsert;
	}
	public String preparedBatchSql(int count){
		int columnCount = columns().size();
		String placeholders = 
			"("
			+ Collections.nCopies(columnCount, "?").stream().collect(Collectors.joining(","))
			+ ")";
		String placeSets = Collections.nCopies(count, placeholders).stream().collect(Collectors.joining(",\n "));
		String columns = columns().stream().collect(Collectors.joining(", "));
		String insert = "INSERT INTO " + table() + "\n"
				+ " ("+ columns +") VALUES\n"
				+ " " + placeSets;
		return insert;
	}
}
