package com.jerrylin.erp.model;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name = "shr_parameter")
public class Parameter {
	private String id;	
	private String code;	
	private String nameDefault;
	private Map<String, String> localeNames = new HashMap<>();	
	private String note;	
	private int sequence;
	private ParameterCategory parameterCategory;

	@Id
	@GenericGenerator(name = "parameter_id", strategy = "com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "parameter_id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	@Column(name = "code")
	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	@Column(name = "name")
	public String getNameDefault() {
		return nameDefault;
	}

	public void setNameDefault(String nameDefault) {
		this.nameDefault = nameDefault;
	}
	@ElementCollection(fetch=FetchType.EAGER)
	@MapKeyColumn(name="localString")
	@Column(name="name")
	@CollectionTable(name="shr_parameter_i18n", joinColumns=
	@JoinColumn(name="parameterId"))
	public Map<String, String> getLocaleNames() {
		return localeNames;
	}

	public void setLocaleNames(Map<String, String> localeNames) {
		this.localeNames = localeNames;
	}
	@Column(name="note")
	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	@Column(name="sequence")
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
	@ManyToOne(fetch=FetchType.EAGER)
	@JoinColumn(name="categoryId")
	public ParameterCategory getParameterCategory() {
		return parameterCategory;
	}

	public void setParameterCategory(ParameterCategory parameterCategory) {
		this.parameterCategory = parameterCategory;
	}
	
	
}
