package com.jerrylin.erp.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;

public class HibernateAwareObjectMapper extends ObjectMapper {
	private static final long serialVersionUID = 7483518238091292897L;
	public HibernateAwareObjectMapper(){
		registerModule(new Hibernate4Module());
	}
}
