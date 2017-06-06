package com.jerrylin.dynasql3.util;

import com.jerrylin.dynasql3.node.SqlNode;

public interface SqlNodeSearchable {
	public void from(SqlNode<?> start);
	public <S extends SqlNode<?>>S find();
}
