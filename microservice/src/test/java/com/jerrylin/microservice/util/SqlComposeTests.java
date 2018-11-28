package com.jerrylin.microservice.util;

import static com.jerrylin.microservice.util.SqlCompose.TAG;
import static com.jerrylin.microservice.util.SqlCompose.tk;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.jerrylin.microservice.util.SqlCompose.GroupPos;
import com.jerrylin.microservice.util.SqlCompose.TagKey;

public class SqlComposeTests {
	@Test
	public void testGetGroupPos(){
		TagKey RAN = tk("ranged");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",			TAG+RAN,
				"FROM employee",	
				"WHERE 1=1"			,TAG+RAN
				);
		GroupPos gp = sc.getGroupPos(RAN);
		int expectedStart = 1;
		int expectedEnd = 2;
		int start = gp.start;
		int end = gp.end;
		assertEquals("", expectedStart, start);
		assertEquals("", expectedEnd, end);
		System.out.println("start: " + expectedStart + ", end: " + expectedEnd);
		
		String result = sc.getGroupRange(RAN);
		String expected = 
			"FROM employee\n"
			+ "WHERE 1=1";
		assertEquals("", expected, result);
		System.out.println(expected);
	}
	@Test
	public void testGetGroupRange(){
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",		TAG+CUS,
				"  INNER JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",		TAG+COND,
				"    WHERE 1 = 1",			TAG+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",		TAG+CUS,
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0");
		
		String expected = 
			"  INNER JOIN (\n"
			+ "    SELECT id, nickname\n"
			+ "    FROM t_customer\n"
			+ "    WHERE 1 = 1\n"
			+ "  ) AS c\n"
			+ "  ON o.cus_id = c.id"
			;
		
		assertEquals("", expected, sc.getGroupRange(CUS));	
		System.out.println(expected);
		
		expected = "    WHERE 1 = 1";
		
		assertEquals("", expected, sc.getGroupRange(COND));
		System.out.println(expected);
	}
	
	@Test
	public void testAppend(){
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",		TAG+CUS,
				"  INNER JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",		TAG+COND,
				"    WHERE 1 = 1",			TAG+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",		TAG+CUS,
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
		
		String result = String.join("\n", sc.append(COND, "AND nickname IS NOT NULL"));
		assertEquals("", expected, result);
		System.out.println(expected);
	}
	
	@Test
	public void testAppendJoinConds(){
		TagKey CUS = tk("join_cus");
		TagKey COND = tk("join_cus_cond");
		
		SqlCompose sc = SqlCompose.gen(
				"SELECT *",
				"FROM t_order AS o",		TAG+CUS,
				"  LEFT JOIN (",			
				"    SELECT id, nickname",
				"    FROM t_customer",		TAG+COND,
				"    WHERE 1 = 1",			TAG+COND,
				"  ) AS c",
				"  ON o.cus_id = c.id",		TAG+CUS,
				"WHERE o.no > 'A001'",
				"ORDER BY o.id DESC",
				"LIMIT 2",
				"OFFSET 0");
		
		List<String> list = sc.appendJoinConds(COND, "AND nickname IS NULL", CUS);
		String result = String.join("\n", list);
		
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
		System.out.println(result);
	}
}
