package com.jerrylin.erp.product.googleapps.service;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.search.SubjectTerm;



import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Product;
import com.jerrylin.erp.service.ReceiveEmailService;
import com.jerrylin.erp.util.JsonParseUtil;

@Service
@Scope("prototype")
public class ProductInfoService {
	private static final String skuField = "sku";
	private static final String nameEngField = "nameEng";
	private static final String stockField = "stock";
	private static final String retailPriceField = "retailPrice";
	private static final String sheetNameField = "sheetName";
	private static final String idxField = "idx";
	private static final String groupPriceField = "groupPrice";
	private static final String counterPriceField = "counterPrice";
	private static final String productNameField = "productName";
	private static final String countField = "count";
	private static final String summaryField = "summary";
	private static final String warnField = "warn";
	
	@Autowired
	private ReceiveEmailService receiveEmailService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	public void execute(){
		Message[] messages = receiveEmailService.receive(new SubjectTerm("ohm-products-"+LocalDate.now().toString()));
		System.out.println("message count:" + messages.length);
		if(messages.length == 0){return;}
		
		try{
			Message message = messages[messages.length-1]; // 取得最後一筆
			Date d = message.getSentDate();
			System.out.println("sent date: " + d);
			Object contentObj = message.getContent();
			String content = null;
			
			// 如果在gmail上，長的信件內容不會自己加上斷行和空白；但Synology MailServer收下來的卻會。這就代表在信件內容的文字格式，在各個平台是不一致的
			// 如果把文字檔案寫在附檔寄出，則不會有這種情況
			// 信件內容會有容量上限，在gmail來講是20K，所以以附檔的形式比較不容易受限於檔案大小
			if(contentObj instanceof String){// 如果只有body沒有附檔，或者沒有body但有一個文字附檔(是否為文字檔以檔案的MimeType=text/plain判斷)
				content = (String)contentObj;
				System.out.println("content type is String");
			}else if(contentObj instanceof Multipart){// 如果有body，且至少有一個(不論是否為文字)附檔；或者沒有body，但有兩個以上附檔；或者沒有body，但有一個非文字附檔
				System.out.println("content type is Multipart");
				Multipart mp = (Multipart)contentObj;
				System.out.println("content part has " + mp.getCount());
				for(int i=0; i < mp.getCount(); i++){
					BodyPart bodyPart = mp.getBodyPart(i);
					String disposition = bodyPart.getDisposition();
					if(disposition == null){// 代表這是email body content
						System.out.println(bodyPart.getContent());
					}
					if(StringUtils.isNotBlank(disposition)// 代表這是真正的附檔
					&& (disposition.equals(Part.ATTACHMENT)
						|| disposition.equals(Part.INLINE))){
						String fileName = bodyPart.getFileName();
						try(InputStream is = bodyPart.getInputStream();){
							System.out.println(fileName + " byte count:" + is.available());
						}
					}
				}
			}
			if(content == null){
				return;
			}
			
			ObjectMapper om = new ObjectMapper();
			JsonNode root = om.readTree(content);
			JsonParseUtil.processSingleNode(root, (f,n)->{
//				System.out.println(f);
			});
			compare(root);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
	@Transactional
	private void compare(JsonNode root){
		JsonNode countValNode = root.get(countField);
		JsonNode summaryNode = root.get(summaryField);
		JsonNode warnNode = root.get(warnField);
		
		Iterator<String> summaryFields = summaryNode.fieldNames();
		List<String> skus = new ArrayList<>();
		summaryFields.forEachRemaining(skus::add);
		
		compareCount(countValNode, skus);
		comparePrice(summaryNode);
	}
	private void compareCount(JsonNode countValNode, List<String>remoteSkus){
		int remoteCount = countValNode.asInt();
		String queryCount = "SELECT COUNT(p.id) FROM " + Product.class.getName() + " p";
		Session s = sfw.getCurrentSession();
		Long dbCount = (Long)s.createQuery(queryCount).uniqueResult();
		int diff = remoteCount - dbCount.intValue();
		System.out.println("遠端:" + remoteCount + " 資料庫:" + dbCount.intValue() + " 兩者相差:" + diff);
		if(diff == 0){
			return;
		}
		String querySkus = "SELECT p.modelId FROM " + Product.class.getName() + " p";
		List<String> dbSkus = s.createQuery(querySkus).list();
		Collection<String> minuend = null;
		Collection<String> subtrahend = null;
		if(diff > 0){
			minuend = remoteSkus;
			subtrahend = dbSkus;
		}else{
			System.out.println("資料庫商品數量大於遠端--異常");
			minuend = dbSkus;
			subtrahend = remoteSkus;
		}
		Collection<String> remainings = CollectionUtils.subtract(minuend, subtrahend);
		System.out.println(Arrays.toString(remainings.toArray(new String[]{})));
	}
	private void comparePrice(JsonNode summaryNode){
		compareSingle(summaryNode, product->{
			Iterator<String>infoFields = product.fieldNames();
			String sku = null;
			while(infoFields.hasNext()){
				String infoField = infoFields.next();
				JsonNode info = product.get(infoField);
				if(infoField.equals(skuField)){
					sku = info.asText();
					continue;
				}
				if(infoField.equals(retailPriceField)){
					Session s = sfw.getCurrentSession();
					String queryPrice = "SELECT p.suggestedRetailPrice FROM " + Product.class.getName() + " p WHERE p.modelId = :modelId";
					List<Double> dbPrice = s.createQuery(queryPrice).setString("modelId", sku).list();
					int remotePrice = info.asInt();
					if(remotePrice > 0
					&& (dbPrice.isEmpty() || dbPrice.get(0) == 0)){
						System.out.println(sku + " remotePrice:"+ remotePrice + " dbPrice:"+(dbPrice.isEmpty() ? "empty" : dbPrice.get(0)));
					}
				}
			}
		});
	}
	private void compareSingle(JsonNode summaryNode, Consumer<JsonNode> consumer){
//		Iterator<String> modelIdsIterator = summaryNode.fieldNames();
//		List<String> modelIds = new ArrayList<>();
//		modelIdsIterator.forEachRemaining(modelIds::add);
		
		int count = 0;
		Iterator<String> skus = summaryNode.fieldNames();
		while(skus.hasNext()){
			count++;
			String sku = skus.next();
			JsonNode product = summaryNode.get(sku);
			Iterator<String>infoFields = product.fieldNames();
			consumer.accept(product);
			// TODO 未來可能將這些屬性轉成Product model
//			while(infoFields.hasNext()){
//				String infoField = infoFields.next();
//				JsonNode info = product.get(infoField);
//				// JsonNode如果是值節點，在轉型不相符的時候，不會發生錯誤。譬如如果原來是非正確數字的字串，轉成整數會變成0輸出
//				if(info.isInt()){
//					info.asInt();
//				}else if(info.isTextual()){
//					info.asText();
//				}
//				System.out.println(infoField + ":" + (info.isInt() ? info.asInt() : info.asText()));
//			}
		}
	}
}
