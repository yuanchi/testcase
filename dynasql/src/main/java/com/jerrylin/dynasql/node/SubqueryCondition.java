package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;

import com.jerrylin.dynasql.Dependable;

public class SubqueryCondition extends TargetExpressible<SubqueryCondition> implements FilterCondition, Dependable{
	private static final long serialVersionUID = 6215431775451549663L;
	private String dependencyMark;
	@Override
	public String genSql(){
		String result = "";
		LinkedList<SqlNode<?>>children = getChildren();
		if(children.size()!=0){
			result = children.stream()
				.map(n->{
					if(!SelectExpression.class.isInstance(n)){
						return n.genSql();
					}
					return genSubquerySql(n);
				}).collect(Collectors.joining(" "));
		}
		return result;
	}
	@Override
	public SubqueryCondition copy(SqlNode<?>parent){
		SubqueryCondition sc = new SubqueryCondition();
		if(parent!=null){
			parent.addCopy(sc);
		} 
		copyTo(sc);
		return sc;
	}
	@Override
	public void setDependencyMark(String dm) {
		this.dependencyMark = dm; 
	}
	@Override
	public String getDependencyMark() {
		return dependencyMark;
	}
}
