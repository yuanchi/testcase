package com.jerrylin.dynasql.node;

public class Join extends SqlNode<Join> {
	private static final long serialVersionUID = -6867882807861844872L;
	private String joinType;
	public String getJoinType(){
		return joinType;
	}
	public void setJoinType(String joinType){
		this.joinType = joinType;
	}
	@Override
	public String genSql() {
		return joinType;
	}
	@Override
	public Join copy(SqlNode<?>parent){
		Join join = new Join();
		join.setJoinType(joinType);
		if(parent!=null){
			parent.addCopy(join);
		} 
		copyTo(join);
		return join;
	}
}
