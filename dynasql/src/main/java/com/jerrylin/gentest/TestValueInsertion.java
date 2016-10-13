package com.jerrylin.gentest;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import org.junit.Test;

public class TestValueInsertion {
	private String driver = "org.mariadb.jdbc.Driver";
	private String url = "jdbc:mariadb://localhost:3306/generatedata";
	private String user = "root";
	private String pwd = "root";
	
	private int total = 100;
	private int batchSize = 20;
	
	public String getDriver() {
		return driver;
	}
	public void setDriver(String driver) {
		this.driver = driver;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public int getTotal() {
		return total;
	}
	public void setTotal(int total) {
		this.total = total;
	}
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	/**
	 * create table customer if not existed<br>
	 * then insert test values
	 */
	// ref. http://viralpatel.net/blogs/batch-insert-in-java-jdbc/
	// ref. http://www.java-tips.org/other-api-tips-100035/69-jdbc/274-how-to-exceute-a-batch-process-from-preparedstatement.html
	// ref. http://stackoverflow.com/questions/4355046/java-insert-multiple-rows-into-mysql-with-preparedstatement
	public void execute(){
		String createTable = "CREATE TABLE customer (id INTEGER NOT NULL AUTO_INCREMENT, name VARCHAR(255), email VARCHAR(255), birth DATETIME, PRIMARY KEY (id))";
		String insert = "INSERT INTO customer (name, email, birth) VALUES(?,?,?)";
		List<Customer> customers = Customer.mockCustomers(total);
		long start = System.currentTimeMillis();
		try{
			Class.forName(driver);
			try(Connection con = DriverManager.getConnection(url, user, pwd);){
				DatabaseMetaData dm = con.getMetaData();
				ResultSet tables = dm.getTables(null, null, "customer", null);
				if(!tables.next()){
					try(Statement stmt = con.createStatement();){
						stmt.executeUpdate(createTable);
						System.out.println("table customer created");
					}
				}else{
					System.out.println("table customer existed");
				}
				con.setAutoCommit(false);
				try(PreparedStatement ps = con.prepareStatement(insert);){
					int current=0;
					for(int i=0; i < total; i++){
						Customer c = customers.get(i);
						
						ps.setString(1, c.getName());
						ps.setString(2, c.getEmail());
						ps.setDate(3, c.getBirth());
						
						ps.addBatch();
						if(++current % batchSize == 0){ // avoid memory leak
							ps.executeBatch();
						}
					}
					ps.executeBatch();
				}
				con.commit();
				con.setAutoCommit(true);
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		long end = System.currentTimeMillis();
		System.out.println("spent time: " + (end-start) + " ms");
	}
	@Test
	public void testExecute(){
		TestValueInsertion tvi = new TestValueInsertion();
		tvi.setBatchSize(1000);
		tvi.setTotal(1000000);// insert 1,000,000 records spent about 44 sec.
		tvi.execute();
	}
}
