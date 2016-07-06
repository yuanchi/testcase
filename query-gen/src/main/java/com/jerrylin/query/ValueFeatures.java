package com.jerrylin.query;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.jerrylin.query.FilterConditionConverter.*;
public class ValueFeatures {
	private List<String> features = new LinkedList<>();
	public static final ValueFeatures EMPTY = newInstance();
	
	public static ValueFeatures newInstance(){
		return new ValueFeatures();
	}
	public static ValueFeatures newInstance(String f){
		ValueFeatures vf = new ValueFeatures();
		vf.add(f.split(","));
		return vf;
	}
	public ValueFeatures string(){
		features.add(VALUE_FEATURE_STRING);
		return this;
	}
	public boolean isString(){
		return features.contains(VALUE_FEATURE_STRING);
	}
	public ValueFeatures startWith(){
		features.add(VALUE_FEATURE_STRING_START_WITH);
		return this;
	}
	public boolean isStartWith(){
		return features.contains(VALUE_FEATURE_STRING_START_WITH);
	}
	public ValueFeatures contain(){
		features.add(VALUE_FEATURE_STRING_CONTAIN);
		return this;
	}
	public boolean isContain(){
		return features.contains(VALUE_FEATURE_STRING_CONTAIN);
	}
	public ValueFeatures endWith(){
		features.add(VALUE_FEATURE_STRING_END_WITH);
		return this;
	}
	public boolean isEndWith(){
		return features.contains(VALUE_FEATURE_STRING_END_WITH);
	}
	public ValueFeatures exact(){
		features.add(VALUE_FEATURE_STRING_EXACT);
		return this;
	}
	public boolean isExact(){
		return features.contains(VALUE_FEATURE_STRING_EXACT);
	}
	public ValueFeatures ignoreCase(){
		features.add(VALUE_FEATURE_STRING_IGNORE_CASE);
		return this;
	}
	public boolean isIgnoreCase(){
		return features.contains(VALUE_FEATURE_STRING);
	}
	public ValueFeatures valueExpected(){
		features.add(VALUE_FEATURE_VALUE_EXPECTED);
		return this;
	}
	public boolean isValueExpected(){
		return features.contains(VALUE_FEATURE_VALUE_EXPECTED);
	}
	public boolean is(String feature){
		return features.contains(feature);
	}
	public ValueFeatures add(String... features){
		Arrays.asList(features).forEach(f->{
			this.features.add(f);
		});
		return this;
	}
	public List<String> getFeatures(){
		return this.features;
	}
}
