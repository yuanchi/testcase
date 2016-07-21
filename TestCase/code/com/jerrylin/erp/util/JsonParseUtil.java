package com.jerrylin.erp.util;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.apache.commons.beanutils.PropertyUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerrylin.erp.jackson.HibernateAwareObjectMapper;

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
	/**
	 * ref. http://stackoverflow.com/questions/21708339/avoid-jackson-serialization-on-non-fetched-lazy-objects/21760361#21760361
	 * @param obj
	 * @return
	 */
	public static String parseHibernateModelToJson(Object obj){
		String json = "";
		try{
			ObjectMapper om = new HibernateAwareObjectMapper();
			json = om.writeValueAsString(obj);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return json;
	}
	
	/**
	 * 顯示節點狀態
	 * isContainerNode == true && isArray == true 代表陣列物件
	 * isContainerNode == true && isObject == true 代表基本物件
	 * isValuNode == true 代表屬性值節點
	 * isTextual == true 代表這個屬型值節點是字串類型
	 * @param node
	 */
	public static void printStatus(JsonNode node){
		PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(node);
		Arrays.asList(ps).stream().forEach(pd->{
			Method method = pd.getReadMethod();
			String name = method.getName();
			if(name.indexOf("is") == 0){
				try{
					Object val = method.invoke(node);
					if(val != null){
//						System.out.println(name + ":" + val);
						if(val instanceof Boolean && ((boolean)val) == true){
							System.out.println(name);
						}
					}
				}catch(Throwable e){
					throw new RuntimeException(e);
				}
			}
		});
	}
	
	public static void iterate(String fieldName, JsonNode node, BiConsumer<String, JsonNode> consumer){
		consumer.accept(fieldName, node);
		if(node.fieldNames().hasNext()){
			Iterator<Entry<String, JsonNode>> fields = node.fields();
			while(fields.hasNext()){
				Entry<String, JsonNode> field = fields.next();
				iterate(field.getKey(), field.getValue(), consumer);
			}
		}else{
			int idx = 0;
			while(node.has(idx)){
				String field = "" + idx;
				iterate(field, node.get(idx), consumer);
				++idx;
			}
		}
	}
	
	/**
	 * 
	 * @param node
	 * @param consumer key是fielName, value是屬性值節點
	 */
	public static void iterateField(String fieldName, JsonNode node, BiConsumer<String, JsonNode> consumer){
		iterate(fieldName, node, (f, n)->{
			processSingleNode(n, consumer);
		});
	}
	public static void processSingleNode(JsonNode n, BiConsumer<String, JsonNode> consumer){
		if(n.isContainerNode() && n.isObject()){
			Iterator<String> fieldNames = n.fieldNames();
			while(fieldNames.hasNext()){
				String fieldName = fieldNames.next();
				JsonNode fieldNode = n.get(fieldName);
				if(fieldNode != null){
					consumer.accept(fieldName, fieldNode);
				}else{
					System.out.println("fieldNode is null: " + fieldName);
				}
			};
		}
	}
}
