package com.jerrylin.dynasql3.modify;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.jerrylin.dynasql3.modify.SqlModify.SqlBatchChunk;

public interface SqlBatchable {
	public List<? extends Collection<?>> paramValues();
	public int batchSize();
	public String preparedBatchSql(int count);
	public int count();
	default String preparedBatchSql(){
		String sql = preparedBatchSql(count());
		return sql;
	}
	default List<SqlBatchChunk> batchChunks(){

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
