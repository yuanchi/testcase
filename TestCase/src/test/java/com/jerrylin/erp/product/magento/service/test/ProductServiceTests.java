package com.jerrylin.erp.product.magento.service.test;

import static com.jerrylin.erp.product.magento.service.ProductService.PRODUCT_CATEGORY_IDS;
import static com.jerrylin.erp.product.magento.service.ProductService.PRODUCT_FIELD_NAMES;
import static com.jerrylin.erp.product.magento.service.ProductService.PRODUCT_WEBSITE_IDS;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerrylin.erp.product.magento.service.ProductService;
import com.jerrylin.erp.util.JsonParseUtil;

public class ProductServiceTests {
	private ProductService productService;
	@Before
	public void setUp(){
		productService = new ProductService();
	}
	private void printValuNode(String fieldName, JsonNode valueNode){
		if(valueNode.isValueNode()){
			Object val = null;
			String type = null;
			if(valueNode.isInt()){
				type = "int";
				val = valueNode.asInt();
			}else{
				type = "string";
				val = valueNode.asText();
			}
			System.out.println((fieldName != null ? fieldName : "") + ":" + val);
		}
	}
	@Test
	public void listProductsByIds(){
		productService.changeToIntranetUrl();
		String results = productService.listProductsByIds(Arrays.asList("AAA003","AAA004"));
		System.out.println(results);
		
		ObjectMapper om = new ObjectMapper();
		try{
			JsonNode root = om.readTree(results);
			AtomicInteger i = new AtomicInteger(0);
			JsonParseUtil.iterate("root", root, (f,n)->{
				if(n.isContainerNode()
				&& n.isObject()){
					Iterator<String> fieldNames = n.fieldNames();
					boolean included = false;
					while(fieldNames.hasNext()){
						String fieldName = fieldNames.next();
						if(!PRODUCT_FIELD_NAMES.contains(fieldName)){
							continue;
						}
						included = true;
						JsonNode valueNode = n.get(fieldName);
						if(fieldName.equals(PRODUCT_CATEGORY_IDS) || fieldName.equals(PRODUCT_WEBSITE_IDS)){
							valueNode.forEach(c->{
								printValuNode(fieldName, c);
							});
						}else{
							printValuNode(fieldName, valueNode);
						}
					}
					if(included){
						i.incrementAndGet();
						System.out.println("----------------------");
					}
				}
			});
			System.out.println("共"+i.get()+"筆");
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Test
	public void changeToIntranetUrl(){
		productService.changeToIntranetUrl();
		productService.listInventoryByProductIds(Arrays.asList("AAA003"));
		
	}
	@Test
	public void updateInventoryByProductId(){
		Map<String, String> params = new LinkedHashMap<>();
//		params.put("asus001", "88");
		params.put("TT033", "23");
//		params.put("apple001", "270");
		
		productService.updateInventoryByProductId(params);
	}
	@Test
	public void listInventoryByProductIds(){
		productService.changeToIntranetUrl();
		productService.listInventoryByProductIds(Arrays.asList("SSERR033"));
	}
	@Test
	public void listAllProducts(){
		productService.changeToIntranetUrl();
		productService.listAllProducts();
	}
	@Test
	public void listAllInventory(){
		//productService.changeToIntranetUrl();
		productService.listAllInventory();
	}
	@Test
	public void listOutOfStockAndQtyMoreThanZero(){
		productService.changeToIntranetUrl();
		productService.listOutOfStockAndQtyMoreThanZero();
	}
	@Test
	public void listInStockAndQtyLessThanOrEqualsToZero(){
		productService.changeToIntranetUrl();
		productService.listInStockAndQtyLessThanOrEqualsToZero();
	}
	@Test
	public void updateOutOfStockToInStockIfQtyMoreThanZero(){
		productService.updateOutOfStockToInStockIfQtyMoreThanZero();
		List<String> ids = productService.listOutOfStockAndQtyMoreThanZero();
	}
}
