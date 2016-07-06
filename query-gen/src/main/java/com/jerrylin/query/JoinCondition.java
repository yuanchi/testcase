package com.jerrylin.query;

public class JoinCondition extends SqlNode {
	public static final String DESC = "desc";
	public String getDesc() {
		return attr(DESC);
	}
	public void setDesc(String desc) {
		attr(DESC, desc);
	}
	@Override
	public String genSql(){
		return "ON " + getDesc();
	}
	@Override
	public JoinCondition newInstance(){
		return new JoinCondition();
	}
}
