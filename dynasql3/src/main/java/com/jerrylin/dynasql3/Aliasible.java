package com.jerrylin.dynasql3;

import com.jerrylin.dynasql3.node.SqlNode;

public interface Aliasible<Me extends SqlNode<?>> {
	public String getAlias();
	public void setAlias(String alias);
	
	default Me as(String alias){
		setAlias(alias);
		return (Me)this;
	}
}
