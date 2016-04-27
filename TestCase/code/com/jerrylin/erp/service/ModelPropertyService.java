package com.jerrylin.erp.service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.model.Member;

@Service
@Scope("singleton")
public class ModelPropertyService {
	@Autowired
	private LocalSessionFactoryBean sessionFactory;
	private Map<Class<?>, Map<String, Class<?>>> modelPropertyTypes = new LinkedHashMap<>();
	
	@PostConstruct
	public void init(){
		for(Map.Entry<String, ClassMetadata> c : sessionFactory.getObject().getAllClassMetadata().entrySet()){
			Class<?> mappingClz = c.getValue().getMappedClass();
			Map<String, Class<?>> container = new LinkedHashMap<>();
			getPropertyTypeMapping(mappingClz,container, null, 2);
			modelPropertyTypes.put(mappingClz, container);
		}
	}
	
	public Map<Class<?>, Map<String, Class<?>>> getModelPropertyTypes(){
		return this.modelPropertyTypes;
	}
	
	private static Class<?> getFirstGenericType(Class<?> clz, String propName){
		Class<?> parameterGenericType = null;
		try {
			Field field = clz.getDeclaredField(propName);
			Type genericType = field.getGenericType();
			if(genericType != null && genericType instanceof ParameterizedType){
				ParameterizedType type = (ParameterizedType)genericType;
				if(type != null){
					Type[] args = type.getActualTypeArguments();
					parameterGenericType = (Class<?>)args[0];
				}
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return parameterGenericType;
	}
	
	private static void getPropertyTypeMapping(Class<?> target, Map<String, Class<?>> container, String parentPath, int level){
		if(StringUtils.isBlank(parentPath)){
			parentPath = "";
		}
		if(parentPath.split("\\.").length == level){
			return;
		}
		List<Class<?>> internals = 
			Arrays.asList(
				String.class,
				Boolean.class,
				Double.class,
				Float.class,
				Boolean.class,
				java.util.Date.class,
				java.sql.Date.class,
				java.sql.Timestamp.class);
		try{
			PropertyDescriptor[] ps = PropertyUtils.getPropertyDescriptors(target);
			for(PropertyDescriptor p : ps){
				String name = p.getName();
				Class<?> type = p.getPropertyType();
				if(type == java.lang.Class.class){
					continue;
				}
				if(Collection.class.isAssignableFrom(type)){
					if(List.class.isAssignableFrom(type)){
						name += "[]";
					}else{
						continue;
					}
				}
				if(Map.class.isAssignableFrom(type)){
					name += "{}";
				}
				String currentPath = StringUtils.isBlank(parentPath) ? name : (parentPath + "." + name);
				container.put(currentPath, type);
				
				if(name.contains("{}")){
					continue;
				}
				
				if(name.contains("[]")){
					Class<?> parameterizedGenericType = getFirstGenericType(target, name.replace("[]", ""));
					getPropertyTypeMapping(parameterizedGenericType, container, currentPath, level);
					continue;
				}
				
				if(!type.isPrimitive() && !internals.contains(type)){
					getPropertyTypeMapping(type, container, currentPath, level);
				}
			}
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	private static void testGetFirstGenericType(){
		List<Member> list = new ArrayList<>();
		getFirstGenericType(Member.class, "vipDiscountDetails");
	}
	
	private static void testIsList(){
		System.out.println(List.class.isAssignableFrom(ArrayList.class));
	}
	
	public static void main(String[]args){
		testIsList();
	}
}
