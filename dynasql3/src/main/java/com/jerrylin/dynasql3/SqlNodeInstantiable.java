package com.jerrylin.dynasql3;

import com.jerrylin.dynasql3.node.SqlNode;

public interface SqlNodeInstantiable<T extends SqlNode<?>> {
	public T create();
}
