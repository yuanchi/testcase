package com.jerrylin.dynasql3.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import com.jerrylin.dynasql3.modify.SqlModify.SqlBatchChunk;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import static org.junit.Assert.*;

public class SqlDeleteTest {
	@Test
	public void preparedBatchSql(){
		SqlDelete delete = SqlDelete.init("member", "id", 10);
		String sql = delete.preparedBatchSql();
		String expected = 
			"DELETE FROM member\n"
			+ " WHERE id=? OR id=? OR id=? OR id=? OR id=? OR id=? OR id=? OR id=? OR id=? OR id=?";
		assertEquals(expected, sql);
	}
	@Test
	public void preparedBatchSql2(){
		SqlDelete delete = SqlDelete.init("member", Arrays.asList("name", "mobile"), 3);
		String sql = delete.preparedBatchSql();
		String expected = 
			"DELETE FROM member\n"
			+ " WHERE (name=? AND mobile=?) OR (name=? AND mobile=?) OR (name=? AND mobile=?)";
		assertEquals(expected, sql);
	}
	@Test
	public void executeBatchDelete(){
		List<Object> ids = new ArrayList<>();
		int deleteCount = 1999;
		for(int i = 0; i < deleteCount; i++){
			ids.add(i+"");
		}
		
		SqlDelete delete = SqlDelete.init("s1", "id", ids);
		List<SqlBatchChunk> chunks = delete.batchChunks();
		long start = System.currentTimeMillis();
		ComboPooledDataSource cpds = SqlInsertTest.dataSource();
		try(Connection connection = cpds.getConnection();){
			connection.setAutoCommit(false);
			try{
				for(SqlBatchChunk chunk : chunks){
					String sql = chunk.getSql();
					List<? extends Collection<?>> paramValues = chunk.getParamValues();
					int count = 0;
					try(PreparedStatement psmt = connection.prepareStatement(sql);){
						for(Collection<?> p : paramValues){
							for(Object v : p){
								psmt.setObject(++count, v);
							}
						}
						psmt.executeUpdate();
					}
				}
				connection.commit();
			}catch(Throwable e){
				connection.rollback();
				throw new RuntimeException(e);
			}finally{
				connection.setAutoCommit(true);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			if(cpds != null){
				cpds.close();
			}
		}
		long end = System.currentTimeMillis();
		// process insert 1999 spent : 671 millisec. (including create table)
		System.out.println("process delete "+ deleteCount +" spent : " + (end - start) + " millisec.");
	}
	@Test
	public void executeBatchDelete2(){
		List<Object> ids = new ArrayList<>();
		int deleteCount = 1999;
		for(int i = 0; i < deleteCount; i++){
			String name = SqlInsertTest.fillZeroToLeft(4, i);
			String mobile = SqlInsertTest.mockMobile(i);
			ids.add(Arrays.asList(name, mobile));
		}
		
		SqlDelete delete = SqlDelete.init("s2", Arrays.asList("name", "mobile"), ids);
		List<SqlBatchChunk> chunks = delete.batchChunks();
		long start = System.currentTimeMillis();
		ComboPooledDataSource cpds = SqlInsertTest.dataSource();
		try(Connection connection = cpds.getConnection();){
			connection.setAutoCommit(false);
			try{
				for(SqlBatchChunk chunk : chunks){
					String sql = chunk.getSql();
					List<? extends Collection<?>> paramValues = chunk.getParamValues();
					int count = 0;
					try(PreparedStatement psmt = connection.prepareStatement(sql);){
						for(Collection<?> p : paramValues){
							for(Object v : p){
								psmt.setObject(++count, v);
							}
						}
						psmt.executeUpdate();
					}
				}
				connection.commit();
			}catch(Throwable e){
				connection.rollback();
				throw new RuntimeException(e);
			}finally{
				connection.setAutoCommit(true);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			if(cpds != null){
				cpds.close();
			}
		}
		long end = System.currentTimeMillis();
		// process insert 1999 spent : 671 millisec. (including create table)
		System.out.println("process delete "+ deleteCount +" spent : " + (end - start) + " millisec.");
	}
}
