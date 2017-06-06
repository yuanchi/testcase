package com.jerrylin.dynasql3;

import com.jerrylin.dynasql3.node.SqlNode;

public interface ChildAddible<Me extends SqlNode<?>> {
	public Me add(SqlNode<?> child);
	public <S extends SqlNode<?>> S createBy(Class<S> key);
}
