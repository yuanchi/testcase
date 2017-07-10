package com.jerrylin.dynasql3.modify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SqlInsert extends SqlModify {
	public static SqlInsert init(String table, int count, String... columns){
		SqlInsert sqlInsert = new SqlInsert();
		sqlInsert.table(table)
			.columns(Arrays.asList(columns))
			.count(count);
		return sqlInsert;
	}
	public static SqlInsert init(String table, List<? extends Collection<?>> paramValues, String... columns){
		SqlInsert sqlInsert = init(table, paramValues.size(), (String[])columns);
		sqlInsert.paramValues(paramValues);
		return sqlInsert;
	}
	public static SqlInsert init(String table, List<LinkedHashMap<String, Object>> params){
		List<? extends Collection<?>> paramValues = params.stream().map(p->p.values()).collect(Collectors.toList());
		Set<String> cols = params.iterator().next().keySet();
		SqlInsert sqlInsert = init(table, paramValues, cols.toArray(new String[cols.size()]));
		return sqlInsert;
	}
	public String preparedBatchSql(){
		String insert = preparedBatchSql(count());
		return insert;
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
	public List<SqlBatchChunk> batchChunks(){
		List<SqlBatchChunk> chunks = Collections.emptyList();
		List<? extends Collection<?>> paramValues = paramValues();
		int count = count();
		if(paramValues == null || count == 0){
			return chunks;
		}
		chunks = new ArrayList<>();
		int batchSize = batchSize();
		if(count < batchSize){
			SqlBatchChunk sbc = new SqlBatchChunk();
			String sql = preparedBatchSql();
			sbc.sql = sql;
			sbc.paramValues = paramValues;
			chunks.add(sbc);
		}else{
			int quotient = count / batchSize;
			int remainder = count % batchSize;
			String sql = preparedBatchSql(batchSize);
			int start = 0;
			for(int i = 0; i < quotient; i++){
				int end = start + batchSize;
				List<? extends Collection<?>> params = paramValues.subList(start, end);
				
				SqlBatchChunk sbc = new SqlBatchChunk();
				sbc.sql = sql;
				sbc.paramValues = params;
				chunks.add(sbc);
				
				start = end;
			}
			if(remainder != 0){
				SqlBatchChunk sbc = new SqlBatchChunk();
				sbc.sql = preparedBatchSql(remainder);
				sbc.paramValues = paramValues.subList(start, start + remainder);
				chunks.add(sbc);
			}
		}
		return chunks;
	}
}
