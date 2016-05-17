package com.jerrylin.erp.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jerrylin.erp.model.Member;
@Controller
@RequestMapping("/member2")
@Scope("session")
public class Member2Controller extends KendoUiGridController<Member, Member> {
	private static final long serialVersionUID = -6935998316165880508L;

}
