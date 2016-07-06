package com.jerrylin.query;

import static com.jerrylin.query.Expression.FILTER_CONDITION_END;
import static com.jerrylin.query.Expression.FILTER_CONDITION_PART;
import static com.jerrylin.query.Expression.FILTER_CONDITION_START;
import static com.jerrylin.query.Expression.FILTER_SELECT;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class FilterCondition extends SqlNode {
	static final String VALUE_ADDED_WHEN_GEN_SQL = "valueAddedWhenGenSql";
	
	static final Pattern FILTER_THREE_PARTS = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(\\S+)");
	static final Pattern FILTER_TWO_PARTS = Pattern.compile("(\\S+)\\s+(\\S+)\\s+");
	
	static final String VALUE_FEATURES = "valueFeatures";
	
	public FilterCondition content(String content, Object val){
		content(content);
		value(val);
		return this;
	}	
	public FilterCondition content(String content){
		String start = null;
		String operator = null;
		String end = null;
		
		Matcher m = FILTER_THREE_PARTS.matcher(content);
		while(m.find()){
			start = m.group(1);
			operator = m.group(2);
			end = m.group(3);
		}
		if(StringUtils.isBlank(start)){
			m = FILTER_TWO_PARTS.matcher(content);
			while(m.find()){
				start = m.group(1);
				operator = m.group(2);
			}
		}
		
		this.start(start)
			.operator(operator);
		
		if(StringUtils.isNotBlank(end)){
			this.end(end);
			if(end.indexOf(":") == 0){
				this.attr(ID, end.replace(":", ""));
			}
		}
		return this;
	}
	/**
	 * string condition value, ex: "Jo%"
	 * @param content: condition statement, ex: "m.name LIKE ?", or "p.code LIKE :code"
	 * @param val: suggesting string type
	 * @return
	 */
	public FilterCondition strStartWith(String content, String val){
		this.content(content)
			.value(val)
			.valueFeatures(ValueFeatures.newInstance().startWith());
		return this;
	}
	/**
	 * see the method {@link #strStartWith(String content, String val)}.
	 * @param content
	 * @return
	 */
	public FilterCondition strStartWith(String content){
		this.content(content)
			.valueFeatures(ValueFeatures.newInstance().startWith());
		return this;
	}
	/**
	 * string condition value, ex: "%xxx%"
	 * also see the method {@link #strStartWith(String content, String val)}.
	 * @param content
	 * @param val
	 * @return
	 */
	public FilterCondition strContain(String content, String val){
		this.content(content)
			.value(val)
			.valueFeatures(ValueFeatures.newInstance().contain());
		return this;
	}
	/**
	 * see the method {@link #strContain(String content, String val)}.
	 * @param content
	 * @return
	 */
	public FilterCondition strContain(String content){
		this.content(content)
			.valueFeatures(ValueFeatures.newInstance().contain());
		return this;
	}
	/**
	 * string condition value, ex: "%ry"
	 * also see the method {@link #strStartWith(String content, String val)}.
	 * @param content
	 * @param val
	 * @return
	 */
	public FilterCondition strEndWith(String content, String val){
		this.content(content)
			.value(val)
			.valueFeatures(ValueFeatures.newInstance().endWith());
		return this;		
	}
	/**
	 * see the method {@link #strEndWith(String content, String val)}.
	 * @param content
	 * @return
	 */
	public FilterCondition strEndWith(String content){
		this.content(content)
			.valueFeatures(ValueFeatures.newInstance().endWith());
		return this;		
	}
	/**
	 * string condition value, ex: "Bob"
	 * also see the method {@link #strStartWith(String content, String val)}.
	 * @param content
	 * @param val
	 * @return
	 */
	public FilterCondition strExact(String content, String val){
		this.content(content)
			.value(val)
			.valueFeatures(ValueFeatures.newInstance().exact());
		return this;
	}
	/**
	 * see the method {@link #strExact(String content, String val)}.
	 * @param content
	 * @return
	 */
	public FilterCondition strExact(String content){
		this.content(content)
			.valueFeatures(ValueFeatures.newInstance().exact());
		return this;
	}
	public FilterCondition strIgnoreCase(String content, String val){
		this.content(content)
			.value(val)
			.valueFeatures(ValueFeatures.newInstance().ignoreCase());
		return this;		
	}
	public FilterCondition strIgnoreCase(String content){
		this.content(content)
			.valueFeatures(ValueFeatures.newInstance().ignoreCase());
		return this;		
	}	
	public FilterCondition start(String desc){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(desc);
		se.attr(FILTER_CONDITION_PART, FILTER_CONDITION_START);
		addChildren(se);
		return this;
	}
	public FilterCondition operator(String symbol){
		Operator o = new Operator();
		o.setSymbol(symbol);
		addChildren(o);
		return this;
	}
	public FilterCondition end(String desc){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(desc);
		se.attr(FILTER_CONDITION_PART, FILTER_CONDITION_END);
		addChildren(se);
		return this;
	}
	public SelectExpression endWithSelect(){
		SelectExpression se = new SelectExpression();
		se.attr(FILTER_CONDITION_PART, FILTER_SELECT);
		addChildren(se);
		return se;
	}
	public FilterCondition value(Object val){
		getChildren().removeIf(sn->(sn instanceof ConditionValue));
		ConditionValue cv = new ConditionValue();
		cv.setVal(val);
		addChildren(cv);
		return this;
	}
	public ConditionValue value(){
		Optional<SqlNode> result = getChildren().stream().filter(n->ConditionValue.class.isInstance(n)).findFirst();
		if(result.isPresent()){
			return (ConditionValue)result.get();
		}
		return null;
	}
	public FilterCondition valueFeatures(ValueFeatures valueFeatures){
		if(valueFeatures != null){
			this.attr(VALUE_FEATURES, StringUtils.join(valueFeatures.getFeatures(), ","));
		}
		return this;
	}
	public ValueFeatures getValueFeatures(){
		String vf = this.attr(VALUE_FEATURES);
		if(StringUtils.isBlank(vf)){
			return ValueFeatures.EMPTY;
		}
		return ValueFeatures.newInstance(vf);
	}
	/**
	 * when generating sql, default filter out ConditionValue node
	 */
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().filter(sn->!(sn instanceof ConditionValue) || isValueAddedWhenGenSql()).map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String condition = StringUtils.join(map, " ");
			return condition;
		}
		return "";
	}
	@Override
	public FilterCondition newInstance(){
		return new FilterCondition();
	}
	public boolean isValueAddedWhenGenSql(){
		return "true".equals(attr(VALUE_ADDED_WHEN_GEN_SQL));
	}
	public void setValueAddedWhenGenSql(boolean added){
		attr(VALUE_ADDED_WHEN_GEN_SQL, Boolean.valueOf(added).toString());
	}
	public FilterCondition valueExpected(){
		valueFeatures(ValueFeatures.newInstance().valueExpected());
		return this;
	}
	public FilterCondition valueExpected(String content){
		content(content)
		.valueFeatures(ValueFeatures.newInstance().valueExpected());
		return this;
	}
	public FilterCondition valueExpected(String content, Object val){
		content(content)
		.value(val)
		.valueFeatures(ValueFeatures.newInstance().valueExpected());
		return this;
	}
}
