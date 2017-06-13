package com.jerrylin.dynasql3.node;

import static com.jerrylin.dynasql3.util.SearchCondition.paramName;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.jerrylin.dynasql3.ExpressionAliasible;

public class RootNodeTest {
	/**
	 * the purpose of this test case is to observe returning type
	 */
	@Test
	public void config(){
		RootNode root = new RootNode();
		root.config(me->{
			// do nothing
		});
	}
	@Test
	public void add(){
		RootNode root = new RootNode();
		SqlNode<?> levle1 = new SqlNode<>();
		RootNode r = root.add(levle1);
		assertTrue(root == r);
	}
	@Test
	public void removeIfParamValNotExisted(){
		RootNode root = RootNode.create();
		root.select("emp.id", "emp.salary", "emp.gender")
			.fromAs("EMPLOYEE", "emp")
			.where(w->
				w.and("emp.salary > :salary1")
				.or("emp.salary < :salary2")
				.or(fc->
					fc.and("emp.gender = :gender")
						.and("emp.point > :point")));
		String expected = 
			"SELECT emp.id, emp.salary, emp.gender\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.salary > :salary1 OR emp.salary < :salary2 OR (emp.gender = :gender AND emp.point > :point)";
		assertEquals(expected, root.toSql());
		
		RootNode copy1 = (RootNode)root.copy();
		copy1.removeIfParamValNotExisted();
		String copy1Sql = copy1.toSql();
		expected = 
			"SELECT emp.id, emp.salary, emp.gender\n"
			+ "FROM EMPLOYEE AS emp";
		assertEquals(expected, copy1Sql);
		
		RootNode copy2 = (RootNode)root.copy();
		SimpleCondition salary1 = copy2.findWith(paramName("salary1"));
		SimpleCondition salary2 = copy2.findWith(paramName("salary2"));
		SimpleCondition gender = copy2.findWith(paramName("gender"));
		SimpleCondition point = copy2.findWith(paramName("point"));
		
		assertEquals("emp.salary > :salary1", salary1.getExpression());
		assertEquals("emp.salary < :salary2", salary2.getExpression());
		assertEquals("emp.gender = :gender", gender.getExpression());
		assertEquals("emp.point > :point", point.getExpression());
		
		salary1.setVal(200);
		gender.setVal("F");
		// there are no param values on salary2, point
		
		copy2.removeIfParamValNotExisted();
		String copy2Sql = copy2.toSql();
		expected = 
			"SELECT emp.id, emp.salary, emp.gender\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.salary > :salary1 OR emp.gender = :gender";
		assertEquals(expected, copy2Sql);
	}
	@Test
	public void withParamValues(){
		RootNode root = RootNode.create()
			.select("emp.id")
			.fromAs("EMPLOYEE", "emp")
			.where("emp.age > :age");
		
		Map<String, Object> paramValues1 = root.withParamValues().getParamValues();
		assertEquals(0, paramValues1.size());
		
		String sql = root.toSql();
		String expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.age > :age";
		assertEquals(expected, sql);
		
		sql = root.copy().removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp";
		assertEquals(expected, sql);
		
		SimpleCondition sc1 = root.findWith(paramName("age"));
		sc1.setVal(20);
		
		Map<String, Object> paramValues2 = root.withParamValues().getParamValues();
		assertEquals(1, paramValues2.size());
		Map.Entry<String, Object> p1 = paramValues2.entrySet().iterator().next();
		assertEquals("age", p1.getKey());
		assertEquals(20, p1.getValue());
		
		sql = root.removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.age > :age";
		assertEquals(expected, sql);
		
		root.where("emp.gender = :gender");
		sql = root.toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.age > :age AND emp.gender = :gender";
		assertEquals(expected, sql);
		
		sql = root.copy().removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.age > :age";
		assertEquals(expected, sql);
		
		SimpleCondition sc2 = root.findWith(paramName("gender"));
		sc2.setVal("F");
		sql = root.removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS emp\n"
			+ "WHERE emp.age > :age AND emp.gender = :gender";
		assertEquals(expected, sql);
				
		Map<String, Object> paramValues3 = root.withParamValues().getParamValues();
		assertEquals(2, paramValues3.size());
		Iterator<Map.Entry<String, Object>> itr1 = paramValues3.entrySet().iterator();
		itr1.next();
		Map.Entry<String, Object> p2 = itr1.next();
		assertEquals("gender", p2.getKey());
		assertEquals("F", p2.getValue());
	}
	@Test
	public void withParamValuesAfterCompiling(){
		RootNode root = RootNode.create()
			.select("emp.id")
			.fromAs("EMPLOYEE", "e")
			.where("emp.age > :age");
		
		String sql = root.toSql();
		String expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS e\n"
			+ "WHERE emp.age > :age";
		assertEquals(expected, sql);
		
		sql = root.copy().removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS e";
		assertEquals(expected, sql);
		
		SimpleCondition sc1 = root.findWith(paramName("age"));
		sc1.setVal(40);
		
		RootNode copy1 = root.copy();
		Map<String, Object> paramValues1 = copy1.withParamValuesAfterCompiling().getParamValues();
		assertEquals(1, paramValues1.size());
		
		Map.Entry<String, Object> paramValue1 = paramValues1.entrySet().iterator().next();
		assertEquals("1", paramValue1.getKey());
		assertEquals(40, paramValue1.getValue());
		
		sql = copy1.removeIfParamValNotExisted().toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS e\n"
			+ "WHERE emp.age > ?";
		assertEquals(expected, sql);
		
		root.where("emp.salary IN (:sals)");
		sql = root.toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS e\n"
			+ "WHERE emp.age > :age AND emp.salary IN (:sals)";
		assertEquals(expected, sql);
		
		List<Integer> sals = Arrays.asList(1000, 1100, 2000, 5000, 600);
		SimpleCondition sc2 = root.findWith(paramName("sals"));
		sc2.setVal(sals);
		
		Map<String, Object> paramValues2 = root.withParamValues().getParamValues();
		assertEquals(2, paramValues2.size());
		
		RootNode copy2 = root.copy();
		copy2.removeIfParamValNotExisted().withParamValuesAfterCompiling();
		sql = copy2.toSql();
		expected = 
			"SELECT emp.id\n"
			+ "FROM EMPLOYEE AS e\n"
			+ "WHERE emp.age > ? AND emp.salary IN (?, ?, ?, ?, ?)";
		assertEquals(expected, sql);
		
		Map<String, Object> paramValues3 = copy2.getParamValues();
		assertEquals(6, paramValues3.size());
		
		
		// TODO if one expression mapped to multiple parameters??
		System.out.println(sql);
	}

	@Test
	public void changeSqlNodeTreeAsOraclePaging(){
		RootNode oldOne = RootNode.create()
			.select("a.id", "a.name")
			.fromAs("ACCOUNT", "a")
			.where(w->
				w.and("a.open > ?")
					.or("a.open < ?"))
			.orderByWith("a.name", "DESC");
		
		RootNode newOne = RootNode.create()
			.select("*")
			.from(f->f.subquery(se->
				se.select(s->s.t("tmp.*").t("rownum").as("rn"))
					.from()
				  		.add(oldOne.copy()) // add old SqlNode tree's copy as view source, combing two trees into one
				  		.as("tmp") // add extra alias
				  		.toStart() // return to start SelectExpression to continue
				  	.where("rownum <= 20")))
			.where("rn > 10");
		
		String sql = newOne.toSql();
		String expected = 
			"SELECT *\n"
		  + "FROM (SELECT tmp.*, rownum AS rn\n"
		  + "FROM (SELECT a.id, a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > ? OR a.open < ?\n"
		  + "ORDER BY a.name DESC) AS tmp\n"
		  + "WHERE rownum <= 20)\n"
		  + "WHERE rn > 10";
		assertEquals(expected, sql);
		
		String sqlf = newOne.toSqlf();
		expected = 
			"SELECT *\n"
		  + "FROM (SELECT tmp.*,\n"
		  + "    rownum AS rn\n"
		  + "   FROM (SELECT a.id,\n"
		  + "       a.name\n"
		  + "      FROM ACCOUNT AS a\n"
		  + "      WHERE a.open > ?\n"
		  + "       OR a.open < ?\n"
		  + "      ORDER BY a.name DESC) AS tmp\n"
		  + "   WHERE rownum <= 20)\n"
		  + "WHERE rn > 10";
		assertEquals(expected, sqlf);
	}
	@Test
	public void removeJoinTargetIfNotReferenced1(){
		RootNode root = RootNode.create()
			.select("e.name", "e.firstname")
			.fromAs("EMPLOYEE", "e")
			.from(f->f.leftOuterJoin("SALARY").as("s")
					.on("e.id = s.emp_id")
				.leftOuterJoin(se->
					se.select("i.age", "i.start")
					.fromAs("INFO", "i")
					.where("i.start > :start_date")).as("info")
					.on("info.emp_id = e.id")
				.leftOuterJoin("PERFORMANCE").as("per")
					.on("per.id = info.per_id"));
		String sqlf = root.toSqlf();
		String expected = 
			"SELECT e.name,\n"
			+ " e.firstname\n"
			+ "FROM EMPLOYEE AS e\n"
			+ " LEFT OUTER JOIN SALARY AS s\n"
			+ "  ON e.id = s.emp_id\n"
			+ " LEFT OUTER JOIN (SELECT i.age,\n"
			+ "    i.start\n"
			+ "   FROM INFO AS i\n"
			+ "   WHERE i.start > :start_date) AS info\n"
			+ "  ON info.emp_id = e.id\n"
			+ " LEFT OUTER JOIN PERFORMANCE AS per\n"
			+ "  ON per.id = info.per_id";
		assertEquals(expected, sqlf);
		
		RootNode copy = root.copy();
		copy.removeJoinTargetIfNotReferenced();
		sqlf = copy.toSqlf();
		expected = 
			"SELECT e.name,\n"
			+ " e.firstname\n"
			+ "FROM EMPLOYEE AS e";
		assertEquals(expected, sqlf);
		
		root.select("per.point");
		sqlf = root.toSqlf();
		expected = 
			"SELECT e.name,\n"
			+ " e.firstname,\n"
			+ " per.point\n"
			+ "FROM EMPLOYEE AS e\n"
			+ " LEFT OUTER JOIN SALARY AS s\n"
			+ "  ON e.id = s.emp_id\n"
			+ " LEFT OUTER JOIN (SELECT i.age,\n"
			+ "    i.start\n"
			+ "   FROM INFO AS i\n"
			+ "   WHERE i.start > :start_date) AS info\n"
			+ "  ON info.emp_id = e.id\n"
			+ " LEFT OUTER JOIN PERFORMANCE AS per\n"
			+ "  ON per.id = info.per_id";		
		assertEquals(expected, sqlf);
		
		root.removeJoinTargetIfNotReferenced();
		sqlf = root.toSqlf();
		expected = 
			"SELECT e.name,\n"
			+ " e.firstname,\n"
			+ " per.point\n"
			+ "FROM EMPLOYEE AS e\n"
			+ " LEFT OUTER JOIN (SELECT i.age,\n"
			+ "    i.start\n"
			+ "   FROM INFO AS i\n"
			+ "   WHERE i.start > :start_date) AS info\n"
			+ "  ON info.emp_id = e.id\n"
			+ " LEFT OUTER JOIN PERFORMANCE AS per\n"
			+ "  ON per.id = info.per_id";		
		assertEquals(expected, sqlf);
	}
	@Test
	public void moveFiltersToJoin1(){
		RootNode root = RootNode.create()
			.select("p.name")
			.fromAs("PERSON", "p")
			.where("p.age > :age", "p.male = :male");
		String sqlf = root.toSqlf();
		String expected = 
			"SELECT p.name\n"
			+ "FROM PERSON AS p\n"
			+ "WHERE p.age > :age\n"
			+ " AND p.male = :male";
		assertEquals(expected, sqlf);
		root.moveFiltersToJoin(); // not effected
		assertEquals(expected, root.toSqlf());
	}
	@Test
	public void moveFiltersToJoin2(){
		RootNode root = RootNode.create()
			.select("p.name")
			.from(f->
				f.t("PERSON").as("p")
				.leftOuterJoin("INFO").as("i")
					.on("i.p_id = p.id"))
			.where("p.age > :age", "p.male = :male", "i.point > :point");
		String sqlf = root.toSqlf();
		String expected = 
			"SELECT p.name\n"
			+ "FROM PERSON AS p\n"
			+ " LEFT OUTER JOIN INFO AS i\n"
			+ "  ON i.p_id = p.id\n"
			+ "WHERE p.age > :age\n"
			+ " AND p.male = :male\n"
			+ " AND i.point > :point";
		assertEquals(expected, sqlf);
		root.moveFiltersToJoin(); // not effected
		expected = 
			"SELECT p.name\n"
			+ "FROM (SELECT *\n"
			+ "  FROM PERSON AS within_p\n"
			+ "  WHERE within_p.age > :age\n"
			+ "   AND within_p.male = :male) AS p\n"
			+ " LEFT OUTER JOIN INFO AS i\n" // bypass left outer join
			+ "  ON i.p_id = p.id\n"
			+ "WHERE i.point > :point";
		assertEquals(expected, root.toSqlf());
		// repeated, not work this time because not required
		assertEquals(expected, root.moveFiltersToJoin().toSqlf());
		System.out.println(root.toSqlf());
	}
}
