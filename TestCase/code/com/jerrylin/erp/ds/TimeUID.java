package com.jerrylin.erp.ds;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGenerator;


public class TimeUID implements IdentifierGenerator {
	private static final DateFormat DF = new SimpleDateFormat("yyyyMMdd-HHmmssSSS-");
	private static long lastTimeSequence;
	private static final String TAG_ASSIGNED_ID = "TAG_ASSIGNED_ID776shJ63GY4R663";
	private static boolean assignedId = false;
	public static String getTagAssignedId(String id){
		return TAG_ASSIGNED_ID + id;
	}
	public static void setAssignedId(boolean ai){
		assignedId = ai;
	}
	public static synchronized String generateByHand(){
		long timeSequence = System.currentTimeMillis();
		if(timeSequence <= lastTimeSequence){
			timeSequence = lastTimeSequence + 1L;
		}
		lastTimeSequence = timeSequence;
		return DF.format(new Date(timeSequence)) + RandomStringUtils.randomAlphabetic(5);
	}
	
	@Override
	public Serializable generate(SessionImplementor sessoinImpl, Object obj)
			throws HibernateException {
		if(assignedId){
			try{
				String docId = BeanUtils.getProperty(obj, "id");
				if(StringUtils.isNotBlank(docId) && docId.startsWith(TAG_ASSIGNED_ID)){
					String assignedId = StringUtils.substringAfter(docId, TAG_ASSIGNED_ID);
					if(StringUtils.isNotBlank(assignedId)){
						return assignedId;
					}
				}
			}catch(Exception e){
				throw new HibernateException(e);
			}
		}
		return generateByHand();
	}

}
