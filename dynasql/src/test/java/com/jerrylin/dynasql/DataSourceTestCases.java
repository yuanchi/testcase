package com.jerrylin.dynasql;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DataSourceTestCases {
	@Test
	public void selectCustomizedValue(){
		ComboPooledDataSource cpds = null;
		try{
			cpds = new ComboPooledDataSource();
			cpds.setDriverClass("org.mariadb.jdbc.Driver");
			cpds.setJdbcUrl("jdbc:mariadb://localhost:3306/angrycat");
			cpds.setUser("root");
			cpds.setPassword("root");
			// optional
			cpds.setInitialPoolSize(5);
			cpds.setMinPoolSize(5);
			cpds.setAcquireIncrement(5);
			cpds.setMaxPoolSize(20);
			
			try(Connection con = cpds.getConnection();){
				System.out.println(con.getAutoCommit());
				con.setAutoCommit(false);
				String sql =
						 "SELECT '1' as first, 1 as second, '2011-11-12 00:00:00' as third, true as forth";
//				System.out.println(sql);
				try(PreparedStatement pstmt = con.prepareStatement(sql);){
					try(ResultSet rs = pstmt.executeQuery();){
						while(rs.next()){
							String first = rs.getString("first");
							int second = rs.getInt("second");
							Date third = rs.getDate("third");
							boolean forth = rs.getBoolean("forth");
							System.out.println(first);
							System.out.println(second);
							System.out.println(rs.getObject("third").getClass());
							System.out.println(forth);
						}
					}
				}
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			if(cpds!=null){
				cpds.close();
			}
		}
	}
}
