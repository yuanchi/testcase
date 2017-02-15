package com.jerrylin.erp.model;

import java.io.Serializable;

public abstract class AngrycatEntity<ID extends Serializable & Comparable<ID>, T extends AngrycatEntity<ID, T>> implements Identifiable<ID> , Comparable<T>, Serializable{
	private static final long serialVersionUID = 5361442075391258689L;
	public boolean isNew(){
		return getId() == null;
	}
	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		if(obj == this){
			return true;
		}
		if(obj.getClass() != this.getClass()){
			return false;
		}
		ID id = getId();
		if(id == null){
			return false;
		}
		AngrycatEntity<ID, T> entity =  AngrycatEntity.class.cast(obj);
		return id.equals(entity.getId());
	}
	@Override
	public int hashCode(){
		int hash = 7;
		ID id = getId();
		hash = 31 * hash + ((id == null) ? 0 : id.hashCode());
		return hash;
	}
	public int compareTo(T obj){
		if(this == obj){
			return 0;
		}
		return getId().compareTo(obj.getId());
	}
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		sb.append("entity.");
		sb.append(this.getClass().getSimpleName());
		sb.append("<");
		sb.append(getId());
		sb.append("-");
		sb.append(super.toString());
		sb.append(">");
		return sb.toString();
	}
}
