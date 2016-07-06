package com.jerrylin.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

public class GroupConditions extends SqlNode {
	public FilterCondition cond(){
		FilterCondition fc = new FilterCondition();
		addChildren(fc);
		return fc;
	}
	public GroupConditions cond(String content, Object val, ValueFeatures valueFeatures){
		FilterCondition cond = cond();
		cond.content(content)
			.valueFeatures(valueFeatures);
		if(val != null){
			cond.value(val);
		}
		return this;
	}
	public GroupConditions cond(String content, ValueFeatures valueFeatures){
		cond().content(content).valueFeatures(valueFeatures);
		return this;
	}
	public GroupConditions cond(String content, Object val){
		cond().content(content).value(val);
		return this;
	}
	public GroupConditions cond(String content){
		cond().content(content);
		return this;
	}
	/**
	 * see the method {@link FilterCondition#strStartWith(String content, Object val)}.
	 * @param content
	 * @param val
	 * @return
	 */
	public GroupConditions strStartWith(String content, String val){
		cond().strStartWith(content, val);
		return this;
	}
	public GroupConditions strStartWith(String content){
		cond().strStartWith(content);
		return this;
	}
	public GroupConditions strContain(String content, String val){
		cond().strContain(content, val);
		return this;
	}
	public GroupConditions strContain(String content){
		cond().strContain(content);
		return this;
	}
	
	public GroupConditions strEndWith(String content, String val){
		cond().strEndWith(content, val);
		return this;
	}
	public GroupConditions strEndWith(String content){
		cond().strEndWith(content);
		return this;
	}
	
	public GroupConditions strExact(String content, String val){
		cond().strExact(content, val);
		return this;
	}
	public GroupConditions strExact(String content){
		cond().strExact(content);
		return this;
	}
	public GroupConditions strIgnoreCase(String content, String val){
		cond().strIgnoreCase(content, val);
		return this;
	}
	public GroupConditions strIgnoreCase(String content){
		cond().strIgnoreCase(content);
		return this;
	}
	public GroupConditions valueExpected(String content){
		cond().valueExpected(content);
		return this;
	}
	public GroupConditions valueExpected(String content, Object val){
		cond().valueExpected(content).value(val);
		return this;
	}
	private GroupConditions addOperator(String symbol){
		Operator operator = new Operator();
		operator.setSymbol(symbol);
		addChildren(operator);
		return this;
	}	
	public GroupConditions and(){
		return addOperator("AND");
	}
	public GroupConditions or(){
		return addOperator("OR");
	}
	public GroupConditions newGroup(){
		GroupConditions gc = new GroupConditions();
		addChildren(gc);
		return gc;
	}
	/**
	 * representing to end current group, turning to previous level;
	 * that is )
	 * @return
	 */
	public GroupConditions endGroup(){
		SqlNode parent = getParent();
		if(parent instanceof GroupConditions){
			return (GroupConditions)parent;
		}
		throw new RuntimeException("GroupConditions.end(): GroupConditions' parent should be the same type!!");
	}
	@Override
	public String genSql(){
		Object[] map = getChildren().stream().map(sn->sn.genSql()).toArray();
		if(map != null && map.length > 0){
			String groupConditions = "(" + StringUtils.join(map, " ") + ")";
			return groupConditions;
		}
		return "";
	}
	@Override
	public GroupConditions newInstance(){
		return new GroupConditions();
	}
	/**
	 * calculating operator happening start and end index<br>
	 * ex.<br>
	 * SELECT * FROM product p<br>
	 * WHERE AND OR p.name LIKE ? AND AND p.code = ? OR<br>
	 * results is:<br>
	 * first group:[0, 1], second group:[3, 4], third group:[6, 6]
	 * @param sn
	 * @return
	 */
	public static List<int[]> childrenOperatorIndex(SqlNode sn){
		int size = sn.getChildren().size();
		List<int[]> idxRange = new ArrayList<>();
		for(int i = 0; i < size; i++){
			int start = i;
			while(start < size && sn.getChildren().get(start) instanceof Operator){
				++start;
			}
			if(start > i){
				idxRange.add(new int[]{i, start - 1});
				i = start;
			}
		}
		return idxRange;
	}
	/**
	 * if there are wrong operators existed, remove them.<br>
	 * ex.<br>
	 * SELECT * FROM game g<br>
	 * WHERE OR g.point LIKE ? AND OR OR<br>
	 * =><br>
	 * SELECT * FROM game g<br>
	 * WHERE g.point LIKE ? 
	 * @param gc
	 */
	public static void removeNotRequiredSiblings(GroupConditions gc){
		if(gc == null){
			return;
		}
		gc.getChildren().forEach(sn->{
			if(sn instanceof GroupConditions){
				GroupConditions child = GroupConditions.class.cast(sn);
				removeNotRequiredSiblings(child);
			}
			if(sn instanceof FilterCondition){
				FilterCondition fc = FilterCondition.class.cast(sn);
				SelectExpression se = fc.findFirstByType(SelectExpression.class);
				if(se != null){
					removeNotRequiredSiblings(se.findFirstByType(Where.class));
				}
			}
		});
		gc.getChildren().removeIf(sn->sn instanceof GroupConditions && sn.getChildren().isEmpty());
		
		int size = gc.getChildren().size();
		int endIdx = size-1;
		List<int[]> found = childrenOperatorIndex(gc);
		// start with Operator, or end with, or within middle but there are multiple
		List<int[]> match = found.stream().filter(i-> i[0] == 0 || i[1] == endIdx || i[1] - i[0] > 0).collect(Collectors.toList());
		Set<Integer> set = new LinkedHashSet<>();
		match.forEach(i->{
			int start = i[0];
			int end = i[1];
			if(i[0] == 0 || i[1] == endIdx){
				for(; start <= end; start++){
					set.add(start);
				}
			}else{
				for(; start < end; start++){// last one not included
					set.add(start);
				}
			}
		});
		List<Integer> idx = new LinkedList<>(set);
		Collections.reverse(idx);
		System.out.println(idx);
		idx.forEach(i->{
			gc.getChildren().remove(i.intValue());
		});
	}	
}
