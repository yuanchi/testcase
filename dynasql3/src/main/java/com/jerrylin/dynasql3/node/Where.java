package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.util.SqlNodeUtil;

public class Where extends FilterConditions<Where> {
	private static final long serialVersionUID = 6561274260565685894L;
	
	@Override
	public String toSql(){
		String result = super.toSql();
		if(SqlNodeUtil.isNotBlank(result)){
			result = "WHERE " + result;
		}
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String result = super.toSqlWith(indent);
		if(SqlNodeUtil.isNotBlank(result)){
			result = indent + "WHERE " + SqlNodeUtil.trimLeading(result);
		}
		return result;
	}
}
