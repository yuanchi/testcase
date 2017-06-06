package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.ChildExpressible;
import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class GroupBy extends SqlNode<GroupBy> implements ChildExpressible<GroupBy>,
ChildSubquerible<GroupBy>{
	private static final long serialVersionUID = -3378555877344416445L;
	@Override
	public String toSql(){
		String result = ChildExpressible.super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "GROUP BY " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String result = ChildExpressible.super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			String preIndent = getParent() != null ? indent : "";
			result = preIndent + "GROUP BY " + result;
		}
		return result;
	}
}
