package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.SqlNode;

public abstract class SqlCondition extends SqlNode implements ISqlCondition{
	private static final long serialVersionUID = -8402017095718411755L;
	private Junction junction = Junction.AND;
	private String groupMark;
	@Override
	public ISqlCondition junction(Junction junction){
		this.junction = junction;
		return this;
	}
	@Override
	public Junction getJunction() {
		return junction;
	}
	@Override
	public ISqlCondition groupMark(String groupMark){
		this.groupMark = groupMark;
		return this;
	}
	@Override
	public String getGroupMark(){
		return this.groupMark;
	}
	public enum Junction{
		START(""),
		OR("OR"),
		AND("AND");
		private String symbol;
		private Junction(String symbol){
			this.symbol = symbol;
		}
		public String getSymbol(){return this.symbol;}
	}
}
