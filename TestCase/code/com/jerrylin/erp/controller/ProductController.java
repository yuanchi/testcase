package com.jerrylin.erp.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.jerrylin.erp.model.InventoryOption;
import com.jerrylin.erp.model.Product;
import com.jerrylin.erp.service.KendoUiService;
import com.jerrylin.erp.util.JsonParseUtil;
@Controller
@RequestMapping(value="/product")
@Scope("session")
public class ProductController extends KendoUiGridController<Product, Product> {
	private static final long serialVersionUID = -5561490772627358428L;
	@Autowired
	private KendoUiService<InventoryOption, InventoryOption> queryInvOpt;
	
	@Override
	@PostConstruct
	public void init(){
		super.init();
		queryInvOpt.getSqlRoot().select()
			.target("p").getRoot()
			.from()
			.target(InventoryOption.class, "p").getRoot()
			.orderBy()
				.asc("p.id");
	}
	
	@Override
	@RequestMapping(value="/list", method={RequestMethod.POST, RequestMethod.GET})
	public String list(HttpServletRequest request, Model model){
		List<InventoryOption> ios = queryInvOpt.genCondtitionsAfterExecuteQueryList().getResults();
		int invOptSize = ios.size();
		Map<String, Class<?>> customDeclaredFieldTypes = new LinkedHashMap<>();
		Map<String, String> stockOpts = new LinkedHashMap<>();
		for(int i=0; i < invOptSize; i++){
			String symbol = "productInventories["+i+"].stockQty";
			customDeclaredFieldTypes.put(symbol, Integer.TYPE);
			stockOpts.put(symbol, ios.get(i).getName());
		}
		kendoUiGridService.setCustomDeclaredFieldTypes(customDeclaredFieldTypes);
		model.addAttribute("stockOpts", JsonParseUtil.parseToJson(stockOpts));
		return super.list(request, model);
	}
}
