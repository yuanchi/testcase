package com.jerrylin.dynasql3.modify;

import java.util.Collection;
import java.util.List;

public class SqlModify<T extends SqlModify<?>> {
	private String table;
	private List<String> columns;
	private int count;
	private int batchSize = 1000;
	private List<? extends Collection<?>> paramValues;

	public T table(String table){
		this.table = table;
		return (T)this;
	}
	public String table(){
		return table;
	}
	public T columns(List<String> columns){
		this.columns = columns;
		return (T)this;
	}
	public List<String> columns(){
		return columns;
	}
	public T count(int count){
		this.count = count;
		return (T)this;
	}
	public int count(){
		return count;
	}
	public T paramValues(List<? extends Collection<?>> paramValues){
		this.paramValues = paramValues;
		count(paramValues.size());
		return (T)this;
	}
	public List<? extends Collection<?>> paramValues(){
		return paramValues;
	}
	public T batchSize(int batchSize){
		this.batchSize = batchSize;
		return (T)this;
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
