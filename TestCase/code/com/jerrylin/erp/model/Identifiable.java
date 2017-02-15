package com.jerrylin.erp.model;

import java.io.Serializable;

public interface Identifiable<ID extends Serializable & Comparable<ID>>{
	public ID getId();
	public void setId(ID id);
}
