package com.jerrylin.dynasql.node;

import static com.jerrylin.dynasql.ParamterTypeFeature.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.jerrylin.dynasql.Aliasible;
import com.jerrylin.dynasql.Dependable;
import com.jerrylin.dynasql.Expressible;
import com.jerrylin.dynasql.InParameter;

public class SelectExpression extends SqlNode<SelectExpression> implements Aliasible<SelectExpression>{
	private static final long serialVersionUID = -7956755697469006936L;
	private static final Pattern FIND_NAMED_PARAM = Pattern.compile(":(\\w+)");  
	
	private String alias;
	private String startIndent;
	/**
	 * get existing Select or new one
	 * @return
	 */
	public Select select(){
		Select select = findFirst(Select.class);
		if(select==null){
			select = new Select();
			add(select);
		}
		return select;
	}
	/**
	 * get existing From or new one
	 * @return
	 */
	public From from(){
		From from = findFirst(From.class);
		if(from==null){
			from = new From();
			add(from);
		}
		return from;
	}
	/**
	 * get existing Where or new one
	 * @return
	 */
	public Where where(){
		Where where = findFirst(Where.class);
		if(where==null){
			where = new Where();
			add(where);
		}
		return where;
	}
	/**
	 * get existing OrderBy or new one
	 * @return
	 */
	public OrderBy orderBy(){
		OrderBy orderBy = findFirst(OrderBy.class);
		if(orderBy==null){
			orderBy = new OrderBy();
			add(orderBy);
		}
		return orderBy;
	}
	/**
	 * get existing GroupBy or new one
	 * @return
	 */
	public GroupBy groupBy(){
		GroupBy groupBy = findFirst(GroupBy.class);
		if(groupBy==null){
			groupBy = new GroupBy();
			add(groupBy);
		}
		return groupBy;
	}
	/**
	 * get existing Having or new one
	 * @return
	 */
	public Having having(){
		Having having = findFirst(Having.class);
		if(having==null){
			having = new Having();
			add(having);
		}
		return having;
	}
	public String getAlias(){
		return alias;
	}
	public void setAlias(String alias){
		this.alias = alias;
	}
	public SelectExpression as(String alias){
		setAlias(alias);
		return this;
	}
	private String calStartIndent(){
		if(startIndent!=null){
			return startIndent;
		}
		int baseIndentLen = baseIndent.length();
		String indent = getStartSelectIndent();
		indent = indent.substring(0, indent.length()-baseIndentLen);
		startIndent = indent;
		return startIndent;
	}
	@Override
	public String genSql(){
		String indent = calStartIndent();
		String sep = "\n" + (this!=getRoot() ? indent : "");
		List<SqlNode<?>> children = getChildren();
		String result = "";
		if(children.size()>0){
			result = children.stream().map(n->n.genSql()).collect(Collectors.joining(sep));
		}
		return result;
	}
	@Override
	public SelectExpression copy(SqlNode<?>parent){
		SelectExpression se = newInstance();
		if(parent!=null){
			parent.addCopy(se);
		} 
		copyTo(se);
		se.setAlias(getAlias());
		return se;
	}
	/**
	 * if current instance is root, calling this method starts copying
	 * @return
	 */
	public SelectExpression copy(){
		SelectExpression se = initAsRoot();
		se.setAlias(getAlias());
		copyTo(se);
		return se;
	}
	/**
	 * remove SimpleConditions whose all attached InParameters' values are NULL
	 * @return
	 */
	public Consumer<SelectExpression> defaultExclude(){
		return se->{
			se.removeConditions(
				sn->sn.getParams().stream().allMatch(
					p->{
						Object val = p.getValue();
						return val == null
							|| (val.getClass()==String.class && String.class.cast(val).trim().equals(""))
							|| (Collection.class.isInstance(val) && Collection.class.cast(val).isEmpty())
							|| (val.getClass().isArray() && Array.getLength(val)==0)
							|| (Map.class.isInstance(val) && Map.class.cast(val).isEmpty())
							;
					}));
		};
	}
	public SelectExpression exclude(Consumer<SelectExpression> consumer){
		consumer.accept(this);
		return this;
	}
	public SelectExpression exclude(){
		return exclude(defaultExclude());
	}
	/**
	 * adjust join dependency if configured: delegating to From genSql method,<br>
	 * then adjust values or expressions
	 * @return
	 */
	public Consumer<SelectExpression> defaultTransfrom(){
		return se->{
			se.find(sn->!getParams().isEmpty());
			se.getFound().stream().forEach(sn->{
				sn.getParams().forEach(p->{
					List<String> features = p.getFeatures(); // TODO how to modify to maintain more easily and less complex
					features.stream().forEach(f->{
						if(f.equals(M_START_WITH)){
							p.value(p.getValue().toString()+"%");
						}else if(f.equals(M_END_WITH)){
							p.value("%"+p.getValue().toString());
						}else if(f.equals(M_CONTAIN)){
							p.value("%"+p.getValue().toString()+"%");
						}else if(f.equals(S_START_WITH)){
							p.value(p.getValue().toString()+"_");
						}else if(f.equals(S_END_WITH)){
							p.value("_"+p.getValue().toString());
						}else if(f.equals(S_CONTAIN)){
							p.value("_"+p.getValue().toString()+"_");
						}else if(f.equals(UPPERCASE)){
							p.value(p.getValue().toString().toUpperCase());
						}else if(f.equals(LOWERCASE)){
							p.value(p.getValue().toString().toLowerCase());
						}else if(f.equals(IGNORECASE)){
							p.value(p.getValue().toString().toUpperCase());
							if(Expressible.class.isInstance(sn)){
								Expressible e = Expressible.class.cast(sn);
								e.setExpression("UPPER("+e.getExpression()+")");
							}
						}
					});
				});
			});
		};
	}
	public SelectExpression transform(Consumer<SelectExpression> consumer){
		consumer.accept(this);
		return this;
	}
	public SelectExpression transform(){
		return transform(defaultTransfrom());
	}
	/**
	 * remove condition SqlNodes by node id
	 * @param id
	 * @return
	 */
	public SelectExpression removeConditionById(String...id){
		List<String> ids = Arrays.asList(id);
		return removeConditions(sn->ids.contains(sn.id()));
	}
	public SelectExpression removeConditions(Predicate<SqlNode<?>> filterIn){
		find(filterIn);
		getFound().forEach(f->{
			if(Dependable.class.isInstance(f)){
				Dependable d = Dependable.class.cast(f);
				d.removeIncludingSiblings();
			}
		});
		find(sn->Delimiter.class == sn.getClass());
		List<SqlNode<?>> found = getFound();
		Collections.reverse(found); // from bottom to top
		found.forEach(f->{
			removeDelimterIfEmpty(f);
		});
		return this;
	}
	private void removeDelimterIfEmpty(SqlNode<?>sn){
		if(sn == null || Delimiter.class != sn.getClass()){
			return;
		}
		Delimiter<?> d = Delimiter.class.cast(sn);
		if(d.getChildren().isEmpty()){
			d.removeIncludingSiblings();
			SqlNode<?> p = sn.getParent();
			removeDelimterIfEmpty(p);
		}
	}
	public static SelectExpression newInstance(){
		SelectExpression se = new SelectExpression();
		return se;
	}
	public static SelectExpression initAsRoot(){
		SelectExpression se = newInstance();
		se.setRoot(se);
		se.setStart(se);
		se.setStartSelectIndent(se.baseIndent);
		return se;
	}
	public List<Object> accessInParameterValues(){
		find(sn->!sn.getParams().isEmpty());
		List<Object> paramValues = new ArrayList<>();
		getFound().stream().forEachOrdered(sn->{
			paramValues.addAll(
				sn.getParams().stream().map(p->p.getValue()).collect(Collectors.toList())
			);
		});
		return paramValues;
	}
	public Map<String, Object> accessNamedParameterValues(){
		find(sn->!sn.getParams().isEmpty());
		Map<String, Object> paramValues = new LinkedHashMap<>();
		getFound().stream().forEachOrdered(sn->{
			Matcher m = FIND_NAMED_PARAM.matcher(sn.genSql());
			List<String> names = new ArrayList<>();
			while(m.find()){
				names.add(m.group(1));
			}
			List<InParameter> params = sn.getParams();
			paramValues.putAll(
				IntStream.range(0, params.size())
					.boxed()
					.collect(Collectors.toMap(i->names.get(i), i->params.get(i).getValue()))
			);
//			Map<String, Object> vv = sn.getParams().stream().collect(Collectors.toMap(p->p.getId(), p->p.getValue()));
//			Map<String, Object> vv = sn.getParams().stream().collect(Collectors.toMap(InParameter::getId, InParameter::getValue));
		});
		return paramValues;
	}
}
