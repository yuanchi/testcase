package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.ChildExpressible;
import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class Select extends SqlNode<Select> implements ChildExpressible<Select>, ChildSubquerible<Select>{
	private static final long serialVersionUID = 8920960929182220159L;
	@Override
	public String toSql(){
		String result = ChildExpressible.super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "SELECT " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String result = ChildExpressible.super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			result = "SELECT " + result;
		}
		return result;
	}
}
