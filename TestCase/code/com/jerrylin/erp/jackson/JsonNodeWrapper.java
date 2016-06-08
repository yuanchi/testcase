package com.jerrylin.erp.jackson;

import java.util.LinkedList;
import java.util.function.Function;
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonNodeWrapper {
	private JsonNode root;
	private LinkedList<JsonNode> found = new LinkedList<>();
	public JsonNodeWrapper(String source){
		ObjectMapper om = new ObjectMapper();
		try{
			this.root = om.readTree(source);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	public JsonNodeWrapper(JsonNode root){
		this.root = root;
	}
	
	public JsonNodeWrapper filter(Predicate<JsonNode> filterIn){
		found.clear();
		filter(root, filterIn);
		return this;
	}
	
	private void filter(JsonNode node, Predicate<JsonNode> filterIn){
		if(node.isContainerNode() && node.isObject()){
			if(filterIn.test(node)){
				found.add(node);
			}
		}else if(node.isContainerNode() && node.isArray()){
			node.forEach(n->{
				filter(n, filterIn);
			});
		}
	}
	
	public <T>LinkedList<T> transformTo(Function<JsonNode, T> exe){
		LinkedList<T> results = new LinkedList<>();
		found.forEach(n->{
			results.add(exe.apply(n));
		});
		return results;
	}
}
