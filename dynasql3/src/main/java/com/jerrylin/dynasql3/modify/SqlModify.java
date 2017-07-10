package com.jerrylin.dynasql3.modify;

import java.util.Collection;
import java.util.List;

public class SqlModify {
	private String table;
	private List<String> columns;
	private int count;
	private int batchSize = 1000;
	private List<? extends Collection<?>> paramValues;

	public SqlModify table(String table){
		this.table = table;
		return this;
	}
	public String table(){
		return table;
	}
	public SqlModify columns(List<String> columns){
		this.columns = columns;
		return this;
	}
	public List<String> columns(){
		return columns;
	}
	public SqlModify count(int count){
		this.count = count;
		return this;
	}
	public int count(){
		return count;
	}
	public SqlModify paramValues(List<? extends Collection<?>> paramValues){
		this.paramValues = paramValues;
		return this;
	}
	public List<? extends Collection<?>> paramValues(){
		return paramValues;
	}
	public SqlModify batchSize(int batchSize){
		this.batchSize = batchSize;
		return this;
	}
	public int batchSize(){
		return batchSize;
	}
	
	public static class SqlBatchChunk{
		String sql;
		List<? extends Collection<?>> paramValues;
		public String getSql() {
			return sql;
		}
		public List<? extends Collection<?>> getParamValues() {
			return paramValues;
		}
	}
}
