package com.jerrylin.erp.initialize.config;

import java.sql.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import com.jerrylin.erp.model.Member;
import com.jerrylin.erp.model.VipDiscountDetail;
import com.jerrylin.erp.sql.SqlRoot;
import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@ComponentScan(basePackages={"com.jerrylin.erp.service", "com.jerrylin.erp.component", "com.jerrylin.erp.query", "com.jerrylin.erp.ds"})
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
		ds.setJdbcUrl("jdbc:mariadb://localhost:3306/angrycat");
		ds.setUser("root");
		ds.setPassword("root");
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
	
	@Bean
	public Map<Class<?>, Map<String, Class<?>>> classFieldType(){
		Map<Class<?>, Map<String, Class<?>>> types = new LinkedHashMap<>();
		
		Map<String, Class<?>> memberFieldType = new LinkedHashMap<>();
		memberFieldType.put("id", String.class);
		memberFieldType.put("important", Boolean.class);
		memberFieldType.put("name", String.class);
		memberFieldType.put("nameEng", String.class);
		memberFieldType.put("fbNickname", String.class);
		memberFieldType.put("gender", Integer.class);
		memberFieldType.put("idNo", String.class);
		memberFieldType.put("birthday", Date.class);
		memberFieldType.put("email", String.class);
		memberFieldType.put("mobile", String.class);
		memberFieldType.put("tel", String.class);
		memberFieldType.put("postalCode", String.class);
		memberFieldType.put("address", String.class);
		memberFieldType.put("toVipDate", Date.class);
		memberFieldType.put("toVipEndDate", Date.class);
		memberFieldType.put("note", String.class);
		memberFieldType.put("clientId", String.class);
		memberFieldType.put("vipDiscountDetails", List.class);
		types.put(Member.class, memberFieldType);
		
		Map<String, Class<?>> vipDiscountDetailFieldType = new LinkedHashMap<>();
		vipDiscountDetailFieldType.put("id", String.class);
		vipDiscountDetailFieldType.put("memberId", String.class);
		vipDiscountDetailFieldType.put("memberIdNo", String.class);
		vipDiscountDetailFieldType.put("effectiveStart", Date.class);
		vipDiscountDetailFieldType.put("effectiveEnd", Date.class);
		vipDiscountDetailFieldType.put("discountUseDate", Date.class);
		vipDiscountDetailFieldType.put("toVipDate", Date.class);
		vipDiscountDetailFieldType.put("available", Boolean.class);
		vipDiscountDetailFieldType.put("useStatus", String.class);
		types.put(VipDiscountDetail.class, vipDiscountDetailFieldType);
		
		return types;
	}
}
