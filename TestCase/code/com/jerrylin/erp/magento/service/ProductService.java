package com.jerrylin.erp.magento.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProductService {
	private static final String LOCAL_HOST_URL = "http://localhost/magento/index.php/";
	private static final String INTRANET_HOST_URL = "http://192.168.1.15/magento/index.php/";
	
	private String baseUrl = LOCAL_HOST_URL;
	
	private void changeToLocalUrl(){
		this.baseUrl = LOCAL_HOST_URL;
	}
	
	private void changeToIntranetUrl(){
		this.baseUrl = INTRANET_HOST_URL;
	}
	
	private String connectToProductApi(String action){
		return connectToProductApi(action, null);
	}
	
	private String connectToProductApi(String action, List<Object> args){
		String requestUrl = baseUrl + "angrycatproduct/api/" + action;
//		String requestUrl = baseUrl + "customer/api/createCustomer";
//		String data = "data=[{\"create_id\":1},{\"create_id\":2}]";
		String data = "apiUser=test01&apiKey=test01&"; // 授權
		if(args != null && args.size()>0){
			ObjectMapper om = new ObjectMapper();
			try {
				data += ("data=" + om.writeValueAsString(args));
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		}
		byte[] postData = data.getBytes(StandardCharsets.UTF_8);
		int postDataLen = postData.length;
		String result = "";
		boolean isResOk = false;
		try{
			URL url = new URL(requestUrl);
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			
			connection.setDoOutput(true);
			connection.setInstanceFollowRedirects(false);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("charset", "utf-8");
			connection.setRequestProperty("Conetent-Length", Integer.toString(postDataLen));
			connection.setUseCaches(false);
			
			
			try(DataOutputStream dos = new DataOutputStream(connection.getOutputStream())){
				dos.write(postData);
				dos.close();
				
				int responseCode = connection.getResponseCode();
				isResOk = (responseCode == HttpURLConnection.HTTP_OK);
				InputStream is = connection.getInputStream();
				if(is == null){
					is = connection.getErrorStream();
				}
				if(is != null){
					try(BufferedReader br = new BufferedReader(new InputStreamReader(is))){
						
						String lineResult = null;
						while((lineResult = br.readLine()) != null){
							result += lineResult;
						}
					}catch(Throwable e){
						throw new RuntimeException(e);
					}
				}
				
				
			}catch(Throwable e){
				throw new RuntimeException(e);
			}finally{
				connection.disconnect();
			}
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		System.out.println("Response is Ok: " + isResOk);
		System.out.println("response result: " + result);
		return result;
	}
	
	public void listAllProducts(){
		String result = connectToProductApi("listAllProductsResponse");

		ObjectMapper om = new ObjectMapper();
		try {
			JsonNode root = om.readTree(result);
			if(!root.isNull() && root.isArray()){
				root.forEach(node->{					
					Iterator<String> fieldNames = node.fieldNames();
					fieldNames.forEachRemaining(fieldName->{
						System.out.println(fieldName + "=>" + node.get(fieldName).asText());
					});
				});
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void listProductsByFilters(){
		Map<String, String> cond1Operators = new LinkedHashMap<>();
		cond1Operators.put("key", "in");
		cond1Operators.put("value", "TT033,TT057");
		
		Map<String, Object> cond1 = new LinkedHashMap<>();
		cond1.put("key", "sku");
		cond1.put("value", cond1Operators);
		
		List<Map<String, Object>> conds = new LinkedList<>();
		conds.add(cond1);
		
		Map<String, List<Map<String, Object>>> filters = new LinkedHashMap<>();
		filters.put("complex_filter", conds);
		
		String results = connectToProductApi("listProductsByFilters", Arrays.asList(filters));
	}
	
	public void listInventoryByProductIds(List<Object> ids){
		// id can be product id or sku
		String result = connectToProductApi("listInventoryByIds", ids);
		
		ObjectMapper om = new ObjectMapper();
		try {
			JsonNode root = om.readTree(result);
			if(!root.isNull() && root.isArray()){
				root.forEach(node->{					
					Iterator<String> fieldNames = node.fieldNames();
					fieldNames.forEachRemaining(fieldName->{
						System.out.println(fieldName + "=>" + node.get(fieldName).asText());
					});
				});
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void updateInventoryByProductId(){
		String productId1 = "asus001";
		Map<String, Object> updateData1 = new LinkedHashMap<>();
		updateData1.put("qty", "90");
		Map<String, Object> update1 = new LinkedHashMap<>();
		update1.put("productId", productId1);
		update1.put("updateData", updateData1);
		
		String productId2 = "acer001";
		Map<String, Object> updateData2 = new LinkedHashMap<>();
		updateData2.put("qty", "180");
		Map<String, Object> update2 = new LinkedHashMap<>();
		update2.put("productId", productId2);
		update2.put("updateData", updateData2);
		
		String productId3 = "apple001";
		Map<String, Object> updateData3 = new LinkedHashMap<>();
		updateData3.put("qty", "270");
		Map<String, Object> update3 = new LinkedHashMap<>();
		update3.put("productId", productId3);
		update3.put("updateData", updateData3);
		
		List<Object> args = new LinkedList<>();
		args.add(update1);
		args.add(update2);
		args.add(update3);
		
		String result = connectToProductApi("updateInventoryByProductId", args);
	}
	
	/**
	 * ref. https://www.magentocommerce.com/api/soap/customer/customer.create.html
	 * @param i
	 * @return
	 */
	private Map<String, Object> createCustomerMock(int i){
		Map<String, Object> cus = new LinkedHashMap<>();

		cus.put("email", "angrycat.t"+i+"@gmail.com");
		cus.put("firstname", "John");
		cus.put("lastname", "Li");
		cus.put("password", "t1234567"+i);
		cus.put("website_id", 1);
		cus.put("store_id", 1);
		cus.put("group_id", 1);
		cus.put("prefix", "t"+i + "_prefix");
		cus.put("suffix", "t"+i + "_suffix");
		cus.put("dob", "1988-11-12 09:13:51");
//		cus.put("taxvat ", null);
		cus.put("gender", 1);
		cus.put("middlename", "t"+i + "_middlename");
		
		Map<String, Object> address = new LinkedHashMap<>();
		address.put("city", "台北市");
		address.put("company", "安格卡特國際貿易有限公司");
		address.put("country_id", "US"); // 兩碼國碼
		address.put("fax", "02-27761505");
		address.put("firstname", cus.get("firstname"));
		address.put("lastname", cus.get("lastname"));
		address.put("middlename", cus.get("middlename"));
		address.put("postcode", "11032");
		address.put("prefix", cus.get("prefix"));
		address.put("region_id", "");
		address.put("region", "");
		address.put("street", "新北市板橋區海山路13號5樓");
		address.put("suffix", cus.get("suffix"));
		address.put("telephone", "02-27761505");
		address.put("is_default_billing", true);
		address.put("is_default_shipping", true);
		
		cus.put("addresses", Arrays.asList(address));
		
		return cus;
	}
	
	private Map<String, Object> createProductMock(int i){
		Map<String, Object> prod = new LinkedHashMap<>();
		
		return prod;
	}	
	public void createProduct(){
		List<Object> products = new ArrayList<>();
		products.add(createProductMock(5));
//		customers.add(createMock(3));
		
		String result = connectToProductApi("createProduct", products);
	}
	public void deleteProduct(){
		String result = connectToProductApi("deleteProduct", Arrays.asList("6"));
	}
	private static void testChangeToIntranetUrl(){
		ProductService service = new ProductService();
		service.changeToIntranetUrl();
		service.listInventoryByProductIds(Arrays.asList("TT120", "TT121"));
		
	}
	public static void main(String[]args){
//		ProductService service = new ProductService();
//		service.updateInventoryByProductId();
		
//		testChangeToIntranetUrl();
	}
}
