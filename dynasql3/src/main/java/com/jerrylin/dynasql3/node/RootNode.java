package com.jerrylin.dynasql3.node;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.jerrylin.dynasql3.Aliasible;
import com.jerrylin.dynasql3.Expressible;
import com.jerrylin.dynasql3.ExpressionAliasible;
import com.jerrylin.dynasql3.ExpressionParameterizable;
import com.jerrylin.dynasql3.Filterable;
import com.jerrylin.dynasql3.Joinable;
import com.jerrylin.dynasql3.SingleChildSubquerible;
import com.jerrylin.dynasql3.SqlNodeFactory;
import com.jerrylin.dynasql3.SqlParameter;
import com.jerrylin.dynasql3.util.SqlNodeUtil;



public class RootNode extends SelectExpression<RootNode> {
	private static final long serialVersionUID = 8222624988662647761L;
	
	private LinkedHashMap<String, Object> paramValues;
	public void setParamValues(LinkedHashMap<String, Object> paramValues) {
		this.paramValues = paramValues;
	}
	public LinkedHashMap<String, Object> getParamValues(){
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
			if(paramMarkCount == 0){
				return false;
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
	String getTargetExpressionSymbol(SqlNode<?> c){
		String alias = null;
		if(ExpressionAliasible.class.isInstance(c)){
			alias = ExpressionAliasible.class.cast(c).getTargetSymbol();
		}else if(SingleChildSubquerible.class.isInstance(c)){
			alias = SingleChildSubquerible.class.cast(c).getSubqueryAlias();
		}
		return alias;
	}
	public <T extends SqlNode<?> & Joinable<?>>RootNode removeJoinTargetIfNotReferenced(){
		List<Expressible> found = // find all expressions except for those belonging to From or On
			findAll(c->{
				if(!Expressible.class.isInstance(c)
				|| From.class.isInstance(c.getParent())
				|| On.class.isInstance(c.getParent())){
					return false;
				}
				return true;
			}).stream()
			.map(c->Expressible.class.cast(c))
			.collect(Collectors.toList());
		
		Set<String> refs = found.stream() // find all used table references
			.flatMap(c->c.getTableReferences().stream())
			.collect(Collectors.toSet());
		
		List<T> removed = findAll(c->Joinable.class.isInstance(c)) // find all joinable table not referenced, prepared for removal
			.stream()
			.map(c->((T)c))
			.filter(c->{
				String alias = getTargetExpressionSymbol(c);
				if(SqlNodeUtil.isBlank(alias)){
					return false;
				}
				if(c.toStart().projectionContains(alias)){
					return false;
				}
				for(String ref : refs){
					if(ref.equals(alias)){
						return false;
					}
				}
				return true;
			})
			.collect(Collectors.toList());
		
		Collections.reverse(removed); // from back to front, from right to left
		
		removed.stream().forEach(c->{
			LinkedList<SqlNode<?>> children = c.getParent().getChildren();
			if(children.getLast() == c){ // if the join position is at last, remove it
				c.remove();
				return;
			}
			boolean referenced = false;
			int i = children.indexOf(c);
			String targetSymbol = getTargetExpressionSymbol(c);
			for(int j = (i+1); j < children.size(); j++){
				SqlNode<?> f = children.get(j);
				if(Joinable.class.isInstance(f) && Joinable.class.cast(f).getOnReferences().contains(targetSymbol)
				|| (Expressible.class.isInstance(f) && Expressible.class.cast(f).getTableReferences().contains(targetSymbol))) // for ORM e.g. HQL
				{
					referenced = true;
					break;
				}
			}
			if(!referenced){ // if the join target is not referenced by the right table, remove it 
				c.remove();
			}
		});
		return this;
	}
	public <T extends SqlNode<?> & Aliasible<?>, S extends SqlNode<?> & Filterable>RootNode moveFiltersToJoin(){
		List<S> conds = findAll(c->Filterable.class.isInstance(c))
			.stream()
			.map(c->(S)c)
			.collect(Collectors.toList());
		
		Map<String, T> targets = new LinkedHashMap<>();
		Map<String, SqlNode<?>> targetParents = new LinkedHashMap<>();
		List<T> founds = findAll(c->Aliasible.class.isInstance(c));
		for(T t : founds){
			if(Joinable.class.isInstance(t) // bypass left outer join
			&& Joinable.TYPE_LEFT_OUTER_JOIN.equals(Joinable.class.cast(t).getJoinType())){
				continue;
			}
			String symbol = null;
			if(ExpressionAliasible.class.isInstance(t)){
				symbol = ExpressionAliasible.class.cast(t).getTargetSymbol();
			}else{
				symbol = t.getAlias();
			}
			if(SqlNodeUtil.isNotBlank(symbol)){
				targets.put(symbol, t);
				targetParents.put(symbol, t.getParent());
			}
		}
		
		Map<String, SelectExpression<?>> newSubs = new LinkedHashMap<>();
		SelectExpression<?> topMost = topMost();
		for(S c : conds){
			String expr = c.getExprPart();
			List<String> refs = Expressible.findTableReferences(expr);
			if(refs.isEmpty()
			|| Collections.frequency(refs, refs.get(0)) != refs.size()){ // TODO
				continue;
			}
			SelectExpression<?> start = c.toStart();
			String ref = refs.get(0);
			for(Map.Entry<String, T> target : targets.entrySet()){
				String symbol = target.getKey();
				T t = target.getValue();
				SqlNode<?> parent = targetParents.get(symbol);
				int size = parent.getChildren().size();
				SelectExpression<?> targetStart = parent.toStart();
				
				if(ref.equals(symbol)
				&& ((start == topMost && size >= 2)
				|| start != targetStart)
				){
					SelectExpression<?> subquery = newSubs.get(symbol);
					if(SimpleExpression.class.isInstance(t) && subquery == null){
						String experssion = SimpleExpression.class.cast(t).getExpression();
						subquery = createBy(SelectExpression.class)
							.select("*")
							.fromAs(experssion, "within_" + symbol);
						subquery.as(symbol);
						t.replaceWith(subquery);
					}else if(JoinExpression.class.isInstance(t) && subquery == null){
						JoinExpression je = JoinExpression.class.cast(t);
						JoinSubquery js = je.changedWith(se->
							se.select("*")
							.fromAs(je.getExpression(), "within_" + symbol));
						subquery = js.getSubquery();
					}else if(SelectExpression.class.isInstance(t) && subquery == null){
						subquery = SelectExpression.class.cast(t);
					}
					newSubs.put(symbol, subquery);
					c.setExprPart(Expressible.getNewExprFrom(expr, subquery.getRootTargetSymbol()));
					c.remove();
					subquery.where().add(c);
					break;
				}
			}
		}
		return this;
	}
	/**
	 * replacing target symbol with extra prefix.<br>
	 * this method is just suitable for those have their own aliases as table references,<br>
	 * or it may generate not correct output.
	 * @param prefix
	 * @return
	 */
	public <T extends SqlNode<?> & Aliasible<T>>RootNode prependTargetSymbol(String prefix){
		List<T> aliases = findAll(c->Aliasible.class.isInstance(c) && SqlNodeUtil.isNotBlank(Aliasible.class.cast(c).getAlias()))
			.stream()
			.map(c->(T)c)
			.collect(Collectors.toList());
		aliases.forEach(a->a.setAlias(prefix + a.getAlias()));
		
		findAll(c->Expressible.class.isInstance(c) && !aliases.contains(c) && !From.class.isInstance(c.getParent()))
			.stream()
			.map(c->Expressible.class.cast(c))
			.forEach(c->c.prependTableReferences(prefix));
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
		
		LinkedHashMap<String, Object> vals = new LinkedHashMap<>();
		setParamValues(vals);
		
		int size = params.size();
		if(size == 0){
			return this;
		}
		for(int i = 0; i < size; i++){
			SqlParameter p = params.get(i);
			String name = p.getName();
			Object val = p.getVal();
			if(SqlNodeUtil.isBlank(name)){
				name = String.valueOf(i+1);
			}
			vals.put(name, val);
		}
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
		LinkedHashMap<String, Object> vals = new LinkedHashMap<>();
		setParamValues(vals);
		
		int size = nodes.size();
		if(size == 0){
			return this;
		}	
		int idx = 0;
		for(int i = 0; i < size; i++){
			T node = nodes.get(i);
			node.transferParamNameToQuestionMark();
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
