package com.jerrylin.erp.controller;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.component.SessionFactoryWrapper;
import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.model.Product;
import com.jerrylin.erp.service.KendoUiAutocompleteService;
import com.jerrylin.erp.service.KendoUiService;
import com.jerrylin.erp.util.JsonParseUtil;

public abstract class KendoUiGridController<T, R> implements Serializable{
	private static final long serialVersionUID = 6941560925887626505L;
	@Autowired
	private KendoUiService<T, R> kendoUiGridService;
	@Autowired
	KendoUiAutocompleteService kendoUiAutocompleteService;
	@Autowired
	private SessionFactoryWrapper sfw;
	private Class<T> rootType;
	
	@PostConstruct
	void init(){
		rootType = (Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[1];
		kendoUiGridService.getSqlRoot()
			.select().target("p").getRoot()
			.from().target(rootType, "p");
	}
	
	public Class<T> getRootType(){
		return rootType;
	}
	
	public String list(HttpServletRequest request, Model model){
		RequestMapping rm = AnnotationUtils.findAnnotation(this.getClass(), RequestMapping.class);
		String[] modulePaths = rm.value();
		String listPath = modulePaths[0] + "/list";
		return listPath;
	}
	
	String conditionConfigToJsonStr(Object cc){
		String json = JsonParseUtil.parseToJson(cc);
		return json;
	}
		
	@RequestMapping(value="/queryMemberAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryMemberAutocomplete(@RequestBody ConditionConfig<Member> conditionConfig){
		String result = kendoUiAutocompleteService.queryMembers(conditionConfig);
		return result;
	}
	
	@RequestMapping(value="/queryProductAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryProductAutocomplete(@RequestBody ConditionConfig<Product> conditionConfig){
		String result = kendoUiAutocompleteService.queryProducts(conditionConfig);
		return result;
	}	

	@RequestMapping(value="/queryConditional",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryConditional(@RequestBody ConditionConfig<T> conditionConfig){
		ConditionConfig<T> cc = kendoUiGridService.executeQueryPageable(conditionConfig);
		String result = conditionConfigToJsonStr(cc);
		return result;
	}	
	@RequestMapping(value="/batchSaveOrMerge",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody List<T> batchSaveOrMerge(@RequestBody List<T> salesDetails){
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
