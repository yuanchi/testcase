package com.jerrylin.erp.genserial;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;

import com.jerrylin.erp.initialize.config.RootConfig;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public class DefaultSerialTest {
	private DataSource dataSource;
	private LocalSessionFactoryBean sessionFactory;
	@Before
	public void init() throws Throwable{
		RootConfig rootConfig = new RootConfig();
		dataSource = rootConfig.dataSource();
		sessionFactory = rootConfig.sessionFactory(dataSource);
		sessionFactory.setAnnotatedClasses(DefaultSerial.class);
		sessionFactory.afterPropertiesSet(); // 如果沒有這一行LocalSessionFactoryBean.getObject會取不到SessionFactory，造成後續的NullPointerException
	}
	@After
	public void deinit(){
		sessionFactory.destroy();
		ComboPooledDataSource.class.cast(dataSource).close();
	}
	@Test
	public void insertIfNotExists(){
		Session s = null;
		Transaction tx = null;
		try{
			s = sessionFactory.getObject().openSession();
//			tx = s.beginTransaction();
			
			DefaultSerial t1 = (DefaultSerial)s.get(DefaultSerial.class, "t1");
			if(t1 == null){
				t1 = new DefaultSerial();
				t1.setId("t1");
				t1.setSep0("AP-");
				t1.setNo("000001");
				s.save(t1);
			}
			DefaultSerial t2 = (DefaultSerial)s.get(DefaultSerial.class, "t2");
			if(t2 == null){
				t2 = new DefaultSerial();
				t2.setId("t2");
				t2.setSep0("NO-");
				t2.setNo("000001");
				s.save(t2);
			}
			s.flush();
//			tx.commit();
		}catch(Throwable e){
//			if(tx != null){
//				tx.rollback();
//			}
			throw new RuntimeException(e);
		}finally{
			if(s != null){
				s.close();
			}
		}
	}
}
