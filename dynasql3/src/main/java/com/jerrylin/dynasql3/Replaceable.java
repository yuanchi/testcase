package com.jerrylin.dynasql3;

import java.util.List;

import com.jerrylin.dynasql3.node.SqlNode;

public interface Replaceable {
	public SqlNode<?> getParent();
	public void setParent(SqlNode<?> parent);
	/**
	 * this method would set original SqlNode's parent as null
	 * @param replacing
	 * @return
	 */
	default <S extends SqlNode<?>>S replaceWith(S replacing){
		SqlNode<?> p = getParent();
		List<SqlNode<?>> children = p.getChildren();
		Integer replacedIdx = null;
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> c = children.get(i);
			if(c == this){
				replacedIdx = i;
				break;
			}
		}
		children.set(replacedIdx, replacing);
		replacing.setParent(p);
		this.setParent(null);
		return replacing;
	}
}
