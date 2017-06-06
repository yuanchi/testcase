package com.jerrylin.dynasql3.util;

import com.jerrylin.dynasql3.Aliasible;
import com.jerrylin.dynasql3.Expressible;
import com.jerrylin.dynasql3.ExpressionParameterizable;
import com.jerrylin.dynasql3.node.SqlNode;

public class SearchCondition {
	private SqlNode<?> start;
	private String condition;
	
	public SqlNode<?> getStart() {
		return start;
	}
	public void from(SqlNode<?> start) {
		this.start = start;
	}
	public String getCondition(){
		return condition;
	}
	public SearchCondition condition(String condition) {
		this.condition = condition;
		return this;
	}
	
	public static SearchById id(String id){
		SearchById sbi = new SearchById();
		sbi.condition(id);
		return sbi;
	}
	public static SearchByExpr expr(String expr){
		SearchByExpr sbe = new SearchByExpr();
		sbe.condition(expr);
		return sbe;
	}
	public static SearchByAlias alias(String alias){
		SearchByAlias sba = new SearchByAlias();
		sba.condition(alias);
		return sba;
	}
	public static SearchByParamName paramName(String paramName){
		SearchByParamName sbpn = new SearchByParamName();
		sbpn.condition(paramName);
		return sbpn;
	}
	
	public static class SearchById extends SearchCondition implements SqlNodeSearchable{
		@Override
		public <S extends SqlNode<?>> S find() {
			SqlNode<?> f = getStart().findFirst(c->getCondition().equals(c.getId()));
			return (S)f;
		}
	}
	public static class SearchByExpr extends SearchCondition implements SqlNodeSearchable{
		@Override
		public <S extends SqlNode<?>> S find() {
			SqlNode<?> f = getStart().findFirst(c->Expressible.class.isInstance(c) && getCondition().equals(Expressible.class.cast(c).getExpression()));
			return (S)f;
		}
	}
	public static class SearchByAlias extends SearchCondition implements SqlNodeSearchable{
		@Override
		public <S extends SqlNode<?>> S find() {
			SqlNode<?> f = getStart().findFirst(c->Aliasible.class.isInstance(c) && getCondition().equals(Aliasible.class.cast(c).getAlias()));
			return (S)f;
		}
	}
	public static class SearchByParamName extends SearchCondition implements SqlNodeSearchable{
		/**
		 * after finding the target ,automatically set the current parameter,<br>
		 * so that you can immediately update param value.
		 */
		@Override
		public <S extends SqlNode<?>> S find() {
			SqlNode<?> f = getStart().findFirst(c->{
				if(!ExpressionParameterizable.class.isInstance(c)){
					return false;
				}
				ExpressionParameterizable<?> ep = ExpressionParameterizable.class.cast(c);
				String condition = getCondition();
				boolean found = ep.getParamNames().contains(condition);
				if(found){
					ep.getParamAsCurrent(condition); // this can guarantee set target parameter value immediately after searched
				}
				return found;
			});
			return (S)f;
		}
	}
}
