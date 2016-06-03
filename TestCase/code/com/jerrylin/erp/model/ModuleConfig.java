package com.jerrylin.erp.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table(name="shr_module_config")
public class ModuleConfig {
	private String id;
	private String name;
	private String moduleName;
	private String json;
	@Id
	@Column(name="id")
	@GenericGenerator(name="jerrylin_moduleconfig_id", strategy="com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator="jerrylin_moduleconfig_id")
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Column(name="name", columnDefinition="設定名稱")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Column(name="moduleName", columnDefinition="模組名稱")
	public String getModuleName() {
		return moduleName;
	}
	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
	@Column(name="json", columnDefinition="json")
	public String getJson() {
		return json;
	}
	public void setJson(String json) {
		this.json = json;
	}
}
