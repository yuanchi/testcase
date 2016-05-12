package com.jerrylin.erp.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jerrylin.erp.component.ConditionConfig;
import com.jerrylin.erp.jackson.mixin.ParameterIgnoreLocalNames;
import com.jerrylin.erp.model.Parameter;
import com.jerrylin.erp.model.ParameterCategory;
import com.jerrylin.erp.service.ParameterCategoryQueryService;
import com.jerrylin.erp.util.JsonParseUtil;

@Controller
@RequestMapping("/parameter")
@Scope("session")
public class ParameterController extends
		KendoUiGridController<Parameter, Parameter> {
	private static final long serialVersionUID = 3627293074582321850L;
	private static final Map<String, String> filterFieldConverter;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("parameterCategory", "parameterCategory.id");
	}
	@Autowired
	private ParameterCategoryQueryService parameterCategoryQueryService;
	
	@Override
	@PostConstruct
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
	}
	
	@Override
	String conditionConfigToJsonStr(Object cc){
		String json = JsonParseUtil.parseToJson(cc, Parameter.class, ParameterIgnoreLocalNames.class);
		return json;
	}
	
	@RequestMapping(value="/queryParameterCatDropDownList",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryParameterCatDropDownList(@RequestBody ConditionConfig<ParameterCategory> conditionConfig){
		String result = parameterCategoryQueryService.findTargetList(conditionConfig);
		return result;
	}
}
