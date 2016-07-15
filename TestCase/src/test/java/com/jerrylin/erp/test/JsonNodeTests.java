package com.jerrylin.erp.test;

import static com.jerrylin.erp.util.JsonParseUtil.iterate;
import static com.jerrylin.erp.util.JsonParseUtil.iterateField;
import static com.jerrylin.erp.util.JsonParseUtil.printStatus;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonNodeTests {
	private JsonNode root;
	@Before
	public void setUp(){
		String source = "[{\"product_id\":\"1\",\"sku\":\"asus001\",\"qty\":\"14.0000\",\"is_in_stock\":\"0\"},{\"product_id\":\"2\",\"sku\":\"acer001\",\"qty\":\"180.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"3\",\"sku\":\"apple001\",\"qty\":\"270.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"4\",\"sku\":\"galaxy001\",\"qty\":\"9995.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"5\",\"sku\":\"TT072(AT)\",\"qty\":\"989.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"6\",\"sku\":\"TT057\",\"qty\":\"998.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"7\",\"sku\":\"TT016 (C)\",\"qty\":\"996.0000\",\"is_in_stock\":\"1\"},{\"product_id\":\"8\",\"sku\":\"TT033\",\"qty\":\"22.0000\",\"is_in_stock\":\"1\"}]";
		ObjectMapper om = new ObjectMapper();
		try{
			this.root = om.readTree(source);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void testIterate(){
		iterate("root", root, (f,n)->{
			System.out.println(n);
			printStatus(n);
		});
	}
	
	@Test
	public void testIterateField(){
		iterateField("root", root, (fieldName, n)->{
			System.out.println(fieldName + ":" + n);
			printStatus(n);
		});
	}

}
