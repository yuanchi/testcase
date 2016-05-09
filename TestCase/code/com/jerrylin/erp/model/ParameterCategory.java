package com.jerrylin.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "shr_parameter_cat")
public class ParameterCategory {
	
	private String id;	
	private String name;
	private int type;

	@Id
	@GenericGenerator(name = "parameter_cat_id", strategy = "com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "parameter_cat_id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "type")
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
