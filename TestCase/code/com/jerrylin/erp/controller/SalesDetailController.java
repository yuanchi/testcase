package com.jerrylin.erp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.jackson.mixin.MemberIgnoreDetail;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.model.Product;
import com.jerrylin.erp.model.SalesDetail;
import com.jerrylin.erp.service.KendoUiService;
import com.jerrylin.erp.service.MemberQueryService;
import com.jerrylin.erp.service.ProductQueryService;
import com.jerrylin.erp.util.JsonParseUtil;

@Controller
@RequestMapping("/salesdetail")
@Scope("session")
public class SalesDetailController {
	private static final Map<String, String> filterFieldConverter;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("member", "member.name");
	}
	
	@Autowired
	private KendoUiService<SalesDetail, SalesDetail> kendoUiGridService;
	@Autowired
	private MemberQueryService memberQueryService;
	@Autowired
	private ProductQueryService productQueryService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@PostConstruct
	public void init(){
		kendoUiGridService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(SalesDetail.class, "p");
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}
	
	public String list(HttpServletRequest request, Model model){
		RequestMapping rm = AnnotationUtils.findAnnotation(this.getClass(), RequestMapping.class);
		String[] modulePaths = rm.value();
		String listPath = modulePaths[0] + "/list";
		return listPath;
	}
	
	private String conditionConfigToJsonStr(Object cc){
		String json = JsonParseUtil.parseToJson(cc, Member.class, MemberIgnoreDetail.class);
		return json;
	}
	
	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryConditional(@RequestBody ConditionConfig<SalesDetail> conditionConfig){
		ConditionConfig<SalesDetail> cc = kendoUiGridService.executeQueryPageable(conditionConfig);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}
	
	@RequestMapping(value="/queryMemberAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryMemberAutocomplete(@RequestBody ConditionConfig<Member> conditionConfig){
		String result = memberQueryService.findTargetPageable(conditionConfig);
		return result;
	}
	
	@RequestMapping(value="/queryProductAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryProductAutocomplete(@RequestBody ConditionConfig<Product> conditionConfig){
		String result = productQueryService.findTargetPageable(conditionConfig);
		return result;
	}	
	
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<SalesDetail> batchSaveOrMerge(@RequestBody List<SalesDetail> salesDetails){
		return kendoUiGridService.batchSaveOrMerge(salesDetails);
	}
	
	@RequestMapping(value="/deleteByIds",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteByIds(@RequestBody List<String> ids){
		List<?> deletedItems = kendoUiGridService.deleteByIds(ids);
		return conditionConfigToJsonStr(deletedItems);
	}
	
}
