package com.jerrylin.erp.service;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.jackson.mixin.MemberIgnoreDetail;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.util.JsonParseUtil;

@Service
@Scope("prototype")
public class KendoUiAutocompleteService {
	@Autowired
	private KendoUiService<Member, Member> queryMemberService;
	
	@PostConstruct
	public void init(){
		queryMemberService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(Member.class, "p");	
	}
	
	public String queryMembers(ConditionConfig<Member> conditionConfig){
		ConditionConfig<Member> cc = queryMemberService.executeQueryPageable(conditionConfig);
		String result = JsonParseUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return result;
	}
}
