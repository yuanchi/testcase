package com.jerrylin.microservice.util;

import static com.jerrylin.microservice.util.SqlCompose.ROOT_CONDS;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_LIMIT;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_OFFSET;
import static com.jerrylin.microservice.util.SqlCompose.ROOT_ORDER;

import java.util.HashMap;

import com.jerrylin.microservice.util.SqlCompose.RootConds;
import com.jerrylin.microservice.util.SqlCompose.RootLimit;
import com.jerrylin.microservice.util.SqlCompose.RootOffset;
import com.jerrylin.microservice.util.SqlCompose.RootOrder;
import com.jerrylin.microservice.util.SqlCompose.TagKey;

public class PageParams extends HashMap<TagKey, String> {
	private static final long serialVersionUID = 1L;
	public PageParams(
			RootConds rootConds, 
			String conds, 
			RootOrder rootOrder, 
			String order, 
			RootLimit rootLimt, 
			String limit, 
			RootOffset rootOffset, 
			String offset){
		this.put(rootConds, conds);
		this.put(rootOrder, order);
		this.put(rootLimt, limit);
		this.put(rootOffset, offset);
	}
	public String rootConds(){
		return get(ROOT_CONDS);
	}
	public String rootOrder(){
		return get(ROOT_ORDER);
	}
	public String rootLimit(){
		return get(ROOT_LIMIT);
	}
	public String rootOffset(){
		return get(ROOT_OFFSET);
	}
}
