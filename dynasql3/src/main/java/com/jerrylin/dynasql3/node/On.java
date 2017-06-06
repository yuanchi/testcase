package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class On extends FilterConditions<On> {
	private static final long serialVersionUID = 4361920332314006546L;
	@Override
	public String toSql(){
		String result = super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "ON " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String result = super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			result = indent + "ON " + SqlNodeUtil.trimLeading(result);
		}
		return result;
	}
}
