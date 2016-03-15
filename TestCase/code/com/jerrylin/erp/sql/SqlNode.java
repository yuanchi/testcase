package com.jerrylin.erp.sql;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public abstract class SqlNode implements ISqlNode {
	private String id;
	private ISqlNode parent;
	private ISqlRoot root;
	private List<ISqlNode> children = new LinkedList<>();
	
	public ISqlNode root(ISqlRoot root){
		this.root = root;
		return this;
	}
	public ISqlNode parent(ISqlNode parent){
		this.parent = parent;
		return this;
	}
	public ISqlNode id(String id){
		this.id = id;
		return this;
	}
	
	@Override
	public ISqlRoot getRoot() {
		return this.root;
	}
	@Override
	public ISqlNode getParent() {
		return this.parent;
	}
	@Override
	public String getId() {
		return this.id;
	}


	@Override
	public void addChild(ISqlNode node) {
		String nodeId = node.getId();
		if(StringUtils.isNotBlank(nodeId)){
			children.forEach(n->{
				if(nodeId.equals(n.getId())){
					throw new RuntimeException("SqlNode id has existed: " + nodeId);
				}
			});
		}
		node.root(this.getRoot());
		node.parent(this);
		children.add(node);
	}

	@Override
	public List<ISqlNode> getChildren() {
		return this.children;
	}


}
