package com.jerrylin.erp.sql;

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
			.reduce("", (a, b)->a + "\n" + b);
		return result;
	}
	@Override
	public ISqlNode singleCopy() {
		SqlRoot root = SqlRoot.getInstance();
		root.id(getId());
		return root;
	}
	
	private static void testBaseOperation(){
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
		System.out.println(root.genSql());
	}
	public static void main(String[]args){
		testBaseOperation();
	}



}
