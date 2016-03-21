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
}
