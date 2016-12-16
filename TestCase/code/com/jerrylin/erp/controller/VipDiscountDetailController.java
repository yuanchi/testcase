package com.jerrylin.erp.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jerrylin.erp.model.VipDiscountDetail;
@Controller
@RequestMapping("/vipdiscountdetail")
@Scope("session")
public class VipDiscountDetailController extends
		KendoUiGridController<VipDiscountDetail, VipDiscountDetail> {
	private static final long serialVersionUID = 8921779582549428292L;

}
