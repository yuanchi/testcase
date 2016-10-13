package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Where extends Delimiter<Where> {
	private static final long serialVersionUID = -966979317730256808L;
	@Override
	public String genSql(){
		String result = "";
		LinkedList<SqlNode<?>> children = getChildren();
		int len = children.size();
		if(len > 0){
			String indent = getStartSelectIndent();
			result = "WHERE " + IntStream.range(0, len)
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
	public Where copy(SqlNode<?>parent){
		Where where = new Where();
		if(parent!=null){
			parent.addCopy(where);
		}
		copyTo(where);
		return where;
	}
}
