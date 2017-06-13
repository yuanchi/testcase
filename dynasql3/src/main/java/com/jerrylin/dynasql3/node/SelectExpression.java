package com.jerrylin.dynasql3.node;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.Aliasible;
import com.jerrylin.dynasql3.Expressible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;
// TODO Set operation??
public class SelectExpression<S extends SelectExpression<?>> extends SqlNode<S> implements Aliasible<S> {
	private static final long serialVersionUID = -707634325499457067L;
	private static final List<Class<? extends SqlNode<?>>> ORDERS = 
		Arrays.asList(Select.class, From.class, Where.class, GroupBy.class, Having.class, OrderBy.class);
	private String alias;
	@Override
	public String getAlias() {
		return alias;
	}
	@Override
	public void setAlias(String alias) {
		this.alias = alias;
	}
	/**
	 * get existed one, or create instance with key class
	 * @param key
	 * @return
	 */
	private <T extends SqlNode<?>> T createIfNotExisted(Class<T> key){
		SqlNode<?> found = null;
		for(SqlNode<?> c : getChildren()){
			if(key.isInstance(c)){
				found = c;
				break;
			}
		}
		if(found == null){
			found = createBy(key);
			add(found);
		}
		return (T)found;
	}
	
	public Select select(){
		Select select = createIfNotExisted(Select.class);
		return select;
	}
	public S select(String...expressions){
		Select select = select();
		for(String expression : expressions){
			select.t(expression);
		}
		return thisType();
	}
	public S select(Consumer<Select> config){
		Select select = select();
		select.config(config);
		return thisType();
	}
	
	public From from(){
		From from = createIfNotExisted(From.class);
		return from;
	}
	public S from(String ...expressions){
		From from = from();
		for(String expression : expressions){
			from.t(expression);
		}
		return thisType();
	}
	public S from(Consumer<From> config){
		From from = from();
		from.config(config);
		return thisType();
	}
	public S fromAs(String expression, String alias){
		From from = from();
		from.t(expression).as(alias);
		return thisType();
	}
	
	public Where where(){
		Where where = createIfNotExisted(Where.class);
		return where;
	}
	public S where(String...conditions){
		Where where = where();
		for(String condition : conditions){
			where.and(condition);
		}
		return thisType();
	}
	public S where(Consumer<Where>config){
		Where where = where();
		where.config(config);
		return thisType();
	}
	
	public OrderBy orderBy(){
		OrderBy orderBy = createIfNotExisted(OrderBy.class);
		return orderBy;
	}
	public S orderBy(String...expressions){
		OrderBy orderBy = orderBy();
		for(String expression : expressions){
			orderBy.t(expression);
		}
		return thisType();
	}
	public S orderBy(Consumer<OrderBy> config){
		OrderBy orderBy = orderBy();
		orderBy.config(config);
		return thisType();
	}
	public S orderByWith(String expression, String direction){
		OrderBy orderBy = orderBy();
		orderBy.t(expression).direct(direction);
		return thisType();
	}
	
	public GroupBy groupBy(){
		GroupBy groupBy = createIfNotExisted(GroupBy.class);
		return groupBy;
	}
	public S groupBy(String...expressions){
		GroupBy groupBy = createIfNotExisted(GroupBy.class);
		for(String expression : expressions){
			groupBy.t(expression);
		}
		return thisType();
	}
	public S groupBy(Consumer<GroupBy> config){
		GroupBy groupBy = groupBy();
		groupBy.config(config);
		return thisType();
	}
	
	public Having having(){
		Having having = createIfNotExisted(Having.class);
		return having;
	}
	public S having(String...expressions){
		Having having = having();
		for(String expression : expressions){
			having.and(expression);
		}
		return thisType();
	}
	public S having(Consumer<Having> config){
		Having having = having();
		having.config(config);
		return thisType();
	}
	
	private static int readingPriority(SqlNode<?> input1, SqlNode<?> input2){
		int size = ORDERS.size();
		int order1 = size;
		int order2 = size;
		for(int i = 0; i < size; i++){
			Class<? extends SqlNode<?>> f = ORDERS.get(i);
			if(f.isInstance(input1)){
				order1 = i;
			}
			if(f.isInstance(input2)){
				order2 = i;
			}
		}
		return order1 - order2;
	}
	S sort(){
		Collections.sort(getChildren(), (o1, o2)->{
			return readingPriority(o1, o2);
		});
		return thisType();
	}
	/**
	 * return alias or expression.
	 * @return
	 */
	public String getRootTargetSymbol(){
		LinkedList<SqlNode<?>> children = from().getChildren();
		if(children.isEmpty()){
			throw new RuntimeException("child SqlNode as target NOT FOUND");
		}
		SimpleExpression se = ((SimpleExpression)children.getFirst());
		String symbol = se.getTargetSymbol();
		return symbol;
	}
	@Override
	public S copy(){
		S copy = super.copy();
		copy.setAlias(alias);
		return copy;
	}
	@Override
	public String toSql(){
		List<SqlNode<?>> children = getChildren();
		if(children.size() == 0){
			return "";
		}
		boolean hasParent = getParent() != null;
		String sep = "\n";
//		if(hasParent){
//			sep = " " + sep;
//		}
		sort();
		String result = children.stream().map(c->c.toSql()).filter(s->SqlNodeUtil.isNotBlank(s)).collect(Collectors.joining(sep));
		if(hasParent){
			result = "("+result+")";
		}
		if(SqlNodeUtil.isNotBlank(alias)){
			result = result + " AS " + alias;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		List<SqlNode<?>> children = getChildren();
		if(children.size() == 0){
			return "";
		}
		boolean hasParent = getParent() != null;
		String preIndent = hasParent ? " " + indent : indent;
		sort();
		String result = children.stream().map(c->c.toSqlWith(preIndent)).filter(s->SqlNodeUtil.isNotBlank(s)).collect(Collectors.joining("\n"));
		result = SqlNodeUtil.trimLeading(result); // remove first indent
		if(hasParent){
			result = "("+result+")";
		}
		if(SqlNodeUtil.isNotBlank(alias)){
			result = result + " AS " + alias;
		}
		return result;
	}
	/**
	 * check if specified table reference is in projections<br>
	 * if projections contain '*', always return true
	 * @param tableRef
	 * @return
	 */
	public boolean projectionContains(String tableRef){
		for(SqlNode<?> c : getChildren()){
			if(Expressible.class.isInstance(c)){
				Expressible e = Expressible.class.cast(c);
				if("*".equals(e.getExpression())
				|| tableRef.equals(e.getExpression()) // suitable for ORM representation
				|| e.getTableReferences().contains(tableRef)){
					return true;
				}
			}
		}
		return false;
	}
}
