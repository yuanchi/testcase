package com.jerrylin.dynasql.node;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.jerrylin.dynasql.InParameter;

public abstract class SqlNode<T extends SqlNode<?>> implements Serializable{
	private static final long serialVersionUID = -2655112636221766773L;
	
	private Map<String, String> attributes;
	private SqlNode<?> parent;
	private LinkedList<SqlNode<?>> children;
	private SelectExpression root;
	private SelectExpression start;
	private LinkedList<SqlNode<?>> found;
	private String id;
	private LinkedList<InParameter> params = new LinkedList<>();
	String baseIndent = "  ";
	T getThisType(){
		return (T)this;
	}
	public <E extends SqlNode<E>>E cast(Class<E>clz){
		return clz.cast(this);
	}
	/**
	 * add child node
	 * @param child
	 * @return
	 */
	public T add(SqlNode<?> child){
		child.parent = this;
		child.root = this.root;
		child.start = this.start;
		getChildren().add(child);
		return getThisType();
	}
	/**
	 * the same as add method, but return parameter child
	 * @param child
	 * @return
	 */
	public <E extends SqlNode<E>>E addChild(E child){
		add(child);
		return child;
	}
	/**
	 * suggesting using this method when executing copy operation instead of add
	 * @param child
	 * @return
	 */
	T addCopy(SqlNode<?>child){
		T t = add(child);
		if(SelectExpression.class.isInstance(child)){// this code snippet is placed here for the method copy operating expectedly
			String preIndent = child.getStartSelectIndent();
			child.start = child.cast(SelectExpression.class); // when using a new SelectExpression, reset this value
			child.setStartSelectIndent(preIndent+baseIndent);
		}
		return t;
	}
	<E extends SqlNode<E>>E addCopyChild(E child){
		addCopy(child);
		return child;
	}
	public T find(Predicate<SqlNode<?>> filterIn){
		getFound().clear();
		find(this, filterIn);
		return getThisType();
	}
	private void find(SqlNode<?> n, Predicate<SqlNode<?>> filterIn){
		if(filterIn.test(n)){
			getFound().add(n);
		}
		n.getChildren().forEach(c->{
			find(c, filterIn);
		});
	}
	public <R extends SqlNode<R>>R findFirst(Class<R> clz){
		return findFirst(sn->clz.isInstance(sn));
	}
	public <R extends SqlNode<R>>R findFirst(Predicate<SqlNode<?>> filterIn){
		getFound().clear();
		findFirst(this, filterIn);
		if(getFound().isEmpty()){
			return null;
		}
		return (R)getFound().get(0);
	}
	private void findFirst(SqlNode<?> n , Predicate<SqlNode<?>> filterIn){
		if(!getFound().isEmpty()){
			return;
		}
		if(filterIn.test(n)){
			getFound().add(n);
			return;
		}
		n.getChildren().forEach(c->{
			findFirst(c, filterIn);
		});
	}
	public T consume(Consumer<SqlNode<?>> consumer){
		consume(this, consumer);
		return getThisType();
	}
	private void consume(SqlNode<?> n, Consumer<SqlNode<?>> consumer){
		consumer.accept(n);
		n.getChildren().forEach(c->{
			consume(c, consumer);
		});
	}
	/**
	 * find SqlNode with id, return the first result
	 * @param id
	 * @return
	 */
	public <R extends SqlNode<R>>R findById(String id){
		return findFirst(n->id.equals(n.id()));
	}
	public <R extends SqlNode<R>>R findByAlias(String alias){
		return findById(alias);
	}
	/**
	 * find SqlNode by InParameter id
	 * @param id
	 * @return
	 */
	public <R extends SqlNode<R>>R findNodeByParamId(String id){
		return findFirst(n->n.getParams().stream().filter(ip->id.equals(ip.getId())).findAny().isPresent());
	}
	/**
	 * find InParameter with InParameter id
	 * @param id
	 * @return
	 */
	public InParameter findParamById(String id){
		SqlNode<?> found = findNodeByParamId(id);
		return found.getParam(id);
	}
	/**
	 * find InParameter with SqlNode id
	 * @param nId
	 * @return
	 */
	public InParameter findParamByNodeId(String nId){
		SqlNode<?> found = findFirst(n->nId.equals(n.id()));
		return found.getParam();
	}
	/**
	 * find upward the closest parent node
	 * @param clz
	 * @return
	 */
	public <R extends SqlNode<R>>R closest(Class<R> clz){
		SqlNode<?> parent = getParent();
		while(parent!=null && !clz.isInstance(parent)){
			parent = parent.getParent();
		}
		if(parent==null){
			return null;
		}
		return clz.cast(parent);
	}
	public String attr(String name){
		return getAttributes().get(name);
	}
	public T attr(String name, String val){
		getAttributes().put(name, val);
		return getThisType();
	}
	public String id(){
		return id;
	}
	/**
	 * add id to current node
	 * @param id
	 * @return
	 */
	public T id(String id){
		this.id = id;
		return getThisType();
	}
	/**
	 * add id to last child node
	 * @param id
	 * @return
	 */
	public T lcId(String id){
		getChildren().getLast().id(id);
		return getThisType();
	}
	public abstract String genSql();
	public abstract T copy(SqlNode<?>parent);
	T copyTo(T n){
		if(attributes!=null){
			n.setAttributes(new LinkedHashMap<>(attributes));
		}
		n.id(id);
		if(!params.isEmpty()){
			n.setParams(new LinkedList<>(params));
		}
		getChildren().forEach(sn->{
			sn.copy(n);
		});
		return n;
	}
	public Map<String, String> getAttributes() {
		if(attributes==null){
			attributes = new LinkedHashMap<>();
		}
		return attributes;
	}
	public void setAttributes(Map<String, String> attributes) {
		this.attributes = attributes;
	}
	public SqlNode<?> getParent() {
		return parent;
	}
	public void setParent(SqlNode<?> parent) {
		this.parent = parent;
	}
	public LinkedList<SqlNode<?>> getChildren() {
		if(children==null){
			children = new LinkedList<>();
		}
		return children;
	}
	public void setChildren(LinkedList<SqlNode<?>> children) {
		this.children = children;
	}
	public SelectExpression getRoot() {
		return root;
	}
	public void setRoot(SelectExpression root) {
		this.root = root;
	}
	public SelectExpression getStart() {
		return start;
	}
	public void setStart(SelectExpression start) {
		this.start = start;
	}
	public LinkedList<SqlNode<?>> getFound() {
		if(found==null){
			found = new LinkedList<>();
		}
		return found;
	}
	public String getStartSelectIndent(){
		SelectExpression se = getStart();
		if(se == null || se.attr("indent") == null){
			return baseIndent;
		}
		return se.attr("indent");
	}
	public void setStartSelectIndent(String indent){
		SelectExpression se = getStart();
		if(se != null){
			se.attr("indent", indent);
		}
	}
	/**
	 * add InParameter to current node.
	 * @param ip
	 * @return
	 */
	public T addParam(InParameter ip){
		params.add(ip);
		return getThisType();
	}
	/**
	 * see the method {@link SqlNode#addParamValue(Object value)}.
	 * @param id
	 * @param value
	 * @return
	 */
	public T addParamValue(String id, Object value){
		InParameter ip = getParam(id);
		if(ip==null){
			ip = new InParameter();
		}
		ip.id(id).value(value);
		return addParam(ip);
	}
	/**
	 * add parameter value to current node
	 * @param value
	 * @return T extends SqlNode
	 */
	public T addParamValue(Object value){
		InParameter ip = new InParameter();
		ip.value(value);
		return addParam(ip);
	}
	/**
	 * add parameter value to last child node
	 * @param value
	 * @return T extends SqlNode
	 */
	public T lcParamValue(Object value){
		getChildren().getLast().addParamValue(value);
		return getThisType();
	}
	public InParameter getParam(){
		return params.listIterator().next();
	}
	public InParameter getParam(String id){
		Optional<InParameter> result = params.stream().filter(ip->id.equals(ip.getId())).findFirst();
		return result.isPresent() ? result.get() : null;
	}
	public InParameter getParam(int idx){
		return params.get(idx);
	}
	public LinkedList<InParameter> getParams(){
		return params;
	}
	public void setParams(LinkedList<InParameter> params){
		this.params = params;
	}
}
