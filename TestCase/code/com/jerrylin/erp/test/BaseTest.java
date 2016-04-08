package com.jerrylin.erp.test;

import java.util.function.Consumer;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.jerrylin.erp.initialize.config.RootConfig;

public class BaseTest {
	public static void executeApplicationContext(Consumer<AnnotationConfigApplicationContext> consumer){
		AnnotationConfigApplicationContext acac = new AnnotationConfigApplicationContext(RootConfig.class);
		consumer.accept(acac);
		acac.close();
	}
	
	public static void main(String[]args){
		String t1 = "4/2-4/5 OHM敦南誠品，單筆滿5000送500，可現抵可累贈，詳情請洽02-27716304";
		System.out.println(t1.length());
	}
}
