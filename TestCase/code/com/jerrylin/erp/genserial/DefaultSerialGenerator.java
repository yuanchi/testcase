package com.jerrylin.erp.genserial;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class DefaultSerialGenerator extends SerialGenerator<Session> {
	private String id;
	private SessionFactory sessionFactory;
	public DefaultSerialGenerator(String id){
		this.id = id;
	}
	public DefaultSerialGenerator(String id, SessionFactory sessionFactory){
		this(id);
		this.sessionFactory = sessionFactory;
	}
	@Override
	protected String getId() {
		return id;
	}
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	@Override
	protected String getNext(){
		Session s = null;
		Transaction tx = null;
		try{
			s  = sessionFactory.openSession();
			tx = s.beginTransaction();
			String nextNo = getNext(s);
			tx.commit();
			return nextNo;
		}catch(Throwable e){
			tx.rollback();
			throw new RuntimeException(e);
		}finally{
			s.close();
		}
	}
	/**
	 * 如果有需要rollback等機制，可以透過這個method在同一個Session操作
	 * @param s
	 * @return
	 * @throws Throwable
	 */
	protected String getNext(Session s){
		DefaultSerial ds = (DefaultSerial)s.load(DefaultSerial.class, this.id);
		String nextNo = getNext(ds);
		s.flush();
		return nextNo;
	}
	protected String getNext(DefaultSerial ds){
		StringBuffer sb = new StringBuffer();
		Calendar c = Calendar.getInstance();
		if(StringUtils.isNotBlank(ds.getSep0())){
			sb.append(ds.getSep0());
		}
		if(StringUtils.isNotBlank(ds.getDateSep0())){
			sb.append(formatDate(ds.getDateSep0(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep1())){
			sb.append(ds.getSep1());
		}
		if(StringUtils.isNotBlank(ds.getDateSep1())){
			sb.append(formatDate(ds.getDateSep1(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep2())){
			sb.append(ds.getSep2());
		}
		if(StringUtils.isNotBlank(ds.getDateSep2())){
			sb.append(formatDate(ds.getDateSep2(), c));
		}
		if(StringUtils.isNotBlank(ds.getSep3())){
			sb.append(ds.getSep3());
		}
		String resetNoField = ds.getResetNoField();
		if(StringUtils.isNotBlank(resetNoField)){
			try{
				String v = (String)PropertyUtils.getProperty(ds, resetNoField);
				String dateString = formatDate(v, c);
				if(StringUtils.isNotBlank(ds.getResetNoFieldLastValue())
				&& !dateString.equals(ds.getResetNoFieldLastValue())){
					ds.setNo(formatNo(ds.getResetNoTo(), ds.getNo().length()));
				}
				ds.setResetNoFieldLastValue(dateString);
			}catch(Throwable e){
				throw new RuntimeException(e);
			}
		}
		if(StringUtils.isNotBlank(ds.getNo())){
			sb.append(ds.getNo());
			ds.setNo(increaseNo(ds.getNo()));
		}
		if(StringUtils.isNotBlank(ds.getSep4())){
			sb.append(ds.getSep4());
		}
		return sb.toString();
	}
	protected String formatDate(String dateSep, Calendar c){
		// ROC代表民國年
		// ROC3代表民國年，並且總是有三位數，不足左邊補零
		if("ROC".equals(dateSep) || "ROC3".equals(dateSep)){
			int year = c.get(Calendar.YEAR) - 1911;
			if("ROC".equals(dateSep)){
				return String.valueOf(year);
			}
			if(year < 100){
				return "0" + String.valueOf(year);
			}
			return String.valueOf(year);
		}
		SimpleDateFormat df = new SimpleDateFormat(dateSep);
		return df.format(c.getTime());
	}
}
