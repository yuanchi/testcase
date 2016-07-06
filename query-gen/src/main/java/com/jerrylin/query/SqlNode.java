package com.jerrylin.query;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
/**
 * basic unit for composing the complete sql structure<br>
 * providing multiple methods to search or manipulate nodes
 * @author JerryLin
 *
 */
public abstract class SqlNode{
	public static final String ID = "id";
	
	private SqlNode parent;
	private List<SqlNode> children = new LinkedList<>();
	String baseIndent = "  ";
	private Map<String, String> attributes = new LinkedHashMap<>();
	private List<SqlNode> found = new LinkedList<>();
	
	public SqlNode addChildren(SqlNode... children){
		Arrays.asList(children).forEach(child->{
			child.parent = this;
			this.children.add(child);
		});
		return this;
	}
	public SqlNode getParent(){
		return this.parent;
	}
	public <T extends SqlNode>T getParent(Class<T>clz){
		return clz.cast(this.parent);
	}
	public List<SqlNode> getChildren(){
		return this.children;
	}
	/**
	 * representing upward to closest SelectExpression
	 * @return
	 */
	public SelectExpression toStart(){
		return closest(SelectExpression.class);
	}
	/**
	 * representing upward to the most top SelectExpression
	 * @return
	 */
	public SelectExpression toRoot(){
		SelectExpression start = toStart();
		while(start != null && start.getParent() != null){
			start = start.toStart();
		}
		return start;
	}
	/**
	 * representing upward to closest SelectExpression's parent
	 * @param clz
	 * @return
	 */
	public <T extends TargetSelectable>T pre(Class<T> clz){
		SqlNode parent = toStart().getParent();
		if(parent.getClass() == clz){
			return clz.cast(parent);
		}
		String simpleName = clz.getSimpleName();
		throw new RuntimeException("SqlNode.pre"+simpleName+"(): parent is NOT "+simpleName+" type");
	}
	/**
	 * upward to Select<br>
	 * also see the method {@link #pre(Class<T> clz)}.
	 * @return
	 */
	public Select preSelect(){
		return pre(Select.class);
	}
	/**
	 * upward to From<br>
	 * also see the method {@link #pre(Class<T> clz)}.
	 * @return
	 */
	public From preFrom(){
		return pre(From.class);
	}
	/**
	 * upward to OrderBy<br>
	 * also see the method {@link #pre(Class<T> clz)}.
	 * @return
	 */
	public OrderBy preOrderBy(){
		return pre(OrderBy.class);
	}
	/**
	 * representing upward to closest SqlNode
	 * @return
	 */
	public <T extends SqlNode>T closest(Class<T>clz){
		SqlNode parent = getParent();
		while(parent != null && parent.getClass() != clz){
			parent = parent.getParent();
		}
		return clz.cast(parent);
	}
	public SqlNode attr(String key, String val){
		attributes.put(key, val);
		return this;
	}
	public String attr(String key){
		return attributes.get(key);
	}
	Map<String, String> getAttributes(){
		return this.attributes;
	}
	public <T extends SqlNode>T findById(String id){
		find(n->{
			return id.equals(n.attr(ID));
		});
		if(found.size() > 0){
			return (T)found.get(0);
		}
		return null;
	}
	public <T extends SqlNode>SqlNode findByType(Class<T>clz){
		find(n->clz.isInstance(n));
		return this;
	}
	public <T extends SqlNode>T findByType(Class<T>clz, int idx){
		findByType(clz);
		int count = found.size();
		if(count > idx){
			return (T)found.get(idx);
		}
		return null;
	}
	public <T extends SqlNode>List<T> getFound(Class<T> clz){
		return (List<T>)found;
	}	
	public <T extends SqlNode>T findFirstByType(Class<T>clz){
		return findByType(clz, 0);
	}
	public SqlNode find(Predicate<SqlNode> predicate){
		found.clear();
		find(this, predicate);
		return this;
	}
	/**
	 * iterate all SqlNodes(that is, all nodes are included within found property)
	 * @return
	 */
	public <T extends SqlNode>T iterate(){
		find(sn->sn instanceof SqlNode);
		return (T)this;
	}
	private void find(SqlNode node, Predicate<SqlNode> predicate){
		if(predicate.test(node)){
			found.add(node);
		}
		node.getChildren().forEach(n->{
			find(n ,predicate);
		});
	}
	public <T extends SqlNode>T copy(){
		return (T)copy(this);
	}
	public <T extends SqlNode> T copyAs(Class<T>clz){
		return clz.cast(copy());
	}
	private SqlNode copy(SqlNode node){
		SqlNode copy = copyAttributes(node);
		if(ConditionValue.class.isInstance(node)){
			ConditionValue.class.cast(copy).setVal(ConditionValue.class.cast(node).getVal());
		}
		node.getChildren().forEach(n->{
			SqlNode childCopy = copy(n);
			copy.addChildren(childCopy);
		});
		return copy;
	}
	private SqlNode copyAttributes(SqlNode node){
		SqlNode n = null;
		try{
			n = node.newInstance();
			n.attributes = new LinkedHashMap<>(node.attributes);
		}catch(Throwable e){
			throw new RuntimeException(e);
		}
		return n;
	}
	/**
	 * configuring id
	 * @param id
	 * @return
	 */
	public <T extends SqlNode>T id(String id){
		attr(ID, id);
		return (T)this;
	}
	/**
	 * configuring id and confirming type(for chaining convenience)
	 * @param id
	 * @param clz
	 * @return
	 */
	public <T extends SqlNode>T id(String id, Class<T>clz){
		return id(id);
	}
	public SqlNode transform(Consumer<SqlNode> consumer){
		found.stream().forEach(sn->{
			consumer.accept(sn);
		});
		return this;
	}
	public SqlNode remove(Consumer<SqlNode> before){
		found.forEach(sn->{
			if(before!=null){
				before.accept(sn);
			}
			SqlNode parent = sn.getParent();
			if(parent != null){
				parent.getChildren().remove(sn);
			}
		});
		found.clear();
		return this;
	}
	public <T extends SqlNode>T findChildExact(Class<T>clz){
		Optional<SqlNode> first = getChildren().stream().filter(sn-> sn.getClass() == clz).findFirst();
		if(first.isPresent()){
			return clz.cast(first.get());
		}
		return null;
	}
	public boolean childrenExisted(Class<?>...clz){
		long count = 
		getChildren().stream().filter(sn->{
			for(Class<?> c : clz){
				if(c.isInstance(sn)){
					return true;
				}
			}
			return false;
		}).count();
		System.out.println("count: " + count);
		return count > 0;
	}
	public abstract String genSql();
	public abstract SqlNode newInstance();
}
