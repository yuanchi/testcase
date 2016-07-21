package com.jerrylin.erp.security;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;

import com.jerrylin.erp.security.exception.SecurityRuntimeException;

@MappedSuperclass
public abstract class SecurityObject {

	private String id;
	private String name;
	private boolean readonly;
	
	@Id
	@GenericGenerator(name = "security_object_id", strategy = "com.jerrylin.erp.ds.TimeUID")
	@GeneratedValue(generator = "security_object_id")
	public String getId() {
		return id;
	}

	public void setId(String id) {
		if(this.readonly){
			throw new SecurityRuntimeException("object ["+getClass().getName()+"] is readonly!");
		}
		this.id = StringUtils.trim(id);
	}
	@Column(name = "name")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		if(this.readonly){
			throw new SecurityRuntimeException("object ["+getClass().getName()+"] is readonly!");
		}
		this.name = StringUtils.trim(name);
	}
	@Transient
	public boolean isReadonly() {
		return readonly;
	}

	public void setReadonly(boolean readonly) {
		this.readonly = readonly;
	}

	@Override
	public int hashCode(){
		if(this.id == null){
			return 0;
		}
		return this.id.hashCode();
	}
	
}
