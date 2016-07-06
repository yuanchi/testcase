package com.jerrylin.query;

import org.apache.commons.lang3.StringUtils;


public class Where extends GroupConditions {
	@Override
	public Where cond(String content, Object val){
		return (Where)super.cond(content, val);
	}
	@Override
	public Where cond(String content){
		return (Where)super.cond(content, null);
	}
	@Override
	public Where cond(String content, ValueFeatures valueFeatures){
		return (Where)super.cond(content, null, valueFeatures);
	}
	@Override
	public Where and(){
		return (Where)super.and();
	}
	@Override
	public Where or(){
		return (Where)super.or();
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
			String where = "WHERE " + StringUtils.join(map, " ");
			return where;
		}
		return "";
	}
	@Override
	public Where newInstance(){
		return new Where();
	}
}
