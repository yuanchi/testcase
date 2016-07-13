package com.jerrylin.erp.product.googleapps.service.test;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.jerrylin.erp.initialize.config.RootConfig;
import com.jerrylin.erp.product.googleapps.service.ProductInfoService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = RootConfig.class)
public class ProductInfoServiceTests extends AbstractTransactionalJUnit4SpringContextTests{
	@Resource
	private ProductInfoService productInfoService;
	
	@Test
	public void execute(){
		productInfoService.execute();
	}
}
