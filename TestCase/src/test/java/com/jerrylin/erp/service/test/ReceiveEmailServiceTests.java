package com.jerrylin.erp.service.test;

import java.io.InputStream;
import java.util.Date;
import java.util.Iterator;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.search.SubjectTerm;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jerrylin.erp.service.ReceiveEmailService;
import com.jerrylin.erp.util.JsonParseUtil;

public class ReceiveEmailServiceTests {
	@Test
	public void receive(){
		ReceiveEmailService serv = new ReceiveEmailService();
		Message[] messages = serv.receive(new SubjectTerm("2016年6月安格卡特員工薪資單-林洪遠"));
		System.out.println("message count:" + messages.length);
	}
	@Test
	public void receiveProducts(){
		ReceiveEmailService serv = new ReceiveEmailService();
		Message[] messages = serv.receive(new SubjectTerm("ohm-products"));
		System.out.println("message count:" + messages.length);
		if(messages.length == 0){return;}
		
		try{
			Message message = messages[messages.length-1]; // 取得最後一筆
			Date d = message.getReceivedDate();
			System.out.println("received: " + d);
			Object contentObj = message.getContent();
			String content = null;
			
			// 如果在gmail上，長的信件內容不會自己加上斷行和空白；但Synology MailServer收下來的卻會
			// 如果把文字檔案寫在附檔寄出，則不會有這種情況
			// 信件內容會有容量上限，在gmail來講是20K
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
			String countField = "count";
			String summaryField = "summary";
			String warnField = "warn";
			
			JsonNode countValNode = root.get(countField);
			JsonNode summaryNode = root.get(summaryField);
			JsonNode warnNode = root.get(warnField);
			
			System.out.println("count: " + countValNode.asInt());
			Iterator<String> modelIds = summaryNode.fieldNames();
			
			String skuField = "sku";
			String nameEngField = "nameEng";
			String stockField = "stock";
			String retailPriceField = "retailPrice";
			String sheetNameField = "sheetName";
			String idxField = "idx";
			String groupPriceField = "groupPrice";
			String counterPriceField = "counterPrice";
			String productNameField = "productName";
			
			int count = 0;
			while(modelIds.hasNext()){
				count++;
				String modelId = modelIds.next();
				JsonNode product = summaryNode.get(modelId);
				Iterator<String>infoFields = product.fieldNames();
				while(infoFields.hasNext()){
					String infoField = infoFields.next();
					JsonNode info = product.get(infoField);
					if(info.isInt()){
						info.asInt();
					}else if(info.isTextual()){
						info.asText();
					}
					System.out.println(infoField + ":" + (info.isInt() ? info.asInt() : info.asText()));
				}
			}
		
		
			
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
	}
}
