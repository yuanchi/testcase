package com.jerrylin.erp.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
/**
 * Sql Tree Node Base Implementation
 * @author JerryLin
 *
 */
public abstract class SqlNode implements ISqlNode {
	private static final long serialVersionUID = -5581891859984293702L;
	private String id;
	private ISqlNode parent;
	private ISqlRoot root;
	private List<ISqlNode> children = new LinkedList<>();
	List<ISqlNode> founds = Collections.emptyList();
	
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
	/**
	 * the same as method find, return self/this node, not the found node
	 * if you want to retrieve found node directly, use method getFounds
	 */
	@Override
	public ISqlNode findNodeById(String id){
		find(n->id.equals(n.getId()));
		return this;
	}
	@Override
	public ISqlNode find(Predicate<ISqlNode> validation) {
		List<ISqlNode> matches = new ArrayList<>();
		find(matches, this, validation);
		this.founds = matches;
		return this;
	}
	private void find(List<ISqlNode> matches, ISqlNode node , Predicate<ISqlNode> validation){
		if(validation.test(node)){
			matches.add(node);
		}
		node.getChildren().forEach(child->{
			find(matches, child, validation);
		});
	}
	/**
	 * update found nodes;
	 * before calling this method, you should call find or findNodeById first.
	 */
	@Override
	public ISqlNode update(Consumer<ISqlNode> update) {
		this.founds.forEach(f->{
			update.accept(f);
		});
		return this;
	}
	@Override
	public List<ISqlNode> getFounds(){
		return this.founds;
	}
}
