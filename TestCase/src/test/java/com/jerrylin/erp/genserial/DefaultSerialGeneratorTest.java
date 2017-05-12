package com.jerrylin.erp.genserial;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.jboss.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.jerrylin.erp.initialize.config.RootConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DefaultSerialGeneratorTest {
	private static final Logger logger = Logger.getLogger(DefaultSerialGeneratorTest.class.getName());
	private DataSource dataSource;
	private LocalSessionFactoryBean sessionFactory;
	
	@Before
	public void init()throws Throwable{
		RootConfig rootConfig = new RootConfig();
		dataSource = rootConfig.dataSource();
		sessionFactory = rootConfig.sessionFactory(dataSource);
		sessionFactory.setAnnotatedClasses(DefaultSerial.class);
		sessionFactory.afterPropertiesSet();
	}
	@After
	public void deinit(){
		sessionFactory.destroy();
		ComboPooledDataSource.class.cast(dataSource).close();
	}
	@Test
	public void getNext(){
		DefaultSerialGenerator generator = new DefaultSerialGenerator("t1", sessionFactory.getObject());
		String nextNo = generator.getNext();
		logger.info("nextNo is " + nextNo);
		nextNo = generator.getNext();
		logger.info("nextNo is " + nextNo);
		nextNo = generator.getNext();
		logger.info("nextNo is " + nextNo);
	}
	@Test
	public void rollbackIfOuterSessionFail(){
		Session s = null;
		Transaction tx = null;
		try{
			s = sessionFactory.getObject().openSession();
			tx = s.beginTransaction();
			
			DefaultSerialGenerator generator = new DefaultSerialGenerator("t1", sessionFactory.getObject());
			generator.getNext(s);
			
			tx.rollback();
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		
	}
	@Test
	public void formatNo(){
		DefaultSerialGenerator generator = new DefaultSerialGenerator("t1");
		
		String expected = "0003";
		long num = 3;
		int len = 4;
		assertEquals(expected, generator.formatNo(num, len));
		
		expected = "00002";
		num = 2;
		len = 5;
		assertEquals(expected, generator.formatNo(num, len));
	}
	@Test
	public void incresetNo(){
		DefaultSerialGenerator generator = new DefaultSerialGenerator("t1");
		
		String expected = "16";
		String no = "15";
		assertEquals(expected, generator.increaseNo(no));
		
		expected = "0000899";
		no = "0000898";
		assertEquals(expected, generator.increaseNo(no));
	}
	@Test
	public void formatDate(){
		DefaultSerialGenerator generator = new DefaultSerialGenerator("t1");
		
		String expected = "02";
		Calendar current = Calendar.getInstance();
		String dateSep = "MM";
		String result = generator.formatDate(dateSep, current);
		assertEquals(expected, result);
		
		expected = "15";
		dateSep = "dd";
		result = generator.formatDate(dateSep, current);
		assertEquals(expected, result);
	}
}
