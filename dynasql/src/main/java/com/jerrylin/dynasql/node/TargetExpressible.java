package com.jerrylin.dynasql.node;

import com.jerrylin.dynasql.Aliasible;



public abstract class TargetExpressible<T extends SqlNode<?>> extends SqlNode<T>{
	private static final long serialVersionUID = 4319017278782914615L;
	/**
	 * the abbreviation of target, representing every kind of sql expressions in general<br>
	 * <br>
	 * under the context of select clause, this might be any or the mix of the below:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; table column name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; entity field name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; literal,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; expressions,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; built-in function calls,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; user-defined function calls<br>
	 * <br>
	 * under the context of from clause, this may be any of the below:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; table name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; entity name<br>
	 * <br>
	 * under the context of order by clause, this may be any of the below:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; table column name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; entity field name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; expressions,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; number placeholder<br>
	 * under the context of subquery as filter condition, this may be any of the below except for operator:<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; table column name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; entity field name,<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp; expressions
	 * @param expression
	 * @return
	 */
	public T t(String expression){
		SimpleExpression se = new SimpleExpression();
		se.setExpression(expression);
		add(se);
		return getThisType();
	}
	public T as(String alias){
		SqlNode<?> c = getChildren().getLast();
		if(!Aliasible.class.isInstance(c)){
			throw new RuntimeException("alias position is not correct");
		}
		Aliasible<?> se = Aliasible.class.cast(c);
		se.as(alias);
		return getThisType();
	}
	public T oper(String symbol){
		Operator o = new Operator();
		o.setSymbol(symbol);
		add(o);
		return getThisType();
	}
	public SelectExpression subquery(){
		SelectExpression se = SelectExpression.newInstance();
		// because subquery would add new starting point and adjust indent, call addCopyChild instead of addChild method
		addCopy(se);
//		String preIndent = se.getStartSelectIndent();
//		se.setStart(se);
//		se.setStartSelectIndent(preIndent+baseIndent);
		return se;
	}
	String genSubquerySql(SqlNode<?>n){
		String r = "";
		SelectExpression se = SelectExpression.class.cast(n);
		r = "(" + n.genSql() + ")";
		String alias = se.getAlias();
		if(alias!=null){
			r += (" " + alias);
		}
		return r;
	}
	public T eId(String id){
		return lcId(id);
	}
}
