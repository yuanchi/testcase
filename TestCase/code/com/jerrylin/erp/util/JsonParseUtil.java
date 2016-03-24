package com.jerrylin.erp.util;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonParseUtil {
	public static String parseToJson(Object object){
		String json = parseToJson(object, null);
		return json;
	}
	
	public static String parseToJson(Object object, Class<?> mixinTarget, Class<?> mixinSource){
		Map<Class<?>, Class<?>> mixins = new HashMap<>();
		mixins.put(mixinTarget, mixinSource);
		String json = parseToJson(object, mixins);
		return json;
	}
	
	public static String parseToJson(Object object, Map<Class<?>, Class<?>> mixins){
		String json = "";
		try{
			ObjectMapper om = new ObjectMapper();
			if(mixins != null){
				mixins.forEach((target, mixin)->{
					om.addMixIn(target, mixin);
				});
			}
			json = om.writeValueAsString(object);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		
		return json;
	}
}
