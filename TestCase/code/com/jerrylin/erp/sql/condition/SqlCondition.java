package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.SqlNode;

public abstract class SqlCondition extends SqlNode implements ISqlCondition{
	private Junction junction = Junction.AND;
	@Override
	public ISqlCondition junction(Junction junction){
		this.junction = junction;
		return this;
	}
	@Override
	public Junction getJunction() {
		return junction;
	}
	public enum Junction{
		START,
		OR,
		AND
	}
}
