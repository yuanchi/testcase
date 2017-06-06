package com.jerrylin.dynasql3.node;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.jerrylin.dynasql3.Identifiable;
import com.jerrylin.dynasql3.SqlNodeFactory;
import com.jerrylin.dynasql3.util.SqlNodeSearchable;
/**
 * 
 * @author JerryLin
 *
 * @param <T>
 */
public class SqlNode<T extends SqlNode<?>> implements Serializable, Identifiable<String, T>{
	private static final long serialVersionUID = 9074894360149912315L;
	
	private SqlNode<?> parent;
	private LinkedList<SqlNode<?>> children = new LinkedList<>();
	private SqlNodeFactory factory;
	private Class<SqlNode<?>> keyToInitialized;
	private String id;
	
	T thisType(){
		return (T)this;
	}
	@Override
	public String getId() {
		return id;
	}
	@Override
	public T setId(String id) {
		this.id = id;
		return thisType();
	}
	public T add(SqlNode<?> child){
		child.parent = this;
		children.add(child);
		return thisType();
	}
	/**
	 * change this instance status, and return its type
	 * @param consumer
	 * @return
	 */
	public T config(Consumer<T> consumer){
		consumer.accept(thisType());
		return thisType();
	}
	/**
	 * copy not including parent field<br>
	 * if subclass has its own fields, this method should be override<br>
	 * if want to test this method, starting node should be initialized via SqlNodeFactory.create().<br>
	 * SqlNodeFactory.create() can guarantee the correct initializing key.<br>
	 * it's possible to change factory after a SqlNode being initialized,<br>
	 * if so, the copying result may not be expected.
	 * @return
	 */
	public T copy(){
		Class<SqlNode<?>> key = keyToInitialized;
		if(key == null){
			key = (Class<SqlNode<?>>) this.getClass();
		}
		SqlNode<?> copy = createBy(key);
		// TODO other fields...
		copy.setId(id);
		copy.factory = factory;
		for(int i = 0; i < children.size(); i++){
			SqlNode<?> child = children.get(i);
			copy.add(child.copy());
		}
		return (T)copy;
	}
	public <S extends SqlNode<?>> S topMost(){
		SqlNode<?> p = parent;
		SqlNode<?> f = this;
		while(p != null){
			if(f == p){
				break;
			}			
			f = f.parent;
			p = f.parent;
		}
		return (S)f;
	}
	/**
	 * access top least SqlNode, not including self<br>
	 * if not found, return null
	 * @param target
	 * @return
	 */
	public <S extends SqlNode<?>> S topLeast(Class<S> target){
		SqlNode<?> f = this;
		while(f.parent != null){
			f = f.parent;
			if(target.isInstance(f)){
				return (S)f;
			}
		}
		throw new RuntimeException("Top Least SqlNode with type[" + target.getName() + "] NOT FOUND");
	}
	public <S extends SqlNode<?>> S findFirst(Predicate<SqlNode<?>> predicate){
		SqlNode<?> f = null;
		for(SqlNode<?> c : getChildren()){
			if(predicate.test(c)){
				f = c;
				break;
			}
			SqlNode<?> f2 = c.findFirst(predicate);
			if(f2 != null){
				f = f2;
				break;
			}
		}
		return (S)f;
	}
	public <S extends SqlNode<?>> List<S> findAll(Predicate<SqlNode<?>> predicate){
		List<S> collect = new ArrayList<>();
		for(SqlNode<?> c : getChildren()){
			if(predicate.test(c)){
				collect.add((S)c);
			}
			List<SqlNode<?>> f = c.findAll(predicate);
			if(!f.isEmpty()){
				collect.addAll((List<S>)f);
			}
		}
		return collect;
	}
	public <S extends SqlNode<?>>S find(Predicate<SqlNode<?>> predicate, int seq){
		List<SqlNode<?>> collect = new ArrayList<>();
		SqlNode<?> f = null;
		for(SqlNode<?> c : getChildren()){
			if(predicate.test(c)){
				collect.add(c);
			}
			if(collect.size() >= seq){
				f = collect.get(seq-1);
				break;
			}
			List<SqlNode<?>> found = c.findAll(predicate);
			if(!found.isEmpty()){
				collect.addAll(found);
				if(collect.size() >= seq){
					f = collect.get(seq-1);
					break;
				}
			}
		}
		return (S)f;
	}
	public <S extends SqlNode<?>> S findWith(SqlNodeSearchable strategy){
		strategy.from(this);
		return strategy.find();
	}
	public SelectExpression<?> toStart(){
		SelectExpression<?> start = topLeast(SelectExpression.class);
		return start;
	}
	public SqlNodeFactory getFactory(){
		return factory;
	}
	public T setFactory(SqlNodeFactory factory){
		this.factory = factory;
		return thisType();
	}
	public SqlNodeFactory getTopMostFactory(){
		return topMost().getFactory();
	}
	public T setTopMostFactory(SqlNodeFactory factory){
		topMost().setFactory(factory);
		return thisType();
	}
	/**
	 * to initialize SqlNode instance with the topmost node's factory.<br>
	 * if different node trees are combined together,<br>
	 * the factory to initialize may be different.
	 * @param key
	 * @return
	 */
	public <S extends SqlNode<?>> S createBy(Class<S> key){
		SqlNode<?> topMost = topMost();
		if(topMost.getFactory() == null){
			topMost.setFactory(SqlNodeFactory.getSingleton());
		}
		return topMost.getFactory().create(key);
	}
	/**
	 * this key comes from SqlNodeFactory.create(), for copying node
	 * @param keyToInitialized
	 */
	public void setKeyToInitialized(Class<SqlNode<?>> keyToInitialized){
		this.keyToInitialized = keyToInitialized;
	}
	public SqlNode<?> getParent() {
		return parent;
	}
	public void setParent(SqlNode<?> parent) {
		this.parent = parent;
	}
	public LinkedList<SqlNode<?>> getChildren() {
		return children;
	}
	public void setChildren(LinkedList<SqlNode<?>> children) {
		this.children = children;
	}
	
	/**
	 * suggest that all sub classes should override this method<br>
	 * TODO currently most SqlNodes do not yet implement this method 
	 * @return
	 */
	public String toSql(){
		return "Not implemented";
	}
	/**
	 * this method supposed to output well-formed sql string<br>
	 * if not overridden, default is to invoke toSqlWith(indent)
	 * @return
	 */
	public String toSqlf(){
		return toSqlWith("");
	}
	public String toSqlWith(String indent){
		return indent + toSql();
	}
}
