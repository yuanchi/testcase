package com.jerrylin.dynasql;

import java.util.LinkedList;
import java.util.List;

public class InParameter {
	private String id;
	private Class<?> type;
	private Object value;
	private List<String> features;
	public InParameter id(String id){
		this.id = id;
		return this;
	}
	public InParameter type(Class<?> type){
		this.type = type;
		return this;
	}
	public InParameter value(Object value){
		this.value = value;
		return this;
	}
	public InParameter addFeature(String feature){
		if(features==null){
			features = new LinkedList<>();
		}
		features.add(feature);
		return this;
	}
	public Class<?> getType() {
		return type;
	}
	public Object getValue() {
		return value;
	}
	public List<String> getFeatures() {
		return features;
	}
	public String getId(){
		return id;
	}
}
