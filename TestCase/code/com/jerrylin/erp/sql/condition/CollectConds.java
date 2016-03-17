package com.jerrylin.erp.sql.condition;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.Where;

/**
 * representing a set conditions
 * @author JerryLin
 *
 */
public class CollectConds extends SqlCondition{
	private static final long serialVersionUID = 3839880394325350005L;
	// Pattern is immutable and thread-safe
	private static final Pattern FIND_PROPERTY_NAME = Pattern.compile("[a-zA-Z0-9\\.\\(]+\\.[a-zA-Z0-9\\.\\)]+");
	private static final Pattern FIND_OPERATOR = Pattern.compile("(\\s+(IN|in|LIKE|like|NOT\\s+LIKE)\\s+)|\\s*(\\>\\=|\\<\\=|\\!\\=|\\=|\\<\\>|\\>|\\<)\\s*");
	private static final Pattern FIND_NAMED_PARAM_STEP1 = Pattern.compile("(\\:|\\(\\:){1}\\w+\\)?");
	private static final Pattern FIND_NAMED_PARAM_STEP2 = Pattern.compile("([^\\:]|[^(\\(\\:)]){1}\\w+[^\\)]?");
	
	
	public static CollectConds getInstance(){
		return new CollectConds();
	}
	public CollectConds addCond(ISqlCondition cond){
		addChild(cond);
		return this;
	}
	public CollectConds collectConds(){
		CollectConds collectConds = CollectConds.getInstance();
		addChild(collectConds);
		return collectConds;
	}
	public CollectConds andCollectConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.AND);
		return conds;
	}
	public CollectConds orCollectConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.OR);
		return conds;		
	}
	public Where getWhere(){
		ISqlNode parent = getParent();
		if(parent instanceof Where){
			return (Where)parent;
		}
		throw new RuntimeException("Parent Node's class is NOT expected: Where");
	}
	private SimpleCondition simpleCond(String expression, Class<?> type, Object val){
		String propertyName = findPropertyName(expression);
		String operator = findOperator(expression);
		String namedParam = findNamedParam(expression);
		
		SimpleCondition s = new SimpleCondition()
								.propertyName(propertyName)
								.operator(operator)
								.type(type)
								.value(val);
		s.id(namedParam);
		addChild(s);
		return s;
	}
	public CollectConds andSimpleCond(String expression, Class<?> type, Object val){
		SimpleCondition s = simpleCond(expression, type, val);
		s.junction(Junction.AND);
		return this;
	}
	public CollectConds andSimpleCond(String expression, Class<?> type){
		andSimpleCond(expression, type, null);
		return this;
	}
	public CollectConds orSimpleCond(String expression, Class<?> type, Object val){
		SimpleCondition s = simpleCond(expression, type, val);
		s.junction(Junction.OR);
		return this;
	}	
	public CollectConds orSimpleCond(String expression, Class<?> type){
		orSimpleCond(expression, type, null);
		return this;
	}
	/**
	 * add AND statement without needing to input condition value
	 * ex: AND p.age IS NOT NULL
	 * @param expression
	 * @return
	 */
	public CollectConds andStatement(String expression){
		ConditionStatement s = new ConditionStatement();
		s.setExpression(expression);
		s.junction(Junction.AND);
		addChild(s);
		return this;
	}
	/**
	 * add OR statement without needing to input condition value
	 * ex: OR p.fbName IS NULL
	 * @param expression
	 * @return
	 */
	public CollectConds orStatement(String expression){
		ConditionStatement s = new ConditionStatement();
		s.setExpression(expression);
		s.junction(Junction.OR);
		addChild(s);
		return this;		
	}
	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
			.map(ISqlNode::genSql)
			.collect(Collectors.toList());
		String result = StringUtils.join(items, "\n      "); 
		String and = Junction.AND.toString();
		if(result.indexOf(and) == 0){
			result = result.substring(4, result.length());
		}
		String or = Junction.OR.toString();
		if(result.indexOf(or) == 0){
			result = result.substring(3, result.length());
		}
		return getJunction().getSymbol() + " (" + result + ")";
	}
	
	private static String findFirstMatch(Pattern p, String input){
		Matcher m = p.matcher(input);
		String found = "";
		while(m.find()){
			int start = m.start();
			int end = m.end();
			found = input.substring(start, end);
		}
		return found;
	}
	/**
	 * If the input is 'p.age = :pAge', return 'p.age'
	 * @param input
	 * @return
	 */
	private static String findPropertyName(String input){
		String found = findFirstMatch(FIND_PROPERTY_NAME, input);
		return found;
	}
	/**
	 * If the input is 'p.age = :pAge', return '='
	 * @param input
	 * @return
	 */
	private static String findOperator(String input){
		String found = findFirstMatch(FIND_OPERATOR, input);
		found = found.trim();
		return found;
	}
	/**
	 * If the input is 'p.age = :pAge', return 'pAge'
	 * @param input
	 * @return
	 */
	private static String findNamedParam(String input){
		String found = findFirstMatch(FIND_NAMED_PARAM_STEP1, input);
//		System.out.println("first found: " + found);
		found = findFirstMatch(FIND_NAMED_PARAM_STEP2, found);
		return found;
	}
	private static void testFindSimpleExpression(){
		String t1 = "AND p.name = :pName";
		String t2 = "OR p.birthday >= :pBirthday";
		String t3 = "AND s.age < :sAge";
		String t4 = "AND p.orders IN (:pOrders)";
		
		List<String> items = Arrays.asList(t1, t2, t3, t4);
		items.forEach(t->{
			String t_porpertyName = findPropertyName(t);
			String t_operator = findOperator(t);
			String t_namedParam = findNamedParam(t);
			
			System.out.println(t_porpertyName + "  " + t_operator + "  " + t_namedParam);
		});		
	}
	public static void main(String[]args){
		testFindSimpleExpression();
	}
	@Override
	public ISqlNode singleCopy() {
		CollectConds c = new CollectConds();
		c.id(getId());
		return c;
	}
	
}
