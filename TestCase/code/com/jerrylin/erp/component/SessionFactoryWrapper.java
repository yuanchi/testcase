package com.jerrylin.erp.component;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class SessionFactoryWrapper {
	@Autowired
	private LocalSessionFactoryBean lsfb;
	
	public LocalSessionFactoryBean getLocalSessionFactoryBean(){
		return lsfb;
	}
	public SessionFactory getSessionFactory(){
		return lsfb.getObject();
	}
	public Session getCurrentSession(){
		return getSessionFactory().getCurrentSession();
	}
	public Session openSession(){
		return getSessionFactory().openSession();
	}
	public void executeSession(Consumer<Session> execute){
		Session s = null;
		try{
			s = openSession();
			execute.accept(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
	}
	public <T>List<T> executeSession(Function<Session, List<T>> execute){
		Session s = null;
		List<T> results = Collections.emptyList();
		try{
			s = openSession();
			results = execute.apply(s);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return results;
	}
	public void executeTransaction(Consumer<Session> execute){
		Session s = null;
		Transaction tx = null;
		try{
			s = openSession();
			tx = s.beginTransaction();
			execute.accept(s);
			tx.commit();
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
	}
	public <T>T executeTransaction(Function<Session, T> execute){
		Session s = null;
		Transaction tx = null;
		T t = null;
		try{
			s = openSession();
			tx = s.beginTransaction();
			t = execute.apply(s);
			tx.commit();
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
		return t;
	}
}
