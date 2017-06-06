package com.jerrylin.dynasql3;

import java.util.LinkedList;

import com.jerrylin.dynasql3.node.SqlNode;

public interface LastChildAliasible<Me extends SqlNode<?>> {
	public LinkedList<SqlNode<?>> getChildren();
	default Me as(String alias){
		LinkedList<SqlNode<?>> children = getChildren();
		if(Aliasible.class.isInstance(children.getLast())){
			Aliasible.class.cast(children.getLast()).as(alias);
		}
		return (Me)this;
	} 
}
