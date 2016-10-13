package com.jerrylin.dynasql;

import com.jerrylin.dynasql.node.SqlNode;

public interface Aliasible<T extends SqlNode<T>> {
	public String getAlias();
	public void setAlias(String alias);
	public T as(String alias);
}
