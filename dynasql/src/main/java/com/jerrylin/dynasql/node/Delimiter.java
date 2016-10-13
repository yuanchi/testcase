package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.UUID;
import java.util.stream.Collectors;

import com.jerrylin.dynasql.Dependable;
import com.jerrylin.dynasql.InParameter;

/**
 * parenthesis representing a operation unit that may contain multiple units
 * @author JerryLin
 *
 * @param <T>
 */
public class Delimiter<T extends Delimiter<?>> extends SqlNode<T> implements Dependable{
	private static final long serialVersionUID = 4018424127629487536L;
	private String dependencyMark;
	private String dependencyUUID;
	/**
	 * add SimpleCondition as child node
	 * @param expression
	 * @return
	 */
	private SimpleCondition addCondition(String expression){
		SimpleCondition sc = new SimpleCondition();
		sc.setExpression(expression);
		add(sc);
		return sc;
	}
	/**
	 * add SimpleCondition as child node<br> 
	 * @param expression
	 * @return T extends Delimiter
	 */
	public T cond(String expression){
		addCondition(expression);
		return getThisType();
	}
	/**
	 * add SubqueryCondition as child node<br>
	 * @return
	 */
	public SubqueryCondition subqueryCond(){
		SubqueryCondition sc = new SubqueryCondition();
		add(sc);
		return sc;
	}
	/**
	 * this method represents the parenthesis to group the operating unit<br>
	 * add Delimiter as child node
	 * @return
	 */
	public Delimiter<?> delimiter(){
		Delimiter<?> gc = new Delimiter<>();
		add(gc);
		return gc;
	}
	/**
	 * add Operator as child node
	 * @param symbol
	 * @return T extends Delimiter
	 */
	public T oper(String symbol){
		Operator o = new Operator();
		o.setSymbol(symbol);
		add(o);
		return getThisType();
	}
	/**
	 * add 'and' Operator as child node if first child existed<br>
	 * add SimpleCondition as child node
	 * @param expression
	 * @return T extends Delimiter
	 */
	public T and(String expression){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("AND");
		T t = cond(expression);
		dependencyEnd();
		return t;
	}
	/**
	 * add 'and' Operator as child node if first child existed<br>
	 * add SubqueryCondition as child node
	 * @param expression
	 * @return
	 */
	public SubqueryCondition andSubqueryCond(){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("AND");
		SubqueryCondition t = subqueryCond();
		dependencyEnd();
		return t;
	}
	/**
	 * this method represents the parenthesis to group the operating unit<br> 
	 * add 'and' Operator as child node if first child existed<br>
	 * add Delimiter as child node
	 * @param expression
	 * @return
	 */	
	public Delimiter<?> andDelimiter(){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("AND");
		Delimiter<?> t = delimiter();
		dependencyEnd();
		return t;
	}
	/**
	 * add 'or' Operator as child node if first child existed<br>
	 * add SimpleCondition as child node
	 * @param expression
	 * @return T extends Delimiter
	 */
	public T or(String expression){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("OR");
		T t = cond(expression);
		dependencyEnd();
		return t;
	}
	/**
	 * add 'or' Operator as child node if first child existed<br>
	 * add SubqueryCondition as child node
	 * @param expression
	 * @return
	 */
	public SubqueryCondition orSubqueryCond(){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("OR");
		SubqueryCondition t = subqueryCond();
		dependencyEnd();
		return t;
	}
	/**
	 * this method represents the parenthesis to group the operating unit<br> 
	 * add 'or' Operator as child node if first child existed<br>
	 * add Delimiter as child node
	 * @param expression
	 * @return
	 */	
	public Delimiter<?> orDelimiter(){
		dependencyStart();
		addOperatorIfPreviousConditionExisted("OR");
		Delimiter<?> t = delimiter();
		dependencyEnd();
		return t;
	}
	private boolean addOperatorIfPreviousConditionExisted(String symbol){
		LinkedList<SqlNode<?>> children = getChildren();
		if(!children.isEmpty()){
			oper(symbol);
			return true;
		}
		return false;
	}
	/**
	 * add id to last condition node
	 * @param id
	 * @return
	 */
	public T cId(String id){
		return lcId(id);
	}
	/**
	 * add parameter value to last condition node
	 * @param val
	 * @return
	 */
	public T cParamValue(Object val){
		return lcParamValue(val);
	}
	@Override
	public String genSql(){
		LinkedList<SqlNode<?>> children = getChildren();
		String result = "";
		if(children.size()>0){
			result = children.stream().map(n->n.genSql()).collect(Collectors.joining(" "));
			result = "(" + result + ")";
		}
		return result;
	}
	@Override
	public T copy(SqlNode<?>parent){
		Delimiter<?> gc = new Delimiter<>();
		gc.setDependencyMark(dependencyMark);
		T t = (T)gc;
		if(parent!=null){
			parent.addCopy(t);
		}
		copyTo(t);
		return t;
	}
	@Override
	public void setDependencyMark(String dm) {
		this.dependencyMark = dm;
	}
	@Override
	public String getDependencyMark() {
		return dependencyMark;
	}
	@Override
	public T add(SqlNode<?> c){
		if(this.dependencyUUID != null && Dependable.class.isInstance(c)){
			Dependable d = Dependable.class.cast(c);
			d.setDependencyMark(dependencyUUID);
		}
		return super.add(c);
	}
	public T dependencyStart(){
		this.dependencyUUID = UUID.randomUUID().toString();
		return getThisType();
	}
	public T dependencyEnd(){
		this.dependencyUUID = null;
		return getThisType();
	}
}
