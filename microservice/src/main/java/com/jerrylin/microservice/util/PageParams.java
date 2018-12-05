package com.jerrylin.microservice.util;

import static com.jerrylin.microservice.util.SqlCompose.R_LIMIT;
import static com.jerrylin.microservice.util.SqlCompose.R_OFFSET;
import static com.jerrylin.microservice.util.SqlCompose.R_ORDER_BY;
import static com.jerrylin.microservice.util.SqlCompose.R_WHERE;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.jerrylin.microservice.util.SqlCompose.JoinWhere;
import com.jerrylin.microservice.util.SqlCompose.RootLimit;
import com.jerrylin.microservice.util.SqlCompose.RootOffset;
import com.jerrylin.microservice.util.SqlCompose.RootOrderBy;
import com.jerrylin.microservice.util.SqlCompose.RootWhere;
import com.jerrylin.microservice.util.SqlCompose.TagKey;

public class PageParams extends HashMap<TagKey, String> {
	private static final long serialVersionUID = 1L;
	public PageParams(
			RootWhere rootConds, 
			String conds, 
			RootOrderBy rootOrder, 
			String order, 
			RootLimit rootLimt, 
			String limit, 
			RootOffset rootOffset, 
			String offset){
		this.put(rootConds, "WHERE " + conds);
		this.put(rootOrder, "ORDER BY " + order);
		this.put(rootLimt, "LIMIT " + limit);
		this.put(rootOffset, "OFFSET " + offset);
	}
	private boolean joinWhereExisted;
	public String rootConds(){
		return get(R_WHERE);
	}
	public String rootOrderBy(){
		return get(R_ORDER_BY);
	}
	public String rootLimit(){
		return get(R_LIMIT);
	}
	public String rootOffset(){
		return get(R_OFFSET);
	}
	public PageParams joinWhere(JoinWhere jw, String conds){
		joinWhereExisted = true;
		put(jw, "WHERE " + conds);
		return this;
	}
	public Map<JoinWhere, String> joinWheres(){
		if(!joinWhereExisted){
			return Collections.emptyMap();
		}
		Map<JoinWhere, String> r = new HashMap<>();
		for(Map.Entry<TagKey, String> e : entrySet()){
			TagKey tk = e.getKey();
			if(tk instanceof JoinWhere){
				r.put((JoinWhere)tk, e.getValue());
			}
		}
		return r;
	}
}
