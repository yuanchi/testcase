package com.jerrylin.erp.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jerrylin.erp.model.ParameterCategory;

@Controller
@RequestMapping("/parametercategory")
@Scope("session")
public class ParameterCategoryController extends
		KendoUiGridController<ParameterCategory, ParameterCategory> {
	private static final long serialVersionUID = -5829133357843830448L;

}
