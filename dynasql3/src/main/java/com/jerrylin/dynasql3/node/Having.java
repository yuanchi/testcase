package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class Having extends FilterConditions<Having> {
	private static final long serialVersionUID = 245751495964079079L;
	@Override
	public String toSql(){
		String result = super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "HAVING " + result;
		}
		return result;
	}

	@Override
	public String toSqlWith(String indent){
		String result = super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			result = indent + "HAVING " + SqlNodeUtil.trimLeading(result);
		}
		return result;
	}
}
