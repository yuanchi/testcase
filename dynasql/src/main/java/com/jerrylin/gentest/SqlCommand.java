package com.jerrylin.gentest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

public class SqlCommand {
	private String table;
	private List<String> columns;
	private int count;
	public String getTable() {
		return table;
	}
	public void setTable(String table) {
		this.table = table;
	}
	public List<String> getColumns() {
		return columns;
	}
	public void setColumns(List<String> columns) {
		this.columns = columns;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public String genSql(){
		StringBuffer sb = new StringBuffer("INSERT INTO");
		sb.append(" ");
		sb.append(table);
		sb.append(" (");
		sb.append(columns.stream().collect(Collectors.joining(",")));
		sb.append(") ");
		sb.append("VALUES ");
		String inParameter = "(" + Collections.nCopies(columns.size(), "?").stream().collect(Collectors.joining(",")) + ")";
		String inParameters = Collections.nCopies(count, inParameter).stream().collect(Collectors.joining(","));
		sb.append(inParameters);
		return sb.toString();
	}
	public SqlCommand valueGroupCount(int count){
		this.count = count;
		return this;
	}
	public SqlCommand columns(String...cols){
		columns = new ArrayList<>();
		columns.addAll(Arrays.asList(cols));
		return this;
	}
	public static SqlCommand insertInto(String table){
		SqlCommand sc = new SqlCommand();
		sc.setTable(table);
		return sc;
	}
	@Test
	public void testInsertInto(){
		String sql = SqlCommand.insertInto("customer")
			.columns("name", "email", "birth")
			.valueGroupCount(5)
			.genSql();
		String expectedSql = "INSERT INTO customer (name,email,birth) VALUES (?,?,?),(?,?,?),(?,?,?),(?,?,?),(?,?,?)";
		assertEquals(expectedSql, sql);
		System.out.println(sql);
	}
}
