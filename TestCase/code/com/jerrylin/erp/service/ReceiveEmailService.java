package com.jerrylin.erp.service;

import java.util.Properties;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
@Service
@Scope("prototype")
public class ReceiveEmailService {
	/**
	 * ref. http://stackoverflow.com/questions/5285378/how-to-read-email-of-outlook-with-javamail
	 * ref. https://metoojava.wordpress.com/tag/store-in-java/
	 * 從主機收信
	 * 要使用此API，自行加上user和password
	 * @param st
	 * @return
	 */
	public Message[] receive(SearchTerm st){
		Properties props = System.getProperties();
		String protocol = "pop3";
		String host = "";
		String user = "";
		String password = "";
		props.setProperty("mail.store.protocol", protocol);
		Session session = Session.getDefaultInstance(props, null);
		Message[] messages = null;
		try{
			Store store = session.getStore(protocol);
			store.connect(host, user, password);
			Folder inbox = store.getFolder("Inbox"); // 取得收件夾
//			System.out.println("unread count" + inbox.getUnreadMessageCount());
			inbox.open(Folder.READ_ONLY);
//			messages = inbox.search(new FlagTerm(new Flags(Flag.SEEN), false));
			messages = inbox.search(st);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return messages;
	}
}
