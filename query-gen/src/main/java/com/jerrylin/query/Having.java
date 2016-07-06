package com.jerrylin.query;

import org.apache.commons.lang3.StringUtils;

public class Having extends GroupConditions {
	@Override
	public Having cond(String content, Object val){
		return (Having)super.cond(content, val);
	}
	@Override
	public Having cond(String content){
		return (Having)super.cond(content, null);
	}
	@Override
	public Having cond(String content, ValueFeatures valueFeatures){
		return (Having)super.cond(content, null, valueFeatures);
	}
	@Override
	public Having and(){
		return (Having)super.and();
	}
	@Override
	public Having or(){
		return (Having)super.or();
	}
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->{
			String sql = sn.genSql();
			if(!sql.contains("AND") && !sql.contains("OR")){
				return sql;
			}
			return "\n" + (toStart().getIndent() + baseIndent) + sql;
		}).toArray();
		if(map != null && map.length > 0){
			String where = "HAVING " + StringUtils.join(map, " ");
			return where;
		}
		return "";
	}
	@Override
	public Having newInstance(){
		return new Having();
	}
}
