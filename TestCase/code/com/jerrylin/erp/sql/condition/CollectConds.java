package com.jerrylin.erp.sql.condition;

import static com.jerrylin.erp.sql.Instruction.CONTAIN_LIKE;
import static com.jerrylin.erp.sql.Instruction.END_LIKE;
import static com.jerrylin.erp.sql.Instruction.START_LIKE;
import static com.jerrylin.erp.sql.Instruction.UPPERCASE;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.sql.condition.StrCondition.MatchMode;

/**
 * representing a set conditions
 * @author JerryLin
 *
 */
public class CollectConds extends SqlCondition{
	private static final long serialVersionUID = 3839880394325350005L;
	// Pattern is immutable and thread-safe
	private static final Pattern FIND_PROPERTY_NAME = Pattern.compile("[a-zA-Z0-9\\.\\(]+\\.[a-zA-Z0-9\\[\\]\\.\\)]+");
	private static final Pattern FIND_OPERATOR = Pattern.compile("(\\s+(IN|in|LIKE|like|NOT\\s+LIKE)\\s+)|\\s*(\\>\\=|\\<\\=|\\!\\=|\\=|\\<\\>|\\>|\\<)\\s*");
	private static final Pattern FIND_NAMED_PARAM_STEP1 = Pattern.compile("(\\:|\\(\\:){1}[\\w\\[\\]]+\\)?");
	private static final Pattern FIND_NAMED_PARAM_STEP2 = Pattern.compile("([^\\:]|[^(\\(\\:)]){1}[\\w\\[\\]]+[^\\)]?");
	
	private String makeGroupMark;
	/**
	 * 啟動群組標記，這會在新增條件的時候，標明哪些條件屬於特定群組，使用完畢後應呼叫disableGroupMark，解除標記狀態
	 * @param groupMark
	 */
	public void enableGroupMark(String groupMark){
		this.makeGroupMark = groupMark;
	}
	public void disableGroupMark(){
		this.makeGroupMark = null;
	}
	private void makeGroupMarkIfEnabled(ISqlCondition cond){
		if(StringUtils.isNotBlank(makeGroupMark)){
			cond.groupMark(makeGroupMark);
		}
	}
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
	private SimpleCondition initSimpleCond(SimpleCondition cond, String expression, Class<?> type, Object val){
		String propertyName = findPropertyName(expression);
		String operator = findOperator(expression);
		String namedParam = findNamedParam(expression);
//		System.out.println("propertyName:"+propertyName + ", operator: " + operator + ", namedParam: " + namedParam);
		SimpleCondition s = cond.propertyName(propertyName)
								.operator(operator)
								.type(type)
								.value(val);		
		s.id(namedParam);
		addChild(s);
		return s;
	}
	private SimpleCondition simpleCond(String expression, Class<?> type, Object val){
		return initSimpleCond(new SimpleCondition(), expression, type, val);
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
	public void addChild(ISqlNode node){
		if(node instanceof ISqlCondition){
			ISqlCondition cond = (ISqlCondition)node;
			makeGroupMarkIfEnabled(cond);
		}
		super.addChild(node);
	}
	private String getLikePattern(MatchMode matchMode){
		if(MatchMode.START == matchMode){
			return START_LIKE;
		}else if(MatchMode.END == matchMode){
			return END_LIKE;
		}else if(MatchMode.ANYWHERE == matchMode){
			return CONTAIN_LIKE;
		}
		return null;
	}
	private StrCondition strCondition(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = new StrCondition();
		strCondition.setMatchMode(matchMode);
		addInstruction(strCondition, getLikePattern(matchMode));
		initSimpleCond(strCondition, expression, String.class, value);
		return strCondition;
	}
	
	public CollectConds andStrCondition(String expression, MatchMode matchMode){
		andStrCondition(expression, matchMode, null);
		return this;
	}
	
	public CollectConds andStrCondition(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = strCondition(expression, matchMode, value);
		strCondition.junction(Junction.AND);
		return this;
	}
	
	public CollectConds orStrCondition(String expression, MatchMode matchMode){
		orStrCondition(expression, matchMode, null);
		return this;
	}
	
	public CollectConds orStrCondition(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = strCondition(expression, matchMode, value);
		strCondition.junction(Junction.OR);
		return this;
	}
	
	private void addInstruction(SimpleCondition s, String instruction){
		String oriInstruction = s.getInstruction();
		String newInstruction = "";
		if(StringUtils.isNotBlank(oriInstruction)){
			newInstruction = (oriInstruction + "|"); 
		}
		newInstruction += instruction;
		s.instruction(newInstruction);
	}
	
	private StrCondition strToUpperCase(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = strCondition(expression, matchMode, value);
		addInstruction(strCondition, UPPERCASE);
		strCondition.setCaseInsensitive(true);
		return strCondition;
	}
	
	public CollectConds andStrToUpperCase(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = strToUpperCase(expression, matchMode, value);
		strCondition.junction(Junction.AND);
		return this;
	}
	
	public CollectConds andStrToUpperCase(String expression, MatchMode matchMode){
		andStrToUpperCase(expression, matchMode, null);
		return this;
	}
	
	public CollectConds orStrToUpperCase(String expression, MatchMode matchMode, String value){
		StrCondition strCondition = strToUpperCase(expression, matchMode, value);
		strCondition.junction(Junction.OR);
		return this;
	}
	
	public CollectConds orStrToUpperCase(String expression, MatchMode matchMode){
		orStrToUpperCase(expression, matchMode, null);
		return this;
	}
	
	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
			.map(ISqlNode::genSql)
			.collect(Collectors.toList());
		if(items.size() == 0){
			return "";
		}
		String result = StringUtils.join(items, "\n      "); 
		String and = Junction.AND.toString();
		if(result.indexOf(and) == 0){
			result = result.substring(4, result.length());
		}
		String or = Junction.OR.toString();
		if(result.indexOf(or) == 0){
			result = result.substring(3, result.length());
		}
		if(items.size() > 1){
			result = "(" + result + ")";
		}
		return getJunction().getSymbol() + " " + result;
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
		c.groupMark(getGroupMark());
		c.makeGroupMark = makeGroupMark; // TODO 理論上，使用完畢應該就要呼叫disableGroupMark除去暫存的makeGroupMark，所以這邊不應該有值
		c.junction(getJunction());
		return c;
	}
	
}
