package com.jerrylin.erp.initialize.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.jerrylin.erp.sql.SqlRoot;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@ComponentScan(basePackages={"com.jerrylin.erp.service", "com.jerrylin.erp.component", "com.jerrylin.erp.query", "com.jerrylin.erp.ds", "com.jerrylin.erp.product"})
@EnableTransactionManagement(proxyTargetClass=true)
public class RootConfig {
	public static final String DEFAULT_BATCH_SIZE = "100";
	
	@Bean(destroyMethod="close")
	public DataSource dataSource(){
		ComboPooledDataSource ds = new ComboPooledDataSource();
		try{
			ds.setDriverClass("org.mariadb.jdbc.Driver");
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		ds.setJdbcUrl("");
		ds.setUser("");
		ds.setPassword("");
		ds.setInitialPoolSize(5);
		ds.setMinPoolSize(5);
		ds.setMaxPoolSize(20);
		ds.setAcquireIncrement(3);
		ds.setMaxStatements(50);
		ds.setCheckoutTimeout(1800);
		
		return ds;
	}
	
	@Bean
	public LocalSessionFactoryBean sessionFactory(DataSource dataSource){
		LocalSessionFactoryBean sfb = new LocalSessionFactoryBean();
		sfb.setDataSource(dataSource);
		sfb.setPackagesToScan("com.jerrylin.erp.model");
		Properties props = new Properties();
		props.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		props.setProperty("hibernate.show_sql", "false");
		props.setProperty("hibernate.jdbc.batch_size", DEFAULT_BATCH_SIZE);
		sfb.setHibernateProperties(props);		
		return sfb;
		
	}
	
	@Bean
	public PlatformTransactionManager transactionManager(LocalSessionFactoryBean sessionFactory){
		HibernateTransactionManager htm = new HibernateTransactionManager();
		htm.setSessionFactory(sessionFactory.getObject());
		return htm;
	}
	
	@Bean
	@Scope("prototype")
	public SqlRoot getSqlRoot(){
		return SqlRoot.getInstance();
	}
}
