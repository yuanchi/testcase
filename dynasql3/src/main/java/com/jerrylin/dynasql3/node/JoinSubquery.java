package com.jerrylin.dynasql3.node;

import com.jerrylin.dynasql3.ChildSubquerible;
import com.jerrylin.dynasql3.Joinable;

public class JoinSubquery extends SqlNode<JoinSubquery> implements
		ChildSubquerible<JoinSubquery>, Joinable<JoinSubquery> {
	private static final long serialVersionUID = -5006841663470928295L;
	
	private String joinType;
	
	@Override
	public String getJoinType() {
		return joinType;
	}
	@Override
	public void setJoinType(String joinType) {
		this.joinType = joinType;
	}
	@Override
	public String toSql(){
		String result = Joinable.super.toSql();
		return result;
	}
	@Override
	public String toSqlWith(String indent){
		String newIndent = " " + indent;
		String r = Joinable.super.toSqlWith(newIndent);
		return r;
	}
}
