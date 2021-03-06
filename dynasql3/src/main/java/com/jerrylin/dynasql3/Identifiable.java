package com.jerrylin.dynasql3;

import java.io.Serializable;

public interface Identifiable<ID extends Serializable & Comparable<ID>, Me extends Identifiable<ID, ?>> {
	public ID id();
	public Me id(ID id);
}
