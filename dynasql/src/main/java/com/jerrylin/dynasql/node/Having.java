package com.jerrylin.dynasql.node;

import java.util.LinkedList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Having extends Delimiter<Having> {
	private static final long serialVersionUID = -8274642948406452849L;
	@Override
	public String genSql(){
		String result = "";
		LinkedList<SqlNode<?>> children = getChildren();
		int len = children.size();
		if(len > 0){
			String indent = getStartSelectIndent();
			result = "HAVING " + IntStream.range(0, len)
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
	public Having copy(SqlNode<?>parent){
		Having having = new Having();
		if(parent!=null){
			parent.addCopy(having);
		}
		copyTo(having);
		return having;
	}
}
