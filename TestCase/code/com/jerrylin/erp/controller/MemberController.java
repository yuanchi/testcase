package com.jerrylin.erp.controller;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.jackson.mixin.MemberIgnoreDetail;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.service.QueryBaseService;
import com.jerrylin.erp.util.JsonParseUtil;

@Controller
@RequestMapping("/member")
@Scope("session")
public class MemberController {
	@Autowired
	private QueryBaseService<Member, Member> queryBaseService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@PostConstruct
	public void init(){
		queryBaseService.getSqlRoot()
		.select()
			.target("p").getRoot()
		.from()
			.target(Member.class.getName(), "p").getRoot()
		.where()
			.andConds()
				.andSimpleCond("p.name = :pName", String.class)
				.andSimpleCond("p.idNo = :pIdNo", String.class)
				.andSimpleCond("p.mobile = :pMobile", String.class);
	}
	
	@RequestMapping(value="/queryAll",
			method={RequestMethod.POST},
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryAll(){
		ConditionConfig<Member> cc = queryBaseService.genCondtitionsAfterExecuteQueryPageable();
		String json = conditionConfigToJsonStr(cc);
		return json;
	}
	
	/**
	 * 分頁條件查詢
	 */
	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryConditional(@RequestBody ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> cc = queryBaseService.executeQueryPageable(conditionConfig);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}
	
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<Member> batchSaveOrMerge(@RequestBody List<Member> members){
		List<Member> results = sfw.executeTxReturnResults(s->{
			int batchSize = sfw.getBatchSize();
			int count = 0;
			
			for(int i = 0; i < members.size(); i++){
				Member m = members.get(i);
				if(StringUtils.isBlank(m.getId())){// 因為前端id容易回傳空字串，讓Hibernate以為id有值，所以此處要自行判斷
					String clientId = genNextClientId(s, "TW");
					m.setClientId(clientId);
					s.save(m);
				}else{
					s.update(m);
				}
				if(++count % batchSize == 0){
					s.flush();
					s.clear();
				}
			}
			s.flush();
			s.clear();
			return members;
		});
		return results;
	}
	
	@RequestMapping(value="/deleteByIds",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteByIds(@RequestBody List<String> ids){
		sfw.executeTransaction(s->{
			String queryHql = "SELECT DISTINCT p FROM " + Member.class.getName() + " p WHERE p.id IN (:ids)";
			ScrollableResults results = s.createQuery(queryHql).setParameterList("ids", ids).scroll(ScrollMode.FORWARD_ONLY);
			while(results.next()){
				Object target = results.get()[0];
				s.delete(target);
			}
			s.flush();
			s.clear();
		});
		return "";
	}
	
	private String conditionConfigToJsonStr(ConditionConfig<Member> cc){
		String json = JsonParseUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}
	
	public static int getLatestClientIdSerialNo(Session s, String countryCode){
		int latestSerialNo = 0;
		@SuppressWarnings("unchecked")
		List<String> clientIds = s.createQuery("SELECT MAX(m.clientId) FROM " + Member.class.getName() + " m WHERE substring(m.clientId, 1, 2) = :countryCode").setString("countryCode", countryCode).list();
		if(!clientIds.isEmpty() && clientIds.get(0) != null){
			String clientId = clientIds.get(0);
			clientId = clientId.replace(countryCode, "");
			latestSerialNo = Integer.parseInt(clientId);
		}
		return latestSerialNo;
	}
	/**
	 * 用兩碼國碼和序號產生六碼客戶編號
	 */
	public static String genClientId(String countryCode, int serialNo){
		return countryCode + StringUtils.leftPad(String.valueOf(serialNo), 4, "0");
	}
	
	public static String genNextClientId(Session s, String countryCode){
		int serialNo = getLatestClientIdSerialNo(s, countryCode);
		String clientId = genClientId(countryCode, ++serialNo);
		return clientId;
	}
}
