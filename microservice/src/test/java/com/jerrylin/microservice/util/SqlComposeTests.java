package com.jerrylin.microservice.util;

import static com.jerrylin.microservice.util.SqlCompose.RA;
import static com.jerrylin.microservice.util.SqlCompose.RL;
import static com.jerrylin.microservice.util.SqlCompose.RO;
import static com.jerrylin.microservice.util.SqlCompose.ROB;
import static com.jerrylin.microservice.util.SqlCompose.RW;
import static com.jerrylin.microservice.util.SqlCompose.R_LIMIT;
import static com.jerrylin.microservice.util.SqlCompose.R_OFFSET;
import static com.jerrylin.microservice.util.SqlCompose.R_ORDER_BY;
import static com.jerrylin.microservice.util.SqlCompose.R_WHERE;
import static com.jerrylin.microservice.util.SqlCompose.TAG_GRP;
import static com.jerrylin.microservice.util.SqlCompose.tk;
import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.junit.Test;

import com.jerrylin.microservice.util.SqlCompose.GroupPos;
import com.jerrylin.microservice.util.SqlCompose.JoinTarget;
import com.jerrylin.microservice.util.SqlCompose.JoinWhere;
import com.jerrylin.microservice.util.SqlCompose.TagKey;
import com.zaxxer.hikari.HikariDataSource;

public class SqlComposeTests {
	static void log(String content){
		System.out.println(content);
	}
	@Test
	public void getGroupPos(){
		TagKey RAN = tk("ranged");
		TagKey RAN2 = tk("ranged2");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",			TAG_GRP+RAN,	TAG_GRP+RAN2,
				"FROM employee",	
				"WHERE 1=1"			,TAG_GRP+RAN,	TAG_GRP+RAN2
				);
		GroupPos gp = sc.getGroupPos(RAN);
		int expectedStart = 1;
		int expectedEnd = 2;
		int start = gp.start;
		int end = gp.end;
		assertEquals("", expectedStart, start);
		assertEquals("", expectedEnd, end);
		log("start: " + expectedStart + ", end: " + expectedEnd);
		
		String result = sc.getGroupRange(RAN);
		String expected = 
			"FROM employee\n"
			+ "WHERE 1=1";
		assertEquals("", expected, result);
		
		GroupPos gp2 = sc.getGroupPos(RAN2);
		assertEquals("", start, gp2.start);
		assertEquals("", end, gp2.end);
	}
	@Test
	public void getGroupRange(){
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",      TAG_GRP+CUS,
				"  INNER JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",    TAG_GRP+COND,
				"    WHERE 1 = 1",        TAG_GRP+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",   TAG_GRP+CUS,
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0"
				);
		
		String expected = 
			"  INNER JOIN (\n"
			+ "    SELECT id, nickname\n"
			+ "    FROM t_customer\n"
			+ "    WHERE 1 = 1\n"
			+ "  ) AS c\n"
			+ "  ON o.cus_id = c.id"
			;
		
		assertEquals("", expected, sc.getGroupRange(CUS));	
		log(expected);
		
		expected = "    WHERE 1 = 1";
		
		assertEquals("", expected, sc.getGroupRange(COND));
		log(expected);
	}
	
	@Test
	public void appendIn(){
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",       TAG_GRP+CUS,
				"  INNER JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",     TAG_GRP+COND,
				"    WHERE 1 = 1",         TAG_GRP+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",    TAG_GRP+CUS,
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0");
		
		String expected = 
			"SELECT *\n"
			+ "FROM t_order AS o\n"
			+ "  INNER JOIN (\n"
			+ "    SELECT id, nickname\n"
			+ "    FROM t_customer\n"
			+ "    WHERE 1 = 1 AND nickname IS NOT NULL\n"
			+ "  ) AS c\n"
			+ "  ON o.cus_id = c.id\n"
			+ "WHERE o.no > 'A001'\n"
			+ "ORDER BY o.id DESC\n"
			+ "LIMIT 2\n"
			+ "OFFSET 0";
		
		String result = sc.appendIn(COND, "AND nickname IS NOT NULL").joinWithBr();
		assertEquals("", expected, result);
		log(expected);
	}
	
	@Test
	public void appendInJoinConds(){
		JoinTarget CUS = new JoinTarget("c");
		JoinWhere COND = CUS.where();
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",       CUS.gk(),
				"  LEFT JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",     COND.gk(),
				"    WHERE 1 = 1",         COND.gk(),
				"  ) AS c",
				"  ON o.cus_id = c.id",    CUS.gk(),
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0");
		
		String result = sc.appendInJoinConds(CUS, "AND nickname IS NULL").joinWithBr();
		
		String expected = 
			"SELECT *\n"
			+ "FROM t_order AS o\n"
			+ "  INNER JOIN (\n" // LEFT JOIN -> INNER JOIN
			+ "    SELECT id, nickname\n"
			+ "    FROM t_customer\n"
			+ "    WHERE 1 = 1 AND nickname IS NULL\n" // append join conditions
			+ "  ) AS c\n"
			+ "  ON o.cus_id = c.id\n"
			+ "WHERE o.no > 'A001'\n"
			+ "ORDER BY o.id DESC\n"
			+ "LIMIT 2\n"
			+ "OFFSET 0";
		assertEquals("", expected, result);
		log(result);
	}
	@Test
	public void removeWithIdx(){
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",
				"  LEFT JOIN (",
				"    SELECT id, nickname",
				"    FROM t_customer",
				"  ) AS c",
				"  ON o.cus_id = c.id",
				"WHERE o.no > 'A001'",		
				"ORDER BY o.id DESC",  // 8		
				"LIMIT 2",             // 9
				"OFFSET 0",            // 10
				";");
		List<String> list = sc.remove(8, 9, 10);
		String result = String.join("\n", list);
		String expected = 
				"SELECT *\n"
				+ "FROM t_order AS o\n"
				+ "  LEFT JOIN (\n"
				+ "    SELECT id, nickname\n"
				+ "    FROM t_customer\n"
				+ "  ) AS c\n"
				+ "  ON o.cus_id = c.id\n"
				+ "WHERE o.no > 'A001'\n"
				+ ";";
		assertEquals("", expected, result);
		log(expected);
	}
	@Test
	public void removeWithTagKey(){
		TagKey JOIN = tk("join_cus");
		TagKey JOINCOND = tk("join_cus_cond");
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",       TAG_GRP+JOIN,
				"  LEFT JOIN (",
				"    SELECT id, nickname",
				"    FROM t_customer",     TAG_GRP+JOINCOND,
				"    WHERE 1 = 1",         TAG_GRP+JOINCOND,
				"  ) AS c",
				"  ON o.cus_id = c.id",    TAG_GRP+JOIN,
				"WHERE o.no > 'A001'",		
				"ORDER BY o.id DESC",		
				"LIMIT 2",
				"OFFSET 0",
				";");
		List<String> list = sc.remove(JOIN, JOINCOND);
		String result = String.join("\n", list);
		String expected = 
				"SELECT *\n"
				+ "FROM t_order AS o\n"
				+ "WHERE o.no > 'A001'\n"
				+ "ORDER BY o.id DESC\n"
				+ "LIMIT 2\n"
				+ "OFFSET 0\n"
				+ ";";
		assertEquals("", expected, result);
		log(expected);
	}	
	@Test
	public void calcTotal(){
		TagKey ROOT_ORD = tk("root_ord");
		TagKey ROOT_PAGING = tk("root_paging");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",    RA+"o",
				"  LEFT JOIN (",
				"    SELECT id, nickname",
				"    FROM t_customer",
				"  ) AS c",
				"  ON o.cus_id = c.id",
				"WHERE o.no > 'A001'",   TAG_GRP+ROOT_ORD,
				"ORDER BY o.id DESC",    TAG_GRP+ROOT_ORD, TAG_GRP+ROOT_PAGING,
				"LIMIT 2",
				"OFFSET 0",                                TAG_GRP+ROOT_PAGING,
				";");
		
		List<String> list = sc.calcTotal(null, ROOT_ORD, ROOT_PAGING);
		String result = String.join("\n", list);
		String expected = 
				"SELECT COUNT(DISTINCT o.id)\n" // change to select count
				+ "FROM t_order AS o\n"
				+ "  LEFT JOIN (\n"
				+ "    SELECT id, nickname\n"
				+ "    FROM t_customer\n"
				+ "  ) AS c\n"
				+ "  ON o.cus_id = c.id\n"
				+ "WHERE o.no > 'A001'\n"
				+ ";"; // remove statements not required for counting
		log(result);
		assertEquals("", expected, result);
		
	}	
	@Test
	public void testTreeSet(){
		TreeSet<Integer> set = new TreeSet<>((a, b)->{return b - a;});
		set.add(4);
		set.add(4);
		set.add(3);
		set.add(5);
		
		assertEquals("", new Integer(5), set.pollFirst());
		assertEquals("", new Integer(4), set.pollFirst());
		assertEquals("", new Integer(3), set.pollFirst());
	}
	@Test
	public void replaceWithIdx(){		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",
				"  LEFT JOIN (",
				"    SELECT id, nickname",
				"    FROM t_customer",
				"  ) AS c",
				"  ON o.cus_id = c.id",
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0",
				";");
		List<String> list = sc.replaceWith("  where o.id > '001'", 7);
		String result = String.join("\n", list);
		String expected = 
			sc.joinWithBr()
			.replace("WHERE o.no > 'A001'", "  where o.id > '001'");
		assertEquals("", expected, result);
		log(result);
	}
	@Test
	public void replaceWithTagKey(){
		TagKey ROOT_ORD = tk("root_ord");
		TagKey ROOT_PAGING = tk("root_paging");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",
				"  LEFT JOIN (",
				"    SELECT id, nickname",
				"    FROM t_customer",
				"  ) AS c",
				"  ON o.cus_id = c.id",
				"WHERE o.no > 'A001'",   TAG_GRP+ROOT_ORD,
				"ORDER BY o.id DESC",    TAG_GRP+ROOT_ORD,  TAG_GRP+ROOT_PAGING,
				"LIMIT 2",
				"OFFSET 0",                                 TAG_GRP+ROOT_PAGING,
				";");
		List<String> list = sc.replaceWith("order by o.id ASC", ROOT_ORD);
		String result = String.join("\n", list);
		String expected = 
				sc.joinWithBr()
				.replace("ORDER BY o.id DESC", "order by o.id ASC");
		assertEquals("", expected, result);
		log(result);
		
		result = String.join("\n", sc.replaceWith("limit 3", ROOT_PAGING));
		expected = 
				sc.joinWithBr()
				.replace("LIMIT 2\nOFFSET 0", "limit 3");
		assertEquals("", expected, result);
		log(result);
	}
	@Test
	public void testSimplePagingQuery(){
		// configuring sql template and marks
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",           RA+"e",
				"FROM employee AS e", RW,
				"WHERE 1 = 1",        RW, ROB,
				"ORDER BY e.id DESC",     ROB, RL,
				"LIMIT 2",                     RL, RO,
				"OFFSET 0",                        RO,
				";"
				);
		// using cloned object keeping original sql composition
		SqlCompose c = sc.clone();
		// collecting and sending parameters
		PageParams pp = 
				new PageParams(
					R_WHERE, "e.id > '0001' AND e.age > 30",
					R_ORDER_BY, "e.no ASC",
					R_LIMIT, "5",
					R_OFFSET, "9"
				);
		// change where conditions and paging settings 
		String result = 
				c.replaceExact(R_WHERE, pp.rootConds())
				.replaceExact(R_ORDER_BY, pp.rootOrderBy())
				 .replaceExact(R_LIMIT, pp.rootLimit())
				 .replaceExact(R_OFFSET, pp.rootOffset())
				.joinWithBr();
		String expected = 
				sc.joinWithBr()
				.replace("WHERE 1 = 1", pp.rootConds())
				.replace("ORDER BY e.id DESC", pp.rootOrderBy())
				.replace("LIMIT 2", pp.rootLimit())
				.replace("OFFSET 0", pp.rootOffset());
		assertEquals("", expected, result);
		log(result);
		
		// calculating total count sql ignoring paging settings
		List<String> total = c.calcTotal(null, R_ORDER_BY, R_LIMIT, R_OFFSET);
		String totalVal = String.join("\n", total);
		String expectedTotal = 
				String.join("\n", c.remove(R_ORDER_BY, R_LIMIT, R_OFFSET))
				.replace("SELECT *", "SELECT COUNT(DISTINCT e.id)");
		assertEquals("", expectedTotal, totalVal);
		log(totalVal);
		
		// using genPagingSql method generates the same result as the above
		SqlCompose c2 = sc.clone();
		String anotherVersion = c2.genPagingSql(pp);
		String expectedResult = expected + "\n" + totalVal;
		assertEquals("", expectedResult, anotherVersion);
		log(expectedResult);
	}
	@Test
	public void genPagingSql(){
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",         
				"FROM t_order AS o",             RA+"o", // root alias as o, default is p
				"LEFT JOIN (",
				"  SELECT id, nickname",
				"  FROM t_customer",
				"  WHERE 1=1",
				") AS cus ON o.cus_id = cus.id", RW,
				"WHERE 1=1",                     RW, ROB,
				"ORDER BY o.id DESC",                ROB, RL,
				"LIMIT 5",                                RL, RO,
				"OFFSET 0",                                   RO,
				";");
		PageParams pp = 
				new PageParams(
					R_WHERE,    "o.no LIKE 'A00%'",
					R_ORDER_BY, "o.no DESC",
					R_LIMIT,    "2",
					R_OFFSET,   "0"
				);
		String sql1 = sc.clone().genPagingSql(pp);
		
		HikariDataSource ds = DataSourceUtils.ds();
		List<Map<String, Object>> collects = new LinkedList<>();
		try(Connection con = ds.getConnection();){
			con.setAutoCommit(false);
			try{
				// ref. https://stackoverflow.com/questions/9696572/queries-returning-multiple-result-sets
				// execute()和getMoreResults()回傳false代表結果是數字，不是ResultSet
				// stmt.getUpdateCount() == -1代表沒有更多ResultSet
				try(Statement stmt = con.createStatement();){
					boolean isResultSet = stmt.execute(sql1);
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
							log("Result " + count + " is just a count " + stmt.getUpdateCount());
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
			log(collect.toString());
		}
	}
	@Test
	public void testGenPagingSqlWithJoinConds(){
		JoinTarget cus = new JoinTarget("c");
		JoinWhere cusConds = cus.where();
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",    cus.gk(), RA+"o", 
				"LEFT JOIN (",
				"  SELECT id, nickname",
				"  FROM t_customer",    cusConds.gk(),
				"  WHERE 1=1",          cusConds.gk(),
				") AS c",
				"  ON o.cus_id = c.id", cus.gk(), RW,
				"WHERE 1=1",                      RW, ROB,
				"ORDER BY o.id DESC",                 ROB, RL,
				"LIMIT 3",                                 RL, RO,
				"OFFSET 0",                                    RO,
				";");
		
		PageParams pp = 
				new PageParams(
						R_WHERE, "o.no LIKE 'A%'",
						R_ORDER_BY, "o.id ASC",
						R_LIMIT, "10",
						R_OFFSET, "3"
				);
		pp.joinWhere(cusConds, "nickname LIKE 'Marry'");
		String sql = sc.clone().genPagingSql(pp);
		log(sql);
	}
}
