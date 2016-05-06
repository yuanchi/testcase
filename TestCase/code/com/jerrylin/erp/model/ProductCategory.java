package com.jerrylin.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="productcategory")
public class ProductCategory {
	private String id;
	private String code;
	private String name;
	private String description;
	@Id
	@Column(name="id")
	@GenericGenerator(name="jerrylin_productcategory_id", strategy="com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator="jerrylin_productcategory_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="code")
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
