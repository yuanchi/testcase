package com.jerrylin.dynasql.node;

import java.util.List;
import java.util.stream.Collectors;

public class Select extends TargetExpressible<Select> {
	private static final long serialVersionUID = 2140990609623812639L;
	@Override
	public String genSql(){
		String result = "";
		List<SqlNode<?>> children = getChildren();
		if(children.size()>0){
			String indent = getStartSelectIndent();
			result = "SELECT " + children.stream().map(n->{
				if(SelectExpression.class.isInstance(n)){
					return genSubquerySql(n);
				}
				return n.genSql();
			}).collect(Collectors.joining(",\n"+indent));
		}
		return result;
	}
	@Override
	public Select copy(SqlNode<?>parent){
		Select select = new Select();
		if(parent!=null){
			parent.addCopy(select);
		} 
		copyTo(select);
		return select;
	}
}
