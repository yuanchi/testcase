package com.jerrylin.erp.controller;

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
import com.jerrylin.erp.util.JsonParseUtil;

@Controller
@RequestMapping("/parameter")
@Scope("session")
public class ParameterController extends
		KendoUiGridController<Parameter, Parameter> {
	private static final long serialVersionUID = 3627293074582321850L;
	
	@Override
	String conditionConfigToJsonStr(Object cc){
		String json = JsonParseUtil.parseToJson(cc, Parameter.class, ParameterIgnoreLocalNames.class);
		return json;
	}
	
	@RequestMapping(value="/queryParameterCatAutocomplete",
			method=RequestMethod.POST,
			produces={"application/xml", "application/json"},
			headers="Accept=*/*")
	public @ResponseBody String queryParameterCatAutocomplete(@RequestBody ConditionConfig<ParameterCategory> conditionConfig){
		String result = kendoUiAutocompleteService.queryParameterCategories(conditionConfig);
		return result;
	}
}
