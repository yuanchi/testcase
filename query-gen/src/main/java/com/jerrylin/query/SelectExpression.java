package com.jerrylin.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
/**
 * representing the unit or starting point of select expression
 * @author JerryLin
 *
 */
public class SelectExpression extends Expression {
	static final String INDENT = "indent";
	static final String ALIAS = "alias";
	
	public SelectExpression(){
		setIndent(baseIndent);
	}
	public String getAlias() {
		return attr(ALIAS);
	}
	public void setAlias(String alias) {
		attr(ALIAS, alias);
	}
	public String getIndent(){
		return attr(INDENT);
	}
	public void setIndent(String indent){
		attr(INDENT, indent);
	}
	public Select select(){
		Select select = new Select();
		addChildren(select);
		return select;
	}
	public From from(){
		From from = new From();
		addChildren(from);
		return from;
	}
	public Where where(){
		Where where = new Where();
		addChildren(where);
		return where;
	}
	public OrderBy orderBy(){
		OrderBy orderBy = new OrderBy();
		addChildren(orderBy);
		return orderBy;
	}
	@Override
	public String genSql(){
		String indent = getIndent();
		String separator = "\n" + indent;
		Object[] map = getChildren().stream().map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String result = StringUtils.join(map, separator);
			if(getParent()!=null){
				String alias = getAlias();
				result  = "(" + result + ")";
				if(StringUtils.isNotBlank(alias)){
					result += (" " + alias);
				}
				return result;
			}
			return result;
		}
		return "";
	}
	@Override
	public SelectExpression newInstance(){
		return new SelectExpression();
	}
	public Map<Integer, Object> conditionIndexValuePairs(){
		
		List<Object> values = 
			findByType(ConditionValue.class)
			.getFound(ConditionValue.class)
			.stream()
			.map(n->n.getVal())
			.collect(Collectors.toList());
		
		Map<Integer, Object> indexedParams = 
			IntStream.range(0, values.size())
			.boxed()
			.collect(Collectors.toMap(i->i, values::get));
		
		return indexedParams;
	}
	public Map<String, Object> conditionIdValuePairs(){
		
		Map<String, Object> idParams = 
			findByType(FilterCondition.class)
			.getFound(FilterCondition.class)
			.stream()
			.collect(Collectors.toMap(n->n.attr(ID), n->n.value().getVal()));
		
		return idParams;
	}
	public static SelectExpression init(){
		SelectExpression expression = new SelectExpression();
		expression.attr(NODE_ROLE, ROOT);
		return expression;
	}
	public SelectExpression filterOut(Predicate<SqlNode>predicate){
		find(predicate).remove(sn->{
			if(sn instanceof FilterCondition){
				FilterCondition fc = FilterCondition.class.cast(sn);
				SqlNode p = fc.getParent();
				List<SqlNode> children = p.getChildren();
				int size = children.size();
				int idx = children.indexOf(fc);
				int associateIdx = idx == 0 ? 1 : idx-1;
				
				if(associateIdx >= size){
					return;
				}
				SqlNode associated = children.get(associateIdx); 
				if(associated instanceof Operator){
					children.remove(associated);
				}
			}
		});
		removeNotRequiredSiblings();
		return this;
	}
	public SelectExpression defaultFilterOut(){
		return filterOut(defaultFilterOutPredicate());
	}
	public SelectExpression changeNode(Consumer<SqlNode> consumer){
		iterate().transform(consumer);
		return this;
	}
	public SelectExpression defaultChangeNode(){
		return changeNode(defaultChangeNodeConsumer());
	}
	/**
	 * using each main sql node string for convenience
	 * @return
	 */
	public SqlGenerator getSqlGenerator(){
		SqlGenerator sg = new SqlGenerator(this);
		return sg;
	}
	public static Predicate<SqlNode> defaultFilterOutPredicate(){
		final boolean EXCLUDED = true;
		final boolean INCLUDED = false;
		return new Predicate<SqlNode>(){
			public boolean test(SqlNode node){
				if(node instanceof FilterCondition){
					FilterCondition fc = FilterCondition.class.cast(node);
					if(!fc.getValueFeatures().isValueExpected()){
						return INCLUDED;
					}
					ConditionValue cv = fc.value();
					if(cv == null){
						return EXCLUDED;
					}
					Object val = cv.getVal();
					if(val == null){
						return EXCLUDED;
					}
					if(val instanceof String){
						String str = String.class.cast(val);
						if(StringUtils.isBlank(str)){
							return EXCLUDED;
						}
					}else if(val instanceof Collection){
						Collection<?> col = Collection.class.cast(val);
						if(col.isEmpty()){
							return EXCLUDED;
						}
					}else if(val.getClass().isArray()){
						Object[] array = (Object[])val;
						if(array.length == 0){
							return EXCLUDED;
						}
					}
				}
				return INCLUDED;
			}
		};
	}
	public static Consumer<SqlNode> defaultChangeNodeConsumer(){
		return new Consumer<SqlNode>(){
			public void accept(SqlNode sn){
				if(sn instanceof FilterCondition){
					FilterCondition fc = FilterCondition.class.cast(sn);
					String features = fc.attr(FilterCondition.VALUE_FEATURES);
					if(features!=null){
						ValueFeatures vf = ValueFeatures.newInstance(features);
						if(vf.isStartWith()){
							fc.value(fc.value().getVal() + "%");
						}
						if(vf.isContain()){
							fc.value("%" + fc.value().getVal() + "%");
						}
						if(vf.isEndWith()){
							fc.value("%" + fc.value().getVal());
						}
						if(vf.isIgnoreCase()){
							SimpleExpression se = fc.findChildExact(SimpleExpression.class);
							se.setExpression("UPPER("+ se.getExpression()+")");
							fc.value(((String)fc.value().getVal()).toUpperCase());
						}
					}
					
				}
			}
		};
	}
	/**
	 * see the method {@link GroupConditions#removeNotRequiredSiblings(GroupConditions gc)}.
	 * @return
	 */
	public void removeNotRequiredSiblings(){
		Where where = findChildExact(Where.class);
		if(where != null){
			GroupConditions.removeNotRequiredSiblings(where);
		}
		Having having = findChildExact(Having.class);
		if(having != null){
			GroupConditions.removeNotRequiredSiblings(having);
		}
		findByType(TargetSelectable.class).getFound(TargetSelectable.class).forEach(ts->ts.removeNotRequiredSiblings());
	}
}
