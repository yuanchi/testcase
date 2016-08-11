package com.jerrylin.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="inventory_option")
public class InventoryOption {
	private String id;
	private String name;
	private int plusOrMinus;
	@Id
	@Column(name="id")
	@GenericGenerator(name="jerrylin_inventory_option_id", strategy="com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator="jerrylin_inventory_option_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="plusOrMinus")
	public int getPlusOrMinus() {
		return plusOrMinus;
	}
	public void setPlusOrMinus(int plusOrMinus) {
		this.plusOrMinus = plusOrMinus;
	}
}
