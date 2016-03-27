package com.jerrylin.erp.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerrylin.erp.component.ConditionConfig;
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
	
	private String conditionConfigToJsonStr(ConditionConfig<Member> cc){
		String json = JsonParseUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}
}
