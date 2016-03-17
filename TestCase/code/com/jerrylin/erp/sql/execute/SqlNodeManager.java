package com.jerrylin.erp.sql.execute;

import java.util.List;

import com.jerrylin.erp.function.PredicateThrowable;
import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.ISqlRoot;
import com.jerrylin.erp.sql.SqlRoot;
import com.jerrylin.erp.sql.Where;
import com.jerrylin.erp.sql.condition.SimpleCondition;

/**
 * config and reuse SqlRoot
 * @author JerryLin
 *
 */
public class SqlNodeManager {
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
	public ISqlNode filterCopy(PredicateThrowable<ISqlNode> filter){
		return filterCopy(root, filter);
	}
	private ISqlNode filterCopy(ISqlNode src, PredicateThrowable<ISqlNode> filter){
		if(filter.test(src)){
			return null;
		}
		ISqlNode copyNode = src.singleCopy();
		List<ISqlNode> nodes = src.getChildren();
		if(nodes.size() != 0){
			for(int i = 0; i < nodes.size(); i++){
				ISqlNode child = nodes.get(i);
				ISqlNode childCopy = filterCopy(child, filter);
				if(childCopy != null){
					copyNode.addChild(childCopy);
				}
			}
		}
		return copyNode;
	}
	private static void testFindConditionById(){
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
					.andSimpleCond("p.name LIKE :pName", String.class)
					.andSimpleCond("p.age = :pAge", Integer.class)
					.getWhere()
				.orConds()
					.andSimpleCond("p.address LIKE :pAddress", String.class)
					.andSimpleCond("p.gender = :pGender", Integer.class)
					.getRoot()
			.orderBy()
				.asc("p1.id")
				.desc("p2.name")
				.getRoot()
			;
		SimpleCondition c = exe.findCondById("pGender");
		System.out.println(c.getPropertyName() + " " + c.getOperator() + " " + c.getId());
		
	}
	
	private static void testFilterCopy(){
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
					.andSimpleCond("p.name LIKE :pName", String.class)
					.andSimpleCond("p.age = :pAge", Integer.class)
					.getWhere()
				.orConds()
					.andSimpleCond("p.address LIKE :pAddress", String.class)
					.andSimpleCond("p.gender = :pGender", Integer.class)
					.getRoot()
			.orderBy()
				.asc("p1.id")
				.desc("p2.name")
				.getRoot()
			;
		
		ISqlNode copy = exe.filterCopy(n->{return false;});
		System.out.println(copy.genSql());
		
		ISqlNode copy2 = exe.filterCopy(n->{
			if(n instanceof Where){
				return true;
			}
			return false;
		});
		System.out.println(copy2.genSql());
	}
	
	public static void main(String[]args){
		testFilterCopy();
	}
}
