package com.jerrylin.dynasql3.modify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;

import org.junit.Test;

import com.jerrylin.dynasql3.modify.SqlModify.SqlBatchChunk;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import static org.junit.Assert.*;

public class SqlUpdateTest {
	@Test
	public void preparedUpdateBatchSql(){
		SqlUpdate update = SqlUpdate.init("member", "id", 5, "id", "nameEng", "idNo");
		String sql = update.preparedBatchSql();
		String expected = 
			"UPDATE member AS m, (SELECT ? AS id, ? AS nameEng, ? AS idNo UNION\n"
			+ " SELECT ?,?,? UNION\n"
			+ " SELECT ?,?,? UNION\n"
			+ " SELECT ?,?,? UNION\n"
			+ " SELECT ?,?,?) AS updates\n"
			+ "SET m.nameEng = updates.nameEng,\n"
			+ " m.idNo = updates.idNo\n"
			+ "WHERE m.id = updates.id";
		assertEquals(expected, sql);
	}
	@Test
	public void preparedUpdateBatchSql2(){
		SqlUpdate update = 
			SqlUpdate.init(
				"member", 
				Arrays.asList("name", "mobile"), 
				5, 
				"name", "mobile", "nameEng", "idNo");
		String sql = update.preparedBatchSql();
		String expected = 
			"UPDATE member AS m, (SELECT ? AS name, ? AS mobile, ? AS nameEng, ? AS idNo UNION\n"
			+ " SELECT ?,?,?,? UNION\n"
			+ " SELECT ?,?,?,? UNION\n"
			+ " SELECT ?,?,?,? UNION\n"
			+ " SELECT ?,?,?,?) AS updates\n"
			+ "SET m.nameEng = updates.nameEng,\n"
			+ " m.idNo = updates.idNo\n"
			+ "WHERE m.name = updates.name AND m.mobile = updates.mobile";
		assertEquals(expected, sql);
	}
	@Test
	public void updateBatchChunks(){
		List<LinkedHashMap<String, Object>> params = new ArrayList<>();
		int updateCount = 1999;
		for(int i = 0; i < updateCount; i++){
			LinkedHashMap<String, Object> param = new LinkedHashMap<>();
			param.put("id", i+"");
			param.put("nameEng", "n_"+i+"_r1");
			params.add(param);
		}
		
		SqlUpdate update = SqlUpdate.init("s1", "id", params);
		List<SqlBatchChunk> chunks = update.batchChunks();
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
		// process update 1999 spent : 766 millisec. (batchSize = 1000)
		System.out.println("process update "+ updateCount +" spent : " + (end - start) + " millisec.");
	}
	@Test
	public void updateBatchChunks2(){
		List<LinkedHashMap<String, Object>> params = new ArrayList<>();
		int updateCount = 3;
		for(int i = 0; i < updateCount; i++){
			LinkedHashMap<String, Object> param = new LinkedHashMap<>();
			String mobile = SqlInsertTest.mockMobile(i);
			String name = SqlInsertTest.fillZeroToLeft(4, i);
			param.put("name", name);
			param.put("mobile", mobile);
			param.put("note", i);
			params.add(param);
		}
		
		SqlUpdate update = SqlUpdate.init("s2", Arrays.asList("name", "mobile"), params);
		List<SqlBatchChunk> chunks = update.batchChunks();
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
		// process update 1999 spent : 766 millisec. (batchSize = 1000)
		System.out.println("process update s2 "+ updateCount +" spent : " + (end - start) + " millisec.");
	}
}
