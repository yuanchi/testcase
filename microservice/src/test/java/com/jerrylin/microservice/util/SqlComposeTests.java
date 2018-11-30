package com.jerrylin.microservice.util;

import static com.jerrylin.microservice.util.SqlCompose.RC;
import static com.jerrylin.microservice.util.SqlCompose.RL;
import static com.jerrylin.microservice.util.SqlCompose.RO;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_CONDS;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_LIMIT;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_OFFSET;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_ORDER;
import static com.jerrylin.microservice.util.SqlCompose.ROR;
import static com.jerrylin.microservice.util.SqlCompose.TAG_GRP;
import static com.jerrylin.microservice.util.SqlCompose.tk;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.TreeSet;

import org.junit.Test;

import com.jerrylin.microservice.util.SqlCompose.GroupPos;
import com.jerrylin.microservice.util.SqlCompose.TagKey;

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
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",       TAG_GRP+CUS,
				"  LEFT JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",     TAG_GRP+COND,
				"    WHERE 1 = 1",         TAG_GRP+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",    TAG_GRP+CUS,
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0");
		
		String result = sc.appendInJoinConds(COND, "AND nickname IS NULL", CUS).joinWithBr();
		
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
				"FROM t_order AS o",
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
				"SELECT COUNT(DISTINCT id)\n" // change to select count
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
				"SELECT *",
				"FROM employee AS e", RC,
				"WHERE 1 = 1",        RC, ROR,
				"ORDER BY e.id DESC",     ROR, RL,
				"LIMIT 2",                     RL, RO,
				"OFFSET 0",                        RO,
				";"
				);
		// using cloned object keeping original sql composition
		SqlCompose c = sc.clone();
		PageParams pp = 
				new PageParams(
					ROOT_CONDS, "WHERE e.id > '0001' AND e.age > 30",
					ROOT_ORDER, "ORDER BY e.no ASC",
					ROOT_LIMIT, "LIMIT 5",
					ROOT_OFFSET, "OFFSET 9"
				);
		// change where conditions and paging settings 
		String result = 
				c.replaceExact(ROOT_CONDS, pp.rootConds())
				.replaceExact(ROOT_ORDER, pp.rootOrder())
				 .replaceExact(ROOT_LIMIT, pp.rootLimit())
				 .replaceExact(ROOT_OFFSET, pp.rootOffset())
				.joinWithBr();
		String expected = 
				sc.joinWithBr()
				.replace("WHERE 1 = 1", pp.rootConds())
				.replace("ORDER BY e.id DESC", pp.rootOrder())
				.replace("LIMIT 2", pp.rootLimit())
				.replace("OFFSET 0", pp.rootOffset());
		assertEquals("", expected, result);
		log(result);
		
		// calculating total count sql ignoring paging settings
		List<String> total = c.calcTotal(null, ROOT_ORDER, ROOT_LIMIT, ROOT_OFFSET);
		String totalVal = String.join("\n", total);
		String expectedTotal = 
				String.join("\n", c.remove(ROOT_ORDER, ROOT_LIMIT, ROOT_OFFSET))
				.replace("SELECT *", "SELECT COUNT(DISTINCT id)");
		assertEquals("", expectedTotal, totalVal);
		log(totalVal);
		
		// using genPagingSql method generates the same result as the above
		SqlCompose c2 = sc.clone();
		String anotherVersion = c2.genPagingSql(pp);
		String expectedResult = expected + "\n" + totalVal;
		assertEquals("", expectedResult, anotherVersion);
		log(expectedResult);
	}
}
