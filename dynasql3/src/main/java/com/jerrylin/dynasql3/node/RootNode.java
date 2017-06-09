package com.jerrylin.dynasql3.node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.ExpressionParameterizable;
import com.jerrylin.dynasql3.SqlNodeFactory;
import com.jerrylin.dynasql3.SqlParameter;
import com.jerrylin.dynasql3.util.SqlNodeUtil;



public class RootNode extends SelectExpression<RootNode> {
	private static final long serialVersionUID = 8222624988662647761L;
	
	private Map<String, Object> paramValues;
	public void setParamValues(Map<String, Object> paramValues) {
		this.paramValues = paramValues;
	}
	public Map<String, Object> getParamValues(){
		return paramValues;
	}
	@Override
	public RootNode add(SqlNode<?> child){
		return (RootNode)super.add(child);
	}
	public boolean withAnyParamName(){
		SqlNode<?> f = findFirst(
			c -> 
				ExpressionParameterizable.class.isInstance(c) 
				&& ExpressionParameterizable.class.cast(c).getParamNameCount() > 0);
		return f != null;
	}
	public RootNode removeIfParamValNotExisted(){
		findAll(c->{
			if(!ExpressionParameterizable.class.isInstance(c)){
				return false;
			}
			ExpressionParameterizable<?> ep = ExpressionParameterizable.class.cast(c);
			List<SqlParameter> params = ep.getParams();
			int paramMarkCount = ep.getParamNameCount();
			if(paramMarkCount == 0){
				paramMarkCount = ep.getQuestionMarkCount();
			}
			if(params == null || params.isEmpty() || params.size() != paramMarkCount){
				return true;
			}
			// if a expression is mapped to multiple parameters, any not existing value can cause the node bypassed
			for(SqlParameter sp : params){
				Object val = sp.getVal();
				if(val == null
				|| (String.class.isInstance(val) && SqlNodeUtil.isBlank(String.class.cast(val).trim()))
				|| (Collection.class.isInstance(val) && Collection.class.cast(val).isEmpty())
				|| (Map.class.isInstance(val) && Map.class.cast(val).isEmpty())){
					return true;
				}
			}
			return false;
		}).stream()
		.forEach(c->c.getParent().getChildren().remove(c));
		return this;
	}
	// TODO
	public RootNode removeFromTargetIfNotReferenced(){
		
		return this;
	}
	<T extends SqlNode<?> & ExpressionParameterizable<T>> List<T> getParameterizableNodes(){
		List<T> collect = findAll(n -> ExpressionParameterizable.class.isInstance(n) && ExpressionParameterizable.class.cast(n).getParams() != null);
		return collect;
	}
	public <T extends SqlNode<?> & ExpressionParameterizable<T>>List<SqlParameter> getParameters(){
		List<T> collect = getParameterizableNodes();
		List<SqlParameter> params = collect.stream().flatMap(n->n.getParams().stream()).collect(Collectors.toList());
		return params;
	}
	public RootNode withParamValues(){
		List<SqlParameter> params = getParameters();
		int size = params.size();
		if(size == 0){
			setParamValues(Collections.emptyMap());
			return this;
		}		
		Map<String, Object> vals = new LinkedHashMap<>();
		for(int i = 0; i < size; i++){
			SqlParameter p = params.get(i);
			String name = p.getName();
			Object val = p.getVal();
			if(SqlNodeUtil.isBlank(name)){
				name = String.valueOf(i+1);
			}
			vals.put(name, val);
		}
		setParamValues(vals);
		return this;
	}
	/**
	 * this method may change expression content.<br>
	 * if you want to keep the original node unchanged,<br>
	 * you can firstly call copy() before invoking this method.
	 * @return
	 */
	public <T extends SqlNode<?> & ExpressionParameterizable<T>> RootNode withParamValuesAfterCompiling(){
		List<T> nodes = getParameterizableNodes();
		int size = nodes.size();
		if(size == 0){
			setParamValues(Collections.emptyMap());
			return this;
		}
		Map<String, Object> vals = new LinkedHashMap<>();
		int idx = 0;
		for(int i = 0; i < size; i++){
			T node = nodes.get(i);
			node.compileToQuestionMark();
			List<SqlParameter> params = node.getParams();
			for(int j = 0; j < params.size(); j++){
				SqlParameter sp = params.get(j);
				Object val = sp.getVal();
				if(val == null
				|| (val != null && !Collection.class.isInstance(val) && !val.getClass().isArray())){
					++idx;
					vals.put(String.valueOf(idx), val);
				}else{
					Collection<?> collect = null;
					if(Collection.class.isInstance(val)){
						collect = Collection.class.cast(val);
					}else if(val.getClass().isArray()){
						collect = Arrays.asList((Object[])val);
					}
					if(collect != null){
						Iterator<?> itr = collect.iterator();
						while(itr.hasNext()){
							++idx;
							Object element = itr.next();
							vals.put(String.valueOf(idx), element);
						}
					}
				}
			}
		}
		if(vals.isEmpty()){
			vals = Collections.emptyMap();
		}
		setParamValues(vals);
		return this;
	}
	
	/**
	 * create root node with a singleton factory.<br>
	 * if a mutable factory is needed, reassign a mutable instance with SqlNodeFactory.createInstance() 
	 * @return
	 */
	public static RootNode create(){
		SqlNodeFactory factory = SqlNodeFactory.getSingleton();
		RootNode root = factory.create(RootNode.class);
		root.setFactory(factory);
		return root;
	}
	public static RootNode createWith(SqlNodeFactory factory){
		RootNode root = factory.create(RootNode.class);
		root.setFactory(factory);
		return root;
	}
	@Override
	public RootNode copy(){
		RootNode copy = (RootNode)super.copy();
		if(paramValues != null){
			copy.setParamValues(new LinkedHashMap<>(paramValues));
		}
		return copy;
	}
}
