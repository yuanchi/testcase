package com.jerrylin.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.jackson.mixin.MemberIgnoreDetail;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.util.JsonParseUtil;
@Service
@Scope("prototype")
public class MemberQueryService extends KendoUiService<Member, Member> {
	private static final long serialVersionUID = 8299736701904469150L;

	@Override
	public String conditionConfigToJsonStr(Object cc){
		String json = JsonParseUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}
}
