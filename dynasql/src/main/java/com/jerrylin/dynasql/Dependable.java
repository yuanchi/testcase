package com.jerrylin.dynasql;

import java.util.LinkedList;
import java.util.stream.Collectors;

import com.jerrylin.dynasql.node.SqlNode;


public interface Dependable {
	public void setDependencyMark(String dm);
	public String getDependencyMark();
	public SqlNode<?> getParent();
	public default void removeIncludingSiblings(){
		SqlNode<?> p = getParent();
		LinkedList<SqlNode<?>> children = p.getChildren();
		children.remove(this);
		String dm = getDependencyMark();
		if(dm==null){
			return;
		}
		children.stream().filter(n->{
			if(Dependable.class.isInstance(n)){
				Dependable d = (Dependable)n;
				if(dm.equals(d.getDependencyMark())){
					return true;
				}
			}
			return false;
		}).collect(Collectors.toList()).stream().forEach(n->{
			children.remove(n);
		});
	}
}
