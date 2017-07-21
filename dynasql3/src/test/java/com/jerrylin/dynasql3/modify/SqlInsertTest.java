package com.jerrylin.dynasql3.modify;

import static org.junit.Assert.assertEquals;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;

import com.jerrylin.dynasql3.modify.SqlModify.SqlBatchChunk;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SqlInsertTest {
	public static ComboPooledDataSource dataSource(){
		ComboPooledDataSource cpds = new ComboPooledDataSource();
		
		try {
			cpds.setDriverClass("org.mariadb.jdbc.Driver");
		} catch (PropertyVetoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //loads the jdbc driver 
		cpds.setJdbcUrl("jdbc:mariadb://localhost:3306/test"); 
		cpds.setUser("root"); 
		cpds.setPassword("root"); // the settings below are optional -- c3p0 can work with defaults 
		cpds.setMinPoolSize(5); 
		cpds.setAcquireIncrement(5); 
		cpds.setMaxPoolSize(20);
		cpds.setInitialPoolSize(5);
		return cpds;
	}
	public static void createTestTable_s1(Connection connection){
		try(Statement stmt = connection.createStatement();){
			String sql = 
				"create table if not exists s1 (\n"
				+ "id varchar(15) charset utf8 primary key,\n"
				+ "nameEng varchar(100) charset utf8\n"
				+ ") engine=InnoDB default charset utf8;";
			stmt.execute(sql);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	public static void createTestTable_s2(Connection connection, boolean deleteOld){
		if(deleteOld){
			try(Statement stmt = connection.createStatement();){
				String sql = 
					"delete from s2";
				stmt.execute(sql);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
		try(Statement stmt = connection.createStatement();){
			String sql = 
				"create table if not exists s2 (\n"
				+ "name varchar(100) NOT NULL,\n"
				+ "mobile varchar(10) NOT NULL,\n"
				+ "note varchar(100) DEFAULT NULL,\n"
				+ "PRIMARY KEY (name, mobile)\n"
				+ ") engine=InnoDB default charset=utf8;";
			stmt.execute(sql);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Test
	public void preparedInsertBatchSql(){
		SqlInsert insert = SqlInsert
			.init(
				"member", 
				5, 
				"id", "nameEng", "idNo");
		String sql = insert.preparedBatchSql();
		String expected = 
			"INSERT INTO member\n"
			+ " (id, nameEng, idNo) VALUES\n"
			+ " (?,?,?),\n"
			+ " (?,?,?),\n"
			+ " (?,?,?),\n"
			+ " (?,?,?),\n"
			+ " (?,?,?)";
		assertEquals(expected, sql);
	}
	@Test
	public void insertBatchChunks(){
		List<LinkedHashMap<String, Object>> params = new ArrayList<>();
		int insertCount = 1999;
		for(int i = 0; i < insertCount; i++){
			LinkedHashMap<String, Object> param = new LinkedHashMap<>();
			param.put("id", ""+i);
			param.put("nameEng", "n_"+i);
			params.add(param);
		}
		
		SqlInsert insert = SqlInsert.init("s1", params);
		List<SqlBatchChunk> chunks = insert.batchChunks();
		long start = System.currentTimeMillis();
		ComboPooledDataSource cpds = dataSource();
		try(Connection connection = cpds.getConnection();){
			connection.setAutoCommit(false);
			try{
				createTestTable_s1(connection);
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
		System.out.println("process insert "+ insertCount +" spent : " + (end - start) + " millisec.");
	}
	public static String mockMobile(int i){
		String refMobile = "0971221221";
		int mobileCount = refMobile.length();
		String code = i+"";
		int remaining = mobileCount - code.length();
		String mobile = refMobile.substring(0, remaining) + code;
		return mobile;
	}
	public static String fillZeroToLeft(int total, int num){
		String code = num +"";
		int remaining = total - code.length();
		String r = Collections.nCopies(remaining, "0").stream().collect(Collectors.joining()) + code;
		return r;
	}
	@Test
	public void testFillZeroToLeft(){
		String code = fillZeroToLeft(5, 1);
		assertEquals("00001", code);
		code = fillZeroToLeft(5, 18);
		assertEquals("00018", code);
	}
	@Test
	public void insertBatchChunks2(){
		List<LinkedHashMap<String, Object>> params = new ArrayList<>();
		int insertCount = 1999;

		for(int i = 0; i < insertCount; i++){
			LinkedHashMap<String, Object> param = new LinkedHashMap<>();
			String name = fillZeroToLeft(4, i);
			param.put("name", name);
			String mobile = mockMobile(i);
			param.put("mobile", mobile);
			params.add(param);
		}
		
		SqlInsert insert = SqlInsert.init("s2", params);
		List<SqlBatchChunk> chunks = insert.batchChunks();
		long start = System.currentTimeMillis();
		ComboPooledDataSource cpds = dataSource();
		try(Connection connection = cpds.getConnection();){
			connection.setAutoCommit(false);
			try{
				createTestTable_s2(connection, true);
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
		System.out.println("process insert s2 "+ insertCount +" spent : " + (end - start) + " millisec.");
	}
}
