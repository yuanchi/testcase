package com.jerrylin.dynasql3.modify;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.Pk;

public class SqlDelete implements SqlBatchable{

	private String table;
	private Pk pk;
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
	public static SqlDelete init(String table, List<String> pkColumns, int count){
		SqlDelete delete = new SqlDelete()
			.table(table)
			.pk(pkColumns)
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
	public static SqlDelete init(String table, List<String> pkColumns, List<Object>ids){
		SqlDelete delete = new SqlDelete()
			.table(table)
			.pk(pkColumns)
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
	public SqlDelete pk(String pkColumn){
		this.pk = Pk.init(pkColumn);
		return this;
	}
	public SqlDelete pk(String column1, String column2){
		this.pk = Pk.init(column1, column2);
		return this;
	}
	public SqlDelete pk(List<String> columns){
		this.pk = Pk.init(columns);
		return this;
	}
	public Pk pk(){
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
		List<String> pkColumns = pk.getCompositeColumns();
		String condition = pkColumns.stream().map(c->(c + "=?")).collect(Collectors.joining(" AND "));
		condition = "("+ condition +")";
		String conditions = Collections.nCopies(count, condition).stream().collect(Collectors.joining(" OR "));
		String delete = "DELETE FROM " + table + "\n"
				+ " WHERE "+ conditions;
		return delete;
	}
	@Override
	public List<? extends Collection<?>> paramValues() {
		List<List<Object>> paramValues = Collections.emptyList();
		if(ids.size() == 0){
			return paramValues;
		}
		Object first = ids.get(0);
		if(first instanceof List){
			paramValues = ids.stream().map(id->(List<Object>)id).collect(Collectors.toList());
		}else{
			paramValues = ids.stream().map(id->Arrays.asList(id)).collect(Collectors.toList());
		}
		return paramValues;
	}
}
