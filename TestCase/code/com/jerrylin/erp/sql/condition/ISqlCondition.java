package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.condition.SqlCondition.Junction;

public interface ISqlCondition extends ISqlNode {
	ISqlCondition junction(Junction junction);
	public Junction getJunction();
}
