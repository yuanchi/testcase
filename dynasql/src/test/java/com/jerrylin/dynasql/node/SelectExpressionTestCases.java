package com.jerrylin.dynasql.node;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

import com.mchange.v2.c3p0.ComboPooledDataSource;

public class SelectExpressionTestCases {
	@Test
	public void initAsRoot(){
		SelectExpression se = SelectExpression.initAsRoot();
		SelectExpression root = se.getRoot();
		SelectExpression startSelect = se.getStart();
		assertTrue(se == root);
		assertTrue(se == startSelect);
	}
	/**
	 * zero-based index
	 * @param currentPage
	 * @param countPerPage
	 * @return
	 */
	private int calcDescStartIdx(int currentPage, int countPerPage){
		return (currentPage-1) * countPerPage;
	}
	@Test
	public void executeQuery(){
		// ref. http://www.mchange.com/projects/c3p0/#quickstart
		// ref. https://mariadb.com/kb/en/mariadb/about-mariadb-connector-j/
		// ref. http://docs.oracle.com/javase/1.5.0/docs/guide/jdbc/getstart/datasource.html#997347
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
			
//			cpds.setAutoCommitOnClose(false);
		
			try(Connection con = cpds.getConnection();){
				System.out.println(con.getAutoCommit());
				con.setAutoCommit(false);
				String sql =
						 "SELECT (SELECT COUNT(*)	  "
						+"  FROM member) total_count, "
						+"  r1.id id,                 "
						+"  r1.name name              "
						+"FROM member r1              "
						+"WHERE r1.id <= (SELECT r2.id"
						+"  FROM member r2            "
						+"  ORDER BY r2.id DESC       "
						+"  LIMIT ?, 1)               "
						+"ORDER BY r1.id DESC         "
						+"LIMIT ?	                  ";
				System.out.println(sql);
				try(PreparedStatement pstmt = con.prepareStatement(sql);){
					pstmt.setInt(1, 10);//1-based index
					pstmt.setInt(2, 10);
					try(ResultSet rs = pstmt.executeQuery();){
						while(rs.next()){
							String total_count = rs.getString("total_count");
							String id = rs.getString("id");
							String name = rs.getString("name");
							System.out.println("total_count" + total_count + "|id:" + id + "|name:" + name);
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
	@Test
	public void mariadbPagination(){
		// ref. http://www.slideshare.net/suratbhati/efficient-pagination-using-mysql-6187107?next_slideshow=1
		// OFFSET can hit performance bottleneck
		String table = "member";
		String pk = "id";
		int currentPage = 2;
		int countPerPage = 10;
		int startPageIdx = calcDescStartIdx(currentPage, countPerPage);
		// TODO ASC implementation
		// TODO add random filter conditions
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.subquery().as("total_count") // TODO maybe remove this code snippet
					.select()
						.t("COUNT(*)").getStart()
					.from()
						.t(table).closest(Select.class)
				.t("r1."+pk).as("id").getStart()
			.from()
				.t(table).as("r1").getStart()
			.where()
				.subqueryCond()// find the starting row id
					.t("r1." + pk).oper("<=").subquery()
						.select()
							.t("r2." + pk).getStart()
						.from()
							.t(table).as("r2").getStart()
						.orderBy()
							.desc("r2." + pk).getStart()
						.add(new Keyword("LIMIT ?, 1").id("limit_start")).getRoot()
			.orderBy()
				.desc("r1." + pk).getStart()
			.add(new Keyword("LIMIT ?").id("limit_count_per_page"))	
			.getRoot();
		String sql = root.genSql();
		SelectExpression copyRoot = root.copy();
		String copySql = copyRoot.genSql();
		assertFalse(root == copyRoot);
		assertEquals(sql, copySql);
		
		Keyword limit_start = copyRoot.findById("limit_start");
		assertEquals("limit_start", limit_start.id());
		
		Keyword limit_count_per_page = copyRoot.findById("limit_count_per_page");
		assertEquals("limit_count_per_page", limit_count_per_page.id());
		
		System.out.println(sql);
		System.out.println("first:"+startPageIdx);
		System.out.println("second:"+countPerPage);
		
//		SelectExpression root2 = SelectExpression.initAsRoot()
//			.select()
//				.t("m1."+pk).getStart()
//			.from()
//				.t(table).as("m1")
//				.innerJoin()
//					.subquery().as("m3")
//						.select()
//							.t("m2." + pk).getStart()
//						.from()
//							.t(table).as("m2").getStart()
//						.where()
//							.cond("m2."+pk+" > '20160425-095421886-DJljP'").getStart()
//						.orderBy()
//							.asc("m2."+pk).getStart()
//						.addChild(new Keyword("LIMIT " + countPerPage)).closest(From.class)
//				.on("m1."+pk+" = m3."+pk).getStart()
//			.orderBy()
//				.desc("m1."+pk)
//			.getRoot();
//		System.out.println("root2 sql:");
//		System.out.println(root2.genSql());
	}
	@Test
	public void removeConditionById(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("acc1.name")
				.subquery().as("acc2")
					.select()
						.t("COUNT(*)").as("acc_num").getStart()
					.from()
						.t("account").as("acc").getStart()
					.where()
						.delimiter()
							.cond("acc.lname LIKE 'F%'").cId("acc_lname_1")
							.and("acc.fname LIKE 'K%'").cId("acc_fname_1").closest(Where.class)
						.orDelimiter()
							.cond("acc.lname LIKE 'J%'").cId("acc_lname_2")
							.and("acc.fname LIKE 'Y%'").cId("acc_fname_2")
							.orDelimiter()
								.cond("acc.address LIKE '%Taipei%'").cId("acc_address_1")
								.or("acc.address LIKE '%Hualiang%'").cId("acc_address_2")
								.or("acc.address LIKE '%Taichung%'").cId("acc_address_3").getRoot()
			.from()
				.t("account").as("acc1").getStart()
			.where()
				.cond("acc1.code > 10000")
				.and("acc1.code < 30000")
			.getRoot();
		String sql = root.genSql();
		String expectedSql = 
			"SELECT acc1.name,"
			+ "\n  (SELECT COUNT(*) acc_num"
			+ "\n  FROM account acc"
			+ "\n  WHERE (acc.lname LIKE 'F%' AND acc.fname LIKE 'K%')"
			+ "\n    OR (acc.lname LIKE 'J%' AND acc.fname LIKE 'Y%' OR (acc.address LIKE '%Taipei%' OR acc.address LIKE '%Hualiang%' OR acc.address LIKE '%Taichung%'))) acc2"
			+ "\nFROM account acc1"
			+ "\nWHERE acc1.code > 10000"
			+ "\n  AND acc1.code < 30000";
		assertEquals(expectedSql, sql);
		root.removeConditionById(
			"acc_address_1",
			"acc_address_2",
			"acc_address_3",
			"acc_fname_2",
			"acc_lname_2");
		String sqlAfterAdjusted = root.genSql();
		expectedSql = 
			"SELECT acc1.name,"
			+ "\n  (SELECT COUNT(*) acc_num"
			+ "\n  FROM account acc"
			+ "\n  WHERE (acc.lname LIKE 'F%' AND acc.fname LIKE 'K%')) acc2"
			+ "\nFROM account acc1"
			+ "\nWHERE acc1.code > 10000"
			+ "\n  AND acc1.code < 30000";
		assertEquals(expectedSql, sqlAfterAdjusted);
	}
	@Test
	public void findParamName(){
		String input = "p.name = :name OR p.accounts IN (:accounts)";
		String p = ":(\\w+)";
		Pattern pattern = Pattern.compile(p);
		Matcher m = pattern.matcher(input);
		int i = 0;
		List<String> expectedResults = Arrays.asList("name", "accounts");
		while(m.find()){
			assertEquals(expectedResults.get(i), m.group(1));
			++i;
		}
	}
	@Test
	public void accessNamedParameterValues(){
		SelectExpression root = SelectExpression.initAsRoot()
			.select()
				.t("*").getStart()
			.from()
				.t("cusomer").as("c")
				.leftOuterJoin("account").as("a")
				.on("c.id = a.cus_id").getStart()
			.where()
				.cond("c.name LIKE :c_name1").cId("c_name1")
				.or("c.name LIKE :c_name2").cId("c_name2")
				.and("a.code IN (:a_codes)").cId("a_codes")
			.getRoot();
		
		SimpleCondition c_name1 = root.findById("c_name1");
		SimpleCondition c_name2 = root.findById("c_name2");
		SimpleCondition a_codes = root.findById("a_codes");
		
		c_name1.addParamValue("Mary");
		c_name2.addParamValue("John");
		a_codes.addParamValue(Arrays.asList("A", "C", "F"));
		
		Map<String, Object> expectedResults = new LinkedHashMap<>();
		expectedResults.put("c_name1", "Mary");
		expectedResults.put("c_name2", "John");
		expectedResults.put("a_codes", Arrays.asList("A", "C", "F"));
		
		Map<String, Object> namedParams = root.accessNamedParameterValues();
		assertEquals(expectedResults.size(), namedParams.size());
		namedParams.forEach((n,v)->{
			assertTrue(expectedResults.containsKey(n));
			assertEquals(expectedResults.get(n), v);
		});
		
		String sql = root.genSql();
		System.out.println(sql);
	}
	@Test
	public void accessInParameterValues(){
		SelectExpression root = SelectExpression.initAsRoot()
			.from()
				.t("cusomer").as("c")
				.leftOuterJoin("account").as("a")
				.on("c.id = a.cus_id").getStart()
			.where()
				.cond("c.name LIKE ?").cParamValue("Mary")
				.or("c.name LIKE ?").cParamValue("John")
				.and("a.code IN ?").cParamValue(Arrays.asList("A", "C", "F"))
			.getRoot();
		
		List<Object> expectedResults = Arrays.asList("Mary", "John", Arrays.asList("A", "C", "F"));
		List<Object> inParamValues = root.accessInParameterValues();
		assertEquals(expectedResults, inParamValues);
		
		String sql = root.genSql();
		System.out.println(sql);
	}
}
