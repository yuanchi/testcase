package com.jerrylin.gentest;

import static com.jerrylin.gentest.TestValuUtils.randomEmail;
import static com.jerrylin.gentest.TestValuUtils.randomSqlDate;
import static com.jerrylin.gentest.TestValuUtils.randomString;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
public class Customer {
	private static final LocalDate START = LocalDate.of(1900, 1, 1);
	private static final LocalDate END = LocalDate.of(2000, 12, 31);
	private int id;
	private String name;
	private String email;
	private Date birth;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Date getBirth() {
		return birth;
	}
	public void setBirth(Date birth) {
		this.birth = birth;
	}
	public static Customer mockCustomer(){
		Customer cus = new Customer();
		cus.setBirth(randomSqlDate(START, END));
		cus.setName(randomString(5));
		cus.setEmail(randomEmail());
		return cus;
	}
	public static List<Customer> mockCustomers(int count){
		List<Customer> c = new ArrayList<>();
		for(int i=0; i<count; i++){
			c.add(mockCustomer());
		}
		return c;
	}
}
