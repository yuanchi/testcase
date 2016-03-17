package com.jerrylin.erp.sql;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.jerrylin.erp.sql.condition.CollectConds;
import com.jerrylin.erp.sql.condition.SqlCondition.Junction;

public class Where extends SqlNode{
	private static final long serialVersionUID = -7259383899187705676L;
	public CollectConds collectConds(){
		CollectConds collectConds = CollectConds.getInstance();
		addChild(collectConds);
		return collectConds;
	}
	public CollectConds andConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.AND);
		return conds;
	}
	public CollectConds orConds(){
		CollectConds conds = collectConds();
		conds.junction(Junction.OR);
		return conds;		
	}
	@Override
	public String genSql() {
		List<String> items = getChildren().stream()
			.map(ISqlNode::genSql)
			.collect(Collectors.toList());
		String result = StringUtils.join(items, "\n");
		String andStart = Junction.AND.getSymbol() + " ";
		String orStart = Junction.OR.getSymbol() + " ";
		if(StringUtils.isNotBlank(result)){
			if(result.startsWith(andStart)){
				result = result.substring(andStart.length(), result.length());
			}
			if(result.startsWith(orStart)){
				result = result.substring(orStart.length(), result.length());
			}
			if(getChildren().size() == 1
			&& result.startsWith("(")
			&& result.endsWith(")")){
				result = result.substring(1, result.length()-1);
			}
			
			result = "WHERE " + result; 
		}
		return result;
	}
	@Override
	public ISqlNode singleCopy() {
		Where where = new Where();
		where.id(getId());
		return where;
	}
}
