package com.jerrylin.erp.sql;

import java.util.List;

public interface ISqlNode {
	public String getId();
	public ISqlNode getParent();
	public void addChild(ISqlNode node);
	public List<ISqlNode> getChildren();
	public ISqlRoot getRoot();
	public ISqlNode root(ISqlRoot root);
	public ISqlNode parent(ISqlNode parent);
	public ISqlNode id(String id);
	public String genSql();
}
