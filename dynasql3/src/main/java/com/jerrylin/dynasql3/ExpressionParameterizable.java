package com.jerrylin.dynasql3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.node.SqlNode;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public interface ExpressionParameterizable<Me extends SqlNode<?>> extends Expressible{
	static final Pattern FIND_NAME_PARM = Pattern.compile(":(\\w+)|\\(:(\\w+)\\)");
	static final Pattern FIND_QUESTION = Pattern.compile("\\?");
	
	public LinkedList<SqlParameter> getParams();
	public Me setParams(LinkedList<SqlParameter> params);
	public SqlParameter getCurrent();
	public Me setCurrent(SqlParameter current);
	

	default LinkedList<SqlParameter> createParamsIfNotExisted(){
		LinkedList<SqlParameter> params = getParams();
		if(params == null){
			params = new LinkedList<>();
			setParams(params);
		}
		return params;
	}
	default SqlParameter createCurrentIfNotExisted(){
		SqlParameter current = getCurrent();
		if(current != null){
			return current;
		}
		LinkedList<SqlParameter> params = createParamsIfNotExisted();
		if(params.isEmpty()){
			current = new SqlParameter();
			params.add(current);
		}else{
			current = params.getFirst();
		}
		setCurrent(current);
		return current;
	}
	/**
	 * get current specified param value
	 * @return
	 */
	default <T>T getVal(){
		return (T)createCurrentIfNotExisted().getVal();
	}
	default <T>T getVal(int idx){
		if(getParams() == null || getParams().size() < (idx+1)){
			return null;
		}
		SqlParameter current = getParams().get(idx);
		setCurrent(current);
		return (T)current.getVal();
	}
	default SqlParameter getParamAsCurrent(String name){
		SqlParameter param = findParamBy(name);
		setCurrent(param);
		return param;
	}
	default <T>T getVal(String name){
		return (T)getParamAsCurrent(name).getVal();
	}
	/**
	 * set current specified param value.<br>
	 * also see the method {@link com.jerrylin.dynasql3.util.SearchCondition.SearchByParamName#find()}.
	 * @param val
	 * @return
	 */
	default Me setVal(Object val){
		createCurrentIfNotExisted().setVal(val);
		return (Me)this;
	}
	default Me setVal(String name, Object val){
		SqlParameter param = getParamAsCurrent(name);
		param.setVal(val);
		return (Me)this;
	}
	default Me setVal(int indx, Object val){
		LinkedList<SqlParameter> params = createParamsIfNotExisted();
		int currentCount = params.size();
		int requiredCount = indx + 1;
		SqlParameter current = null;
		if(currentCount >= requiredCount){
			current = params.get(indx);
		}else{
			int stillRequired = requiredCount - currentCount;
			while(stillRequired != 0){
				SqlParameter param = new SqlParameter();
				params.add(param);
				--stillRequired;
			}
			current = params.getLast();
		}
		current.setVal(val);
		setCurrent(current);
		return (Me)this;
	}
	default List<String> getParamNames(){
		String expression = getExpression();
		Matcher m = FIND_NAME_PARM.matcher(expression);
		List<String> f = new ArrayList<>();
		while(m.find()){
			String g1 = m.group(1);
			if(SqlNodeUtil.isNotBlank(g1)){
				f.add(g1);
				continue;
			}
			String g2 = m.group(2);
			if(SqlNodeUtil.isNotBlank(g2)){
				f.add(g2);
			}
		}
		return f;
	}
	default int getParamNameCount(){
		return getParamNames().size();
	}
	default int getQuestionMarkCount(){
		String expression = getExpression();
		Matcher m = FIND_QUESTION.matcher(expression);
		int count = 0;
		while(m.find()){
			++count;
		}
		return count;
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	default SqlParameter findParamBy(String name){
		LinkedList<SqlParameter> params = createParamsIfNotExisted();
		SqlParameter f = null;
		
		if(params.isEmpty()){
			List<String> names = getParamNames();
			for(String n : names){
				SqlParameter p = new SqlParameter();
				p.setName(n);
				params.add(p);
				if(name.equals(n)){
					f = p;
				}
			}
			return f;
		}
		
		for(SqlParameter sp : params){
			if(name.equals(sp.getName())){
				f = sp;
				break;
			}
		}
		return f;
	}
	default Me compileToQuestionMark(){
		String expression = getExpression();
		String result = expression;
		Matcher m = FIND_NAME_PARM.matcher(expression);
		
		while(m.find()){
			String g = m.group();
			String name = m.group(1);
			if(name == null){
				name = m.group(2);
			}
			Object val = getVal(name);
			String replacing = "?";
			if(val != null){
				Collection<?> collect = null;
				if(Collection.class.isInstance(val)){
					collect = Collection.class.cast(val);
				}else if(val.getClass().isArray()){
					collect = Arrays.asList(((Object[])val));
				}
				if(collect != null){
					replacing = collect.stream().map(v->"?").collect(Collectors.joining(", "));
					replacing = "("+replacing+")";
				}
			}
			if(g.startsWith("(")
			&& g.endsWith(")")
			&& !replacing.startsWith("(")
			&& !replacing.endsWith(")")){
				replacing = "("+replacing+")";
			}
			result = result.replace(g, replacing);
		}
		setExpression(result);
		return (Me)this;
	}
}
