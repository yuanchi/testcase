package com.jerrylin.erp.sql.execute;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.function.PredicateThrowable;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.sql.condition.SimpleCondition;
import com.jerrylin.erp.sql.condition.StrCondition;
import com.jerrylin.erp.sql.condition.StrCondition.MatchMode;

/**
 * config and reuse SqlRoot
 * @author JerryLin
 *
 */
public class SqlNodeManager {
	private static final boolean EXCLUDED = true;
	private static final boolean INCLUDED = false;
	
	private SqlRoot root;
	public SqlNodeManager(){
		root = SqlRoot.getInstance();
	}
	public SqlRoot getRoot(){
		return root;
	}
	public SimpleCondition findCondById(String id){
		ISqlNode target = findNodeById(root, id);
		return (SimpleCondition)target;
	}
	private ISqlNode findNodeById(ISqlNode node, String id){
		if(id.equals(node.getId())){
			return node;
		}
		List<ISqlNode> nodes = node.getChildren();
		if(nodes.size() != 0){
			for(int i = 0; i < nodes.size(); i++){
				ISqlNode child = nodes.get(i);
				ISqlNode target = findNodeById(child, id);
				if(target != null){
					return target;
				}
			}
		}
		return null;
	}
	/**
	 * filter out nodes not needed, and copy a new SqlNode(from SqlRoot)
	 * if parent node is filtered out, his children are also excluded.
	 * @param filter
	 * @return
	 */
	public ISqlNode filterOutCopy(PredicateThrowable<ISqlNode> filter){
		return filterOutCopy(root, filter);
	}
	private ISqlNode filterOutCopy(ISqlNode src, PredicateThrowable<ISqlNode> filterout){
		if(filterout.test(src)){
			return null;
		}
		ISqlNode copyNode = src.singleCopy();
		List<ISqlNode> nodes = src.getChildren();
		if(nodes.size() != 0){
			for(int i = 0; i < nodes.size(); i++){
				ISqlNode child = nodes.get(i);
				ISqlNode childCopy = filterOutCopy(child, filterout);
				if(childCopy != null){
					copyNode.addChild(childCopy);
				}
			}
		}
		return copyNode;
	}
	/**
	 * according to config, adjust string condition node expression
	 * @param node
	 */
	private void adjustStrConditionNode(ISqlNode node){
		if(node instanceof StrCondition){
			StrCondition cond = (StrCondition)node;
			MatchMode mode = cond.getMatchMode();
			cond.value(mode.transformer((String)cond.getValue()));
						
			if(cond.isCaseInsensitive()){
				cond.propertyName("lower(" + cond.getPropertyName() + ")");
				cond.value(((String)cond.getValue()).toLowerCase());
			}
		}
		List<ISqlNode> nodes = node.getChildren();
		nodes.forEach(child->{
			adjustStrConditionNode(child);
		});
		
	}
	
	private Map<String, Object> getParams(ISqlNode node){
		Map<String, Object> params = new LinkedHashMap<>();
		getParams(node, params);
		return params;
	}
	
	private void getParams(ISqlNode node, Map<String, Object> params){
		if(node instanceof SimpleCondition){
			SimpleCondition sc = (SimpleCondition)node;
			params.put(sc.getId(), sc.getValue());
		}
		node.getChildren().forEach(child->{
			getParams(child, params);
		});
	}
	
	private static void testAdjustStrConditionNode(){
		SqlNodeManager exe = new SqlNodeManager();
		ISqlRoot root = exe.getRoot()
			.select()
				.target("p1.id", "pId")
				.target("p1.name", "pName")
				.target("p2.mobile", "pMobile")
				.target("p2.tel", "pTel")
				.getRoot()
			.from()
				.target("com.jerrylin.erp.model.Member", "p1")
				.target("com.jerrylin.erp.model.Member", "p2")
				.getRoot()
			.joinAlias("LEFT JOIN p1.orders", "p1Orders")
			.where()
				.andConds()
					.andStrCondition("p1.name LIKE :pName", MatchMode.START, "John")
					.andSimpleCond("p1.age = :pAge", Integer.class)
					.getWhere()
				.orConds()
					.andStrCaseInsensitive("p2.address LIKE :pAddress", MatchMode.ANYWHERE, "AsxhyyOOPP")
					.andSimpleCond("p2.gender = :pGender", Integer.class)
					.getRoot()
			.orderBy()
				.asc("p1.id")
				.desc("p2.name")
				.getRoot()
			;
		
		ISqlNode copy = exe.filterOutCopy(n->{
			if(!(n instanceof SimpleCondition)){
				return INCLUDED;
			}
			SimpleCondition sc = (SimpleCondition)n;
			Object val = sc.getValue();
			if(val == null){
				return EXCLUDED;
			}
			if(val instanceof String){
				String strVal = (String)val;
				if(StringUtils.isBlank(strVal)){
					return EXCLUDED;
				}
			}
			if(val instanceof Collection){
				Collection<?> collect = (Collection<?>)val;
				if(collect.size() == 0){
					return EXCLUDED;
				}
			}
			if(val instanceof Object[]){
				Object[] objArray = (Object[])val;
				if(objArray.length == 0){
					return EXCLUDED;
				}
			}
			return INCLUDED;});
		exe.adjustStrConditionNode(copy);
		System.out.println(copy.genSql());
		Map<String, Object> params = exe.getParams(copy);
		System.out.println("");
		System.out.println("params key value: ");
		params.forEach((k,v)->{
			System.out.println(k + ":" + v);
		});
		
		
		
	}
	
	public static void main(String[]args){
		testAdjustStrConditionNode();
	}
}
