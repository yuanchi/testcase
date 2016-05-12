package com.jerrylin.erp.service;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jerrylin.erp.model.Product;
@Service
@Scope("prototype")
public class ProductQueryService extends KendoUiService<Product, Product> {
	private static final long serialVersionUID = 4982423057956978157L;

}
