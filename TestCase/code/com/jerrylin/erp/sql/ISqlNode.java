package com.jerrylin.erp.sql;

import java.io.Serializable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
/**
 * Sql Tree Base Interface
 * @author JerryLin
 *
 */
public interface ISqlNode extends Serializable{
	
	public String getId();
	public ISqlNode getParent();
	public void addChild(ISqlNode node);
	public List<ISqlNode> getChildren();
	public ISqlRoot getRoot();
	public ISqlNode root(ISqlRoot root);
	public ISqlNode parent(ISqlNode parent);
	public ISqlNode id(String id);
	public String genSql();
	/**
	 * 單一節點的複製，不牽涉到root、parent、children
	 * @return
	 */
	public ISqlNode singleCopy();
	public ISqlNode findNodeById(String id);
	public ISqlNode find(Predicate<ISqlNode> validation);
	public ISqlNode update(Consumer<ISqlNode> update);
}
