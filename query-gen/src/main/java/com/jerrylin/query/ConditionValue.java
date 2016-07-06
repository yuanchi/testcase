package com.jerrylin.query;

public class ConditionValue extends SqlNode {
	private Object val;
	public Object getVal() {
		return val;
	}
	public void setVal(Object val) {
		this.val = val;
	}
	@Override
	public String genSql(){
		if(val != null){
			return val.toString();
		}
		return null;
	}
	@Override
	public ConditionValue newInstance(){
		return new ConditionValue();
	}
}
