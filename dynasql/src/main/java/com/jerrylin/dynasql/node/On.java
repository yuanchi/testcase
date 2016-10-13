package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class On extends Delimiter<On> {
	private static final long serialVersionUID = 778709235852211226L;
	@Override
	public String genSql(){
		String result = "";
		LinkedList<SqlNode<?>> children = getChildren();
		int len = children.size();
		if(len > 0){
			String indent = getStartSelectIndent();
			result = "ON " + IntStream.range(0, len)
				.boxed()
				.map(i->{
					SqlNode<?> n = children.get(i);
					if(i == 0){
						return n.genSql();
					}
					if(Operator.class.isInstance(n)){
						return "\n" + indent + n.genSql();
					}
					return " " + n.genSql();
				}).collect(Collectors.joining());
		}
		return result;
	}
	@Override
	public On copy(SqlNode<?>parent){
		On on = new On();
		if(parent!=null){
			parent.addCopy(on);
		}
		copyTo(on);
		return on;
	}
}
