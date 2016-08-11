package com.jerrylin.erp.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jerrylin.erp.security.User;

@Controller
@RequestMapping(value="/user")
@Scope("session")
public class UserController extends
		KendoUiGridController<User, User> {
	private static final long serialVersionUID = 8426832143205068678L;
	private static final Map<String, String> filterFieldConverter;
	private static final Map<String, Class<?>> customDeclaredFieldTypes;
	static{
		filterFieldConverter = new LinkedHashMap<>();
		filterFieldConverter.put("defaultGroup", "defaultGroup.info.name");
		filterFieldConverter.put("groups", "groups.id");
		filterFieldConverter.put("roles", "roles.id");
		filterFieldConverter.put("info", "info.name");
		
		customDeclaredFieldTypes = new LinkedHashMap<>();
		customDeclaredFieldTypes.put("defaultGroup.info.name", String.class);
		customDeclaredFieldTypes.put("groups.id", String.class);
		customDeclaredFieldTypes.put("roles.id", String.class);
		customDeclaredFieldTypes.put("info.name", String.class);
	}
	@Override
	void init(){
		super.init();
		kendoUiGridService.setFilterFieldConverter(filterFieldConverter);
		kendoUiGridService.setCustomDeclaredFieldTypes(customDeclaredFieldTypes);
		
		String alias = kendoUiGridService.getAlias();
		kendoUiGridService.getSqlRoot()
			.joinAlias("LEFT JOIN " + alias+".roles", "roles")
			.joinAlias("LEFT JOIN " + alias+".groups", "groups");
	}
}
