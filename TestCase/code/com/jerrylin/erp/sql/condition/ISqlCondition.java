package com.jerrylin.erp.sql.condition;

import com.jerrylin.erp.sql.ISqlNode;
import com.jerrylin.erp.sql.condition.SqlCondition.Junction;
/**
 * Sql Condition Base Interface
 * @author JerryLin
 *
 */
public interface ISqlCondition extends ISqlNode {
	ISqlCondition junction(Junction junction);
	public Junction getJunction();
	public ISqlCondition groupMark(String groupMark);
	public String getGroupMark();
}
