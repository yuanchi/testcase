package com.jerrylin.erp.genserial;

import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;

public class GenSerialUtil {
	private static Map<String, SerialGenerator<?>> generators = new HashMap<>();
	
	public static synchronized void addGenerator(SessionFactory sessionFactory, String... ids) throws Throwable{
		for(String id : ids){
			addGenerator(new DefaultSerialGenerator(id, sessionFactory));
		}
	}
	public static synchronized void addGenerator(SerialGenerator<?> generator){
		if(generator.getId() == null){
			throw new RuntimeException("Generator id can't be null");
		}
		if(generators.containsKey(generator.getId())){
			throw new RuntimeException("Duplicate generator id["+generator.getId()+"]");
		}
		generators.put(generator.getId(), generator);
	}
	public static synchronized String getNext(String id)throws Throwable{
		SerialGenerator<?> generator = generators.get(id);
		if(generator == null){
			throw new RuntimeException("Serial Generator not found ["+id+"]");
		}
		return generator.getNext();
	}
	public static synchronized <S>String getNext(String id, S s){
		SerialGenerator<S> generator = (SerialGenerator<S>)generators.get(id);
		if(generator == null){
			throw new RuntimeException("Serial Generator not found ["+id+"]");
		}
		return generator.getNext(s);
	}
}
