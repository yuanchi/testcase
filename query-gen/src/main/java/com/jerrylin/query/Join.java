package com.jerrylin.query;

public class Join extends SqlNode {
	public static final String DESC = "desc";
	public String getDesc() {
		return attr(DESC);
	}
	public void setDesc(String desc) {
		attr(DESC, desc);
	}
	@Override
	public String genSql(){
		return getDesc();
	}
	@Override
	public Join newInstance(){
		return new Join();
	}
}
