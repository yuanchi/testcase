package com.jerrylin.dynasql.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jerrylin.dynasql.Aliasible;
import com.jerrylin.dynasql.Expressible;

public class From extends TargetExpressible<From> {
	private static final long serialVersionUID = 3423825780246455247L;
	private LinkedList<String> shouldBeReferencedAliases;
	public From addJoin(String joinType){
		Join join = new Join();
		join.setJoinType(joinType);
		add(join);
		return this;
	}
	public From join(){
		return addJoin("JOIN");
	}
	public From join(String target){
		return join()
				.t(target);
	}
	public From leftOuterJoin(){
		return addJoin("LEFT OUTER JOIN");
	}
	public From leftOuterJoin(String target){
		return leftOuterJoin()
				.t(target);
	}
	public From rightOuterJoin(){
		return addJoin("RIGHT OUTER JOIN");
	}
	public From rightOuterJoin(String target){
		return rightOuterJoin()
				.t(target);
	}
	public From innerJoin(){
		return addJoin("INNER JOIN");
	}
	public From innerJoin(String target){
		return innerJoin()
				.t(target);
	}
	public From crossJoin(){
		return addJoin("CROSS JOIN");
	}
	public From crossJoin(String target){
		return crossJoin()
				.t(target);
	}
	public On on(){
		On on = new On();
		add(on);
		return on;
	}
	public From on(String expression){
		On on = new On();
		add(on);
		on.cond(expression);
		return this;
	}
	/**
	 * marking the closest alias that should be referenced<br>
	 * root alias probably not defined
	 * @return
	 */
	public From referencedOrExcluded(){
		LinkedList<SqlNode<?>> children = getChildren();
		int len = children.size();
		String alias = null;
		for(int i=len-1; i>0; i--){ // i > 0 because root alias must't be marked
			SqlNode<?> c = children.get(i);
			if(Aliasible.class.isInstance(c)){
				Aliasible<?> a = Aliasible.class.cast(c);
				alias = a.getAlias();
				if(alias!=null){
					getShouldBeReferencedAliases().add(alias);
					break;
				}
			}
		}
		if(alias==null){
			throw new RuntimeException("From->referencedOrExcluded: alias not found, please call as method to define alias");
		}
		return this;
	}
	public List<String> getShouldBeReferencedAliases(){
		if(shouldBeReferencedAliases==null){
			shouldBeReferencedAliases = new LinkedList<>();
		}
		return shouldBeReferencedAliases;
	}
	public boolean isShouldBeReferencedAliasesExisted(){
		return shouldBeReferencedAliases!=null && !shouldBeReferencedAliases.isEmpty();
	}
	public From clearShouldBeReferencedAliases(){
		if(isShouldBeReferencedAliasesExisted()){
			shouldBeReferencedAliases.clear();
		}
		return this;
	}
	private void findExprRefAlias(Map<String, List<Expressible>> results, SqlNode<?> n){
		if(!Expressible.class.isInstance(n)){
			return;
		}
		Expressible e = Expressible.class.cast(n);
		List<String> aliases = e.findRelatedAliases();
		for(String alias : aliases){
			List<Expressible> val = results.get(alias);
			if(val==null){
				val = new ArrayList<>();
				results.put(alias, val);
			}
			val.add(e);
		}
	}
	/**
	 * find expressions which reference the aliases within the From clause
	 * @return Map, the keys represent the alias, the values represent the expressions referencing the alias
	 */
	Map<String, List<Expressible>> findExprRefAlias(){
		SelectExpression start = getStart();
		List<SqlNode<?>> list = 
			Arrays.asList(
				start.findFirst(Select.class),
				start.findFirst(Where.class),
				start.findFirst(OrderBy.class),
				start.findFirst(GroupBy.class),
				start.findFirst(Having.class));
		Map<String, List<Expressible>> results = new LinkedHashMap<>();
		for(SqlNode<?> sn: list){
			if(sn==null){
				continue;
			}
			sn.consume(n->{
				findExprRefAlias(results, n);
			});
		}
		return results;
	}
	List<String> findAllAlias(){
		List<String> aliases = 
				getChildren().stream()
					.filter(n->Aliasible.class.isInstance(n) && ((Aliasible<?>)n).getAlias()!=null)
					.map(n->((Aliasible<?>)n).getAlias())
					.collect(Collectors.toList());
		return aliases;
	}
	/**
	 * find expressions used within the From clause, which reference the aliases;<br>
	 * under JDBC context, Expressible usually refers to SimpleCondition;<br>
	 * under hql context, Expressible usually refers to SimpleExpression that implements Aliasible
	 * @return Map, the keys represent the alias, the values represent the expressions referencing the alias
	 */
	Map<String, List<Expressible>> findExprRefAliasWithinFrom(){
		Map<String, List<Expressible>> results = new LinkedHashMap<>();
		consume(n->{
			findExprRefAlias(results, n);
		});
		return results;
	}
	private String findRootAliasOrName(){
		SqlNode<?> first = getChildren().getFirst();
		Aliasible<?> a = Aliasible.class.cast(first);
		String symbol = a.getAlias();
		if(symbol == null){// root target may be SelectExpression(i.e. subquery), if so, the alias is expected
			SimpleExpression se = SimpleExpression.class.cast(first);
			symbol = se.getExpression();
		}
		if(symbol == null){
			throw new RuntimeException("root target missing alias or expression");
		}
		return symbol;
	}
	/**
	 * finding out the aliases within From clause not referenced that should be excluded
	 * @return
	 */
	List<String> findExcludedAlias(){
		Select select = getStart().findFirst(Select.class);
		if(select!=null){
			Optional<SqlNode<?>> asteriskFound = select 
				.getChildren().stream()
					.filter(sn->SimpleExpression.class.isInstance(sn) && "*".equals(SimpleExpression.class.cast(sn).getExpression())).findAny();
			if(asteriskFound.isPresent()){
				return Collections.emptyList();
			}
		}
		List<String> aliases = findAllAlias();
		if(aliases.isEmpty()){
			return Collections.emptyList();
		}
		String rootAlias = findRootAliasOrName();
		
		Map<String, List<Expressible>> expr = findExprRefAlias();
		Set<String> referencedAliases = expr.keySet(); // aliases referenced from other clauses' expressions, e.g. Select, Where...
		List<String> results = Collections.emptyList();
		LinkedList<SqlNode<?>> children = getChildren();
		if(children.stream().filter(sn->On.class.isInstance(sn)).findAny().isPresent()){// infer JDBC
			results = new ArrayList<>();
			subtractAssociatedAlias(rootAlias, copy(null), aliases, results, referencedAliases);
		}else{// infer hql
			aliases.remove(rootAlias);
			if(referencedAliases.isEmpty()){
				results = aliases;
			}else{
				aliases.removeAll(referencedAliases);
				Map<String, List<Expressible>> expressions = findExprRefAliasWithinFrom();
				results = aliases.stream().filter(a->{
					List<Expressible> es = expressions.get(a);
					if(es == null){
						return true;
					}
					return !es.stream().filter(e->{
							return Aliasible.class.isInstance(e)
							&& referencedAliases.contains(Aliasible.class.cast(e).getAlias());
						})
						.findAny().isPresent();
				}).collect(Collectors.toList());
			}
		}
		return results;
	}
	private void subtractAssociatedAlias(String rootAlias, From copy, List<String> aliases, List<String> results, Set<String> referencedAliases){
		aliases.remove(rootAlias);// root alias can't be excluded even if it is not referenced
		aliases.removeAll(referencedAliases);// aliases not be referenced		

		Map<String, List<Expressible>> fromExpressions = copy.findExprRefAliasWithinFrom();
		List<String> excludedAliases = aliases.stream().filter(a->{
			List<Expressible> e = fromExpressions.get(a);
			return e.size()==1 // for JDBC ON sub clause
				;
		}).collect(Collectors.toList());
		
		if(excludedAliases.isEmpty()){
			return;
		}
		
		results.addAll(excludedAliases);// not need to exclude duplicates
		
		List<SqlNode<?>> excluded = new ArrayList<>();
		LinkedList<SqlNode<?>> children = copy.getChildren();
		int len = children.size();
		IntStream.range(0, len)
		.boxed()
		.forEachOrdered(i->{
			SqlNode<?> n = children.get(i);
			if(!Aliasible.class.isInstance(n)){
				return;
			}
			Aliasible<?> a = Aliasible.class.cast(n);
			if(excludedAliases.contains(a.getAlias())){
				excluded.add(n);
				int pre = i-1;
				if(pre >= 0 && Join.class.isInstance(children.get(pre))){
					excluded.add(children.get(pre));
				}
				int next = i+1;
				if(next <= len-1 && On.class.isInstance(children.get(next))){
					excluded.add(children.get(next));
				}
			}
		});
		children.removeAll(excluded);
		
		List<String> nextAliases = copy.findAllAlias();
		subtractAssociatedAlias(rootAlias, copy, nextAliases, results, referencedAliases);
	}
	@Override
	public String genSql(){
		LinkedList<SqlNode<?>> children = new LinkedList<>(getChildren());
		int len = children.size();
		if(len==0){
			return "";
		}
		if(isShouldBeReferencedAliasesExisted()){
			List<String> excludedAliases = findExcludedAlias();
			excludedAliases.retainAll(shouldBeReferencedAliases);
			List<SqlNode<?>> excluded = new ArrayList<>();
			IntStream.range(0, len)
				.boxed()
				.forEach(i->{
					SqlNode<?> n = children.get(i);
					if(!Aliasible.class.isInstance(n)){
						return;
					}
					Aliasible<?> a = Aliasible.class.cast(n);
					if(excludedAliases.contains(a.getAlias())){
						excluded.add(n);
						int pre = i-1;
						if(pre >= 0 && Join.class.isInstance(children.get(pre))){
							excluded.add(children.get(pre));
						}
						int next = i+1;
						if(next <= len-1 && On.class.isInstance(children.get(next))){
							excluded.add(children.get(next));
						}
					}
				});
			children.removeAll(excluded);
		}
		
		long joinCount = children.stream().filter(n->{
			return SimpleCondition.class.isInstance(n) || Join.class.isInstance(n);
		}).count();
		String result = "FROM ";
		String indent = getStartSelectIndent();
		if(joinCount == 0){
			result += children.stream().map(n->{
				if(SelectExpression.class.isInstance(n)){
					return genSubquerySql(n);
				}
				return n.genSql();
			}).collect(Collectors.joining(", "));
		}else{
			AtomicInteger joinIdx = new AtomicInteger(1);
			result += IntStream.range(0, children.size())
				.boxed()
				.map(i->{
					SqlNode<?> n = children.get(i);
					if(i == 0){
						if(SimpleExpression.class.isInstance(n)){
							return n.genSql();
						}else if(SelectExpression.class.isInstance(n)){
							return genSubquerySql(n);
						}
					}
					if(SimpleExpression.class.isInstance(n)){
						return " " + n.genSql();
					}
					if(Join.class.isInstance(n)){
						int idx = joinIdx.getAndIncrement();
						if(idx==1){
							return " " + n.genSql();
						}else{
							return "\n" + indent + n.genSql();
						}
					}
					if(SelectExpression.class.isInstance(n)){
						return " " + genSubquerySql(n);
					}
					if(On.class.isInstance(n)){
						return "\n" + indent + n.genSql();
					}
					return "";
				}).collect(Collectors.joining());
		}
		return result;
	}
	@Override
	public From copy(SqlNode<?>parent){
		From from = new From();
		if(isShouldBeReferencedAliasesExisted()){
			from.shouldBeReferencedAliases = new LinkedList<>(shouldBeReferencedAliases);
		}
		if(parent!=null){
			parent.addCopy(from);
		}
		copyTo(from);
		return from;
	}
}
