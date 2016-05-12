package com.jerrylin.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.model.ParameterCategory;
@Service
@Scope("prototype")
public class ParameterCategoryQueryService extends
		KendoUiService<ParameterCategory, ParameterCategory> {
	private static final long serialVersionUID = -2060841913006745677L;

}
