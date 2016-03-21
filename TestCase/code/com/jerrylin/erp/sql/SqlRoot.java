package com.jerrylin.erp.sql;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.sql.condition.SimpleCondition;

/**
 * representing root node as starting point
 * @author JerryLin
 *
 */
public class SqlRoot extends SqlNode implements ISqlRoot{
	private static final long serialVersionUID = 6314237980437169038L;
	
	public static SqlRoot getInstance(){
		SqlRoot root = new SqlRoot();
		root.root(root);
		return root;
	}
	@Override
	public Select select(){
		Select select = new Select();
		addChild(select);
		return select;
	}
	@Override
	public From from(){
		From from = new From();
		addChild(from);
		return from;
	}
	@Override
	public Where where(){
		Where where = new Where();
		addChild(where);
		return where;
	}
	@Override
	public ISqlRoot joinAlias(String expression, String alias) {
		Join join = new Join();
		join.expression(expression)
			.alias(alias);
		addChild(join);
		return this;
	}
	@Override
	public ISqlRoot joinOn(String expression, String on) {
		Join join = new Join();
		join.expression(expression)
			.on(on);
		addChild(join);
		return this;
	}
	@Override
	public OrderBy orderBy() {
		OrderBy orderBy = new OrderBy();
		addChild(orderBy);
		return orderBy;
	}
	@Override
	public String genSql(){
		String result = getChildren()
			.stream()
			.map(ISqlNode::genSql)
			.reduce("", (a, b)->a + (StringUtils.isNotBlank(b) ? ("\n" + b) : ""));
		return result;
	}
	@Override
	public ISqlNode singleCopy() {
		SqlRoot root = SqlRoot.getInstance();
		root.id(getId());
		return root;
	}
	/**
	 * filter out nodes not needed, and copy a new SqlNode(from SqlRoot)
	 * if parent node is filtered out, his children are also excluded.
	 * @param filter
	 * @return
	 */
	@Override
	public ISqlRoot excludeCopy(Predicate<ISqlNode> filter){
		return (ISqlRoot)excludeCopy(this, filter);
	}
	private ISqlNode excludeCopy(ISqlNode src, Predicate<ISqlNode> filterout){
		if(filterout.test(src)){
			return null;
		}
		ISqlNode copyNode = src.singleCopy();
		List<ISqlNode> nodes = src.getChildren();
		if(nodes.size() != 0){
			for(int i = 0; i < nodes.size(); i++){
				ISqlNode child = nodes.get(i);
				ISqlNode childCopy = excludeCopy(child, filterout);
				if(childCopy != null){
					copyNode.addChild(childCopy);
				}
			}
		}
		return copyNode;
	}	
	@Override
	public ISqlRoot find(Predicate<ISqlNode> validation){
		super.find(validation);
		return this;
	}
	@Override
	public ISqlRoot update(Consumer<ISqlNode> update){
		super.update(update);
		return this;
	}
	@Override
	public ISqlRoot findNodeById(String id){
		super.findNodeById(id);
		return this;
	}
	@Override
	public Map<String, Object> getCondIdValuePairs() {
		find(n->(n instanceof SimpleCondition));
		Map<String, Object> params = new LinkedHashMap<>();
		founds.forEach(n->{
			SimpleCondition s = (SimpleCondition)n;
			params.put(s.getId(), s.getValue());
		});
		return params;
	}
	private static ISqlRoot getSampleRoot(){
		ISqlRoot root = SqlRoot.getInstance()
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
		return root;
	}
	private static void testBaseOperation(){
		ISqlRoot root = getSampleRoot();
		System.out.println(root.genSql());
	}
	private static void testFindConditionById(){
		ISqlRoot root = getSampleRoot();
		SimpleCondition c = (SimpleCondition)root.findNodeById("pGender");
		System.out.println(c.getPropertyName() + " " + c.getOperator() + " " + c.getId());
		
	}
	
	private static void testExcludeCopy(){
		ISqlRoot root = getSampleRoot();
		
		ISqlNode copy = root.excludeCopy(n->{return INCLUDED;});
		System.out.println(copy.genSql());
		
		ISqlNode copy2 = root.excludeCopy(n->{
			if(n instanceof Where){
				return EXCLUDED;
			}
			return INCLUDED;
		});
		System.out.println(copy2.genSql());
		System.out.println(root == copy2);
	}
	private static void testFindUpdate(){
		ISqlRoot root = getSampleRoot();
		
		root.find(n->"pAge".equals(n.getId()))
			.update(n->{
				SimpleCondition s = (SimpleCondition)n;
				s.value(10);
			});
		
		Map<String, Object> params = root.getCondIdValuePairs();
		params.forEach((k,v)->{
			System.out.println(k+"|"+v);
		});
	}
	private static void testRemove(){
		ISqlRoot root = getSampleRoot();
//		root.find(n->(n instanceof SimpleCondition))
//			.remove();
		root.find(n->((n instanceof Asc) || (n instanceof Desc)))
			.remove();
		System.out.println(root.genSql());
		
	}
	
	public static void main(String[]args){
		testRemove();
	}





}
