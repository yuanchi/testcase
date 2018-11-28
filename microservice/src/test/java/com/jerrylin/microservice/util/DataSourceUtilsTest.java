package com.jerrylin.microservice.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.zaxxer.hikari.HikariDataSource;

import static org.junit.Assert.assertEquals;

public class DataSourceUtilsTest {
	@Test
	public void showMeta(){
		HikariDataSource ds = DataSourceUtils.ds();
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			try{
				List<String> tables = new ArrayList<>();
				try(Statement stmt = con.createStatement();
					ResultSet rs = stmt.executeQuery("show tables");){
					ResultSetMetaData rsmd = rs.getMetaData();
					int columnCount = rsmd.getColumnCount();
					while(rs.next()){
						for(int i = 1; i <= columnCount; i++){
							String table = rs.getString(i);
							tables.add(table);
						}
					}
				}
				for(String table : tables){
					try(Statement stmt = con.createStatement();
						ResultSet rs = stmt.executeQuery("show create table " + table);){
						ResultSetMetaData rsmd = rs.getMetaData();
						int columnCount = rsmd.getColumnCount();
						List<String> columnNames = new ArrayList<>();
						for(int i = 1; i <= columnCount; i++){
							columnNames.add(rsmd.getColumnLabel(i));
						}
						while(rs.next()){
							for(String columnName : columnNames){
								Object val = rs.getObject(columnName);
								System.out.println(columnName + ":" + val);
							}
						}
					}
				}
				System.out.println(tables.size() + " tables:");
				con.commit();
			}catch(Throwable e){
				con.rollback();
				throw new RuntimeException(e);
			}finally{
				con.setAutoCommit(true);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			ds.close();
		}
	}
	
	private String breakline(String...ss ){
		StringBuilder sb = new StringBuilder();
		for(String s : ss){
			sb.append(s);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	@Test
	public void testBreakline(){
		String r = breakline(
				"SELECT sd1.*",
				"FROM shr_defaultserial sd1",
				"RIGHT JOIN (SELECT DISTINCT sd.id",
				"  FROM shr_defaultserial sd",
				"  WHERE 1 = 1",
				"  ORDER BY sd.id DESC",
				"  LIMIT 10",
				"  OFFSET 0) sd2",
				"  ON sd1.id = sd2.id;",
				"",
				"SELECT COUNT(DISTINCT id) AS idCount",
				"FROM shr_defaultserial",
				"WHERE 1 = 1",
				"LIMIT 10",
				"OFFSET 0;");
		
		String expected = 
				"SELECT sd1.*\n"
			  + "FROM shr_defaultserial sd1\n"
			  + "RIGHT JOIN (SELECT DISTINCT sd.id\n"
			  + "  FROM shr_defaultserial sd\n"
			  + "  WHERE 1 = 1\n"
			  + "  ORDER BY sd.id DESC\n"
			  + "  LIMIT 10\n"
			  + "  OFFSET 0) sd2\n"
			  + "  ON sd1.id = sd2.id;\n"
			  + "\n"	
		      + "SELECT COUNT(DISTINCT id) AS idCount\n"
			  + "FROM shr_defaultserial\n"
			  + "WHERE 1 = 1\n"
			  + "LIMIT 10\n"
		      + "OFFSET 0;\n ";
		assertEquals("", r, expected);
		
		System.out.println(r);
	}
	
	@Test
	public void multiResultSets(){
		HikariDataSource ds = DataSourceUtils.ds();
		List<Map<String, Object>> collects = new LinkedList<>();
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			try{
				String create =
					breakline(
						"CREATE TABLE IF NOT EXISTS shr_defaultserial (",
						"  id varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NOT NULL,",
						"  sep0 varchar(20) DEFAULT NULL,",
						"  dateSep0 varchar(10) DEFAULT NULL,",
						"  sep1 varchar(20) DEFAULT NULL,",
						"  dateSep1 varchar(10) DEFAULT NULL,",
						"  sep2 varchar(20) DEFAULT NULL,",
						"  dateSep2 varchar(10) DEFAULT NULL,",
						"  sep3 varchar(20) DEFAULT NULL,",
						"  no varchar(20) DEFAULT NULL,",
						"  sep4 varchar(20) DEFAULT NULL,",
						"  note varchar(300) DEFAULT NULL,",
						"  resetNoField varchar(20) DEFAULT NULL,",
						"  resetNoFieldLV varchar(100) DEFAULT NULL,",
						"  resetNoTo int(10) unsigned NOT NULL DEFAULT '1',",
						"  PRIMARY KEY (id)",
						") ENGINE=InnoDB DEFAULT CHARSET=utf8;"
					);
				
				String selects =
					breakline(
						"SELECT sd1.*",
						"FROM shr_defaultserial sd1",
						"WHERE 1 = 1", // cond1						
						"LIMIT 10", // cond2
						"OFFSET 0", // cond3
						"ORDER BY sd1.id DESC",
						"",
						"SELECT COUNT(DISTINCT id) AS totalCount",
						"FROM shr_defaultserial",
						"WHERE 1 = 1"); // cond1
				
				String sql = create + selects;
				// ref. https://stackoverflow.com/questions/9696572/queries-returning-multiple-result-sets
				// execute()和getMoreResults()回傳false代表結果是數字，不是ResultSet
				// stmt.getUpdateCount() == -1代表沒有更多ResultSet
				try(Statement stmt = con.createStatement();){
					boolean isResultSet = stmt.execute(sql);
					int count = 0;
					while(true){
						if(isResultSet){
							try(ResultSet rs = stmt.getResultSet()){
								ResultSetMetaData rsmd = rs.getMetaData();
								int colCount = rsmd.getColumnCount();
								List<String> columnNames = new ArrayList<>();
								for(int i = 1; i <= colCount; i++){
									columnNames.add(rsmd.getColumnLabel(i));
//									System.out.println(rsmd.getColumnLabel(i));
								}
								
								Map<String, Object> collect = new LinkedHashMap<>();
								collects.add(collect);
								
								while(rs.next()){
									for(String columnName : columnNames){
										Object val = rs.getObject(columnName);
										collect.put(columnName, val);
									}
								}
							}
						}else{
							if(stmt.getUpdateCount() == -1){
								break;
							}
							System.out.println("Result " + count + " is just a count " + stmt.getUpdateCount());
						}
						count++;
						isResultSet = stmt.getMoreResults();
					}
				}
				con.commit();
			}catch(Throwable e){
				con.rollback();
				throw new RuntimeException("xxxx" + e);
			}finally{
				con.setAutoCommit(true);
			}
		}catch(Throwable e){
			throw new RuntimeException("yyy" + e);
		}finally{
			ds.close();
		}
		
		for(Map<String, Object> collect : collects){
			System.out.println(collect);
		}
	}
}
