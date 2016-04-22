package com.jerrylin.erp.controller;

import java.util.List;

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
import com.jerrylin.erp.model.SalesDetail;
import com.jerrylin.erp.service.KendoUiService;
import com.jerrylin.erp.util.JsonParseUtil;

@Controller
@RequestMapping("/salesdetail")
@Scope("session")
public class SalesDetailController {
	@Autowired
	private KendoUiService<SalesDetail, SalesDetail> kendoUiGridService;
	@Autowired
	private SessionFactoryWrapper sfw;
	
	@PostConstruct
	public void init(){
		kendoUiGridService.getSqlRoot()
			.select()
				.target("p").getRoot()
			.from()
				.target(SalesDetail.class, "p");
			
	}
	
	public String list(HttpServletRequest request, Model model){
		RequestMapping rm = AnnotationUtils.findAnnotation(this.getClass(), RequestMapping.class);
		String[] modulePaths = rm.value();
		String listPath = modulePaths[0] + "/list";
		return listPath;
	}
	
	private String conditionConfigToJsonStr(ConditionConfig<SalesDetail> cc){
		String json = JsonParseUtil.parseToJson(cc);
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
	
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<SalesDetail> batchSaveOrMerge(@RequestBody List<SalesDetail> salesDetails){
		kendoUiGridService.batchSaveOrMerge(salesDetails);
		return salesDetails;
	}
	
	@RequestMapping(value="/deleteByIds",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String deleteByIds(@RequestBody List<String> ids){
		kendoUiGridService.deleteByIds(ids);
		return "";
	}
	
}