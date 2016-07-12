package com.jerrylin.erp.service.test;

import javax.mail.Message;
import javax.mail.search.SubjectTerm;

import org.junit.Test;

import com.jerrylin.erp.service.ReceiveEmailService;

public class ReceiveEmailServiceTests {
	@Test
	public void receive(){
		ReceiveEmailService serv = new ReceiveEmailService();
		Message[] messages = serv.receive(new SubjectTerm("2016年6月安格卡特員工薪資單-林洪遠"));
		System.out.println("message count:" + messages.length);
	}
}
