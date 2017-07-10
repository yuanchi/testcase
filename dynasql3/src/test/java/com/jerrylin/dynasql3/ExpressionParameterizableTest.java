package com.jerrylin.dynasql3;

import static com.jerrylin.dynasql3.util.SearchCondition.*;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

import org.junit.Test;

import com.jerrylin.dynasql3.node.CustomExpression;
import com.jerrylin.dynasql3.node.RootNode;
import com.jerrylin.dynasql3.node.SimpleCondition;

public class ExpressionParameterizableTest {
	
	private void findMatchedName(String expected, String tested){
		Matcher m = ExpressionParameterizable.FIND_NAME_PARM.matcher(tested);
		while(m.find()){
			String g = m.group();
			assertEquals(expected, g);
		}
	}
	@Test
	public void findNameParam(){
		List<String> testeds = 
			Arrays.asList("p.name = :pNnme",	"p.friends IN (:pFriends)", "emp.birth >:someday");
		List<String> expecteds = 
			Arrays.asList(":pNnme",				"(:pFriends)",				":someday");
		
		for(int i = 0; i < testeds.size(); i++){
			String tested = testeds.get(i);
			String expected = expecteds.get(i);
			findMatchedName(expected, tested);
		}
	}
	private void findMatchedNameCount(int expected, String tested){
		Matcher m = ExpressionParameterizable.FIND_NAME_PARM.matcher(tested);
		int count = 0;
		while(m.find()){
			String g = m.group();
			String g1 = m.group(1);
			String g2 = m.group(2);
			System.out.println("g is " + g + " g1 is " + g1 + ", g2 is " + g2);
			++count;
		}
		assertEquals(expected, count);
	}
	@Test
	public void findNameParamCount(){
		List<List<Object>> collect = Arrays.asList(
			Arrays.asList("p.name LIKE :pname OR p.age > :page", 2),
			Arrays.asList("emp.salary IN (:sal) and emp.start > :startDay AND (emp.id > :empId OR emp.gender = :epmGender)", 4)
		);
		for(List<Object> c : collect){
			String tested = (String)c.get(0);
			Integer expected = (Integer)c.get(1);
			findMatchedNameCount(expected, tested);
		}
	}
	@Test
	public void compileToQuestionMark(){
		SimpleCondition sc = new SimpleCondition();
		
		sc.setExpression("p.name LIKE :pname OR p.age > :page");
		sc.transferParamNameToQuestionMark();
		String expected = "p.name LIKE ? OR p.age > ?";
		assertEquals(expected, sc.getExpression());
		
		sc.setExpression("emp.salary IN (:sal) and emp.start > :startDay AND (emp.id > :empId OR emp.gender = :epmGender)");
		sc.transferParamNameToQuestionMark();
		expected = "emp.salary IN (?) and emp.start > ? AND (emp.id > ? OR emp.gender = ?)";
		assertEquals(expected, sc.getExpression());
		
		List<Integer> salaries = Arrays.asList(1000, 2000, 2200, 5000);
		sc.setVal("sal", salaries);
		sc.setExpression("emp.salary IN (:sal) and emp.start > :startDay AND (emp.id > :empId OR emp.gender = :epmGender)");
		sc.transferParamNameToQuestionMark();
		expected = "emp.salary IN (?, ?, ?, ?) and emp.start > ? AND (emp.id > ? OR emp.gender = ?)";
		assertEquals(expected, sc.getExpression());
	}
	@Test
	public void setParamValueWithIdx(){
		RootNode root = RootNode.create()
			.select("a.id", "a.name")
			.fromAs("ACCOUNT", "a")
			.where(w->
				w.and("a.open > ?")
					.or("a.open < ?"))
			.orderByWith("a.name", "DESC");
		
		CustomExpression ce = root.getTopMostFactory().create(CustomExpression.class);
		ce.setExpression("LIMIT ? OFFSET ?"); // one expression mapping to two more parameters
		
		root.add(ce);
		
		String sql = root.toSql();
		String expected = 
			"SELECT a.id, a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > ? OR a.open < ?\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT ? OFFSET ?";
		assertEquals(expected, sql);
		
		String sqlf = root.toSqlf();
		expected = 
			"SELECT a.id,\n"
		  + " a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > ?\n"
		  + " OR a.open < ?\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT ? OFFSET ?";
		assertEquals(expected, sqlf);
		
		CustomExpression found = root.findWith(expr("LIMIT ? OFFSET ?"));
		found.setVal(0, 10);
		found.setVal(1, 3);

		int found1 = found.getVal(0);
		int found2 = found.getVal(1);
		assertEquals(10, found1);
		assertEquals(3, found2);
	}
	@Test
	public void setParamValueWithName(){
		RootNode root = RootNode.create()
			.select("a.id", "a.name")
			.fromAs("ACCOUNT", "a")
			.where(w->
				w.and("a.open > :openStart")
					.or("a.open < :openEnd"))
			.orderByWith("a.name", "DESC");
		
		CustomExpression ce = root.getTopMostFactory().create(CustomExpression.class);
		ce.setExpression("LIMIT :limit OFFSET :offset"); // one expression mapping to two more parameters
		
		root.add(ce);
		
		String sql = root.toSql();
		String expected = 
			"SELECT a.id, a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > :openStart OR a.open < :openEnd\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT :limit OFFSET :offset";
		assertEquals(expected, sql);
		
		String sqlf = root.toSqlf();
		expected = 
			"SELECT a.id,\n"
		  + " a.name\n"
		  + "FROM ACCOUNT AS a\n"
		  + "WHERE a.open > :openStart\n"
		  + " OR a.open < :openEnd\n"
		  + "ORDER BY a.name DESC\n"
		  + "LIMIT :limit OFFSET :offset";
		assertEquals(expected, sqlf);
		
		CustomExpression found1 = root.findWith(paramName("limit"));
		found1.setVal(10); // current is limit parameter without needing to specify name or index
		found1.setVal("offset", 3);

		int found1_v1 = found1.getVal(0); // access with index
		int found1_v2 = found1.getVal(1);
		assertEquals(10, found1_v1);
		assertEquals(3, found1_v2);
		
		CustomExpression found2 = root.findWith(paramName("offset"));
		found2.setVal(6); // current is offset parameter without needing to specify name or index
		found2.setVal("limit", 15);

		int found2_v1 = found2.getVal("offset"); // access with parameter name
		int found2_v2 = found2.getVal("limit");
		assertEquals(6, found2_v1);
		assertEquals(15, found2_v2);
	}
}
