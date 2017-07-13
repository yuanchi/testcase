package com.jerrylin.dynasql3.modify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.modify.SqlModify.SqlBatchChunk;

public class SqlDelete implements SqlBatchable{

	private String table;
	private String pk;
	private int count;
	private List<Object> ids;
	private int batchSize = 1000;
	public static SqlDelete init(String table, String pk, int count){
		SqlDelete delete = new SqlDelete()
			.table(table)
			.pk(pk)
			.count(count);
		return delete;
	}
	public static SqlDelete init(String table, String pk, List<Object>ids){
		SqlDelete delete = new SqlDelete()
			.table(table)
			.pk(pk)
			.ids(ids);
		return delete;
	}
	public SqlDelete table(String table){
		this.table = table;
		return this;
	}
	public String table(){
		return this.table;
	}
	public SqlDelete pk(String pk){
		this.pk = pk;
		return this;
	}
	public String pk(){
		return this.pk;
	}
	public SqlDelete count(int count){
		this.count = count;
		return this;
	}
	public int count(){
		return this.count;
	}
	public SqlDelete ids(List<Object>ids){
		this.ids = ids;
		count(ids.size());
		return this;
	}
	public List<Object> ids(){
		return this.ids;
	}
	public SqlDelete batchSize(int batchSize){
		this.batchSize = batchSize;
		return this;
	}
	public int batchSize(){
		return this.batchSize;
	}
	public String preparedBatchSql(int count){
		String condition = pk + "=?";
		String conditions = Collections.nCopies(count, condition).stream().collect(Collectors.joining(" OR "));
		String delete = "DELETE FROM " + table + "\n"
				+ " WHERE "+ conditions;
		return delete;
	}
	@Override
	public List<? extends Collection<?>> paramValues() {
		List<List<Object>> paramValues = ids.stream().map(id->Arrays.asList(id)).collect(Collectors.toList());
		return paramValues;
	}
}
