package com.jerrylin.dynasql;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import com.jerrylin.dynasql.node.SelectExpression;

public class DynaSql implements Serializable{
	private static final long serialVersionUID = 161312995833824281L;
	private Map<String, InParameter> params = Collections.emptyMap();
	private SelectExpression root;

	public DynaSql addParam(String id, InParameter ip){
		if(params.isEmpty()){
			params = new LinkedHashMap<>();
		}
		params.put(id, ip);
		return this;
	}
	public DynaSql addParam(String id, Object value){
		InParameter ip = getParam(id);
		if(ip == null){
			ip = new InParameter();
		}
		ip.value(value);
		return addParam(id, ip);
	}
	public DynaSql addParam(InParameter ip){
		int idx = params.size();
		return addParam(idx+"", ip);
	}
	public DynaSql addParam(Object value){
		InParameter ip = new InParameter();
		ip.value(value);
		return addParam(ip);
	}
	public InParameter getParam(String id){
		return params.get(id);
	}
	public InParameter getParam(int idx){
		return getParam(idx+"");
	}
	public SelectExpression getRoot(){
		if(root==null){
			root = SelectExpression.initAsRoot();
		}
		return root;
	}
}
